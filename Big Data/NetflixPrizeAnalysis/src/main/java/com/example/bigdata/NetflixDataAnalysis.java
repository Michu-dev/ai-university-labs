package com.example.bigdata;

import com.example.bigdata.agg.AnomalyFilterFunction;
import com.example.bigdata.agg.MovieAggregationFunction;
import com.example.bigdata.connectors.Connectors;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

public class NetflixDataAnalysis {
    public static void main(String[] args) throws Exception {
        // Set up the execution environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        ParameterTool propertiesFromFile = ParameterTool.fromPropertiesFile("src/main/resources/flink.properties");
        ParameterTool propertiesFromArgs = ParameterTool.fromArgs(args);
        ParameterTool properties = propertiesFromFile.mergeWith(propertiesFromArgs);

        DataStream<String> inputStream = env
                .fromSource(Connectors.getFileSource(properties),
                        WatermarkStrategy.noWatermarks(), "Kafka Source");

        // Read the streaming data from the main dataset
        DataStream<Tuple4<String, Integer, Integer, Double>> streamingData = inputStream
                .map(line -> {
                    String[] fields = line.split(",");
                    String date = fields[0];
                    int filmId = Integer.parseInt(fields[1]);
                    int userId = Integer.parseInt(fields[2]);
                    double rate = Double.parseDouble(fields[3]);
                    return new Tuple4<>(date, filmId, userId, rate);
                })
                .assignTimestampsAndWatermarks(new BoundedOutOfOrdernessTimestampExtractor<Tuple4<String, Integer, Integer, Double>>(Time.days(1)) {
                    @Override
                    public long extractTimestamp(Tuple4<String, Integer, Integer, Double> element) {
                        // Extract the timestamp from the date field
                        return Instant.parse(element.f0).toEpochMilli();
                    }
                });

        // Read the static data from the movie_titles.csv file
        DataStream<Tuple4<Integer, Integer, String, Set<Integer>>> staticData = env
                .readTextFile("C:\\Users\\micha\\ai-university-labs\\Big Data\\SensorDataAnalysis\\movie_titles.csv")
                .map(line -> {
                    String[] fields = line.split(",");
                    int filmId = Integer.parseInt(fields[0]);
                    int year = Integer.parseInt(fields[1]);
                    String title = fields[2];
                    return new Tuple4<>(filmId, year, title, new HashSet<>());
                });

        // Perform the aggregation on the streaming data
        DataStream<Tuple4<String, Integer, Integer, Double>> aggregatedData = streamingData
                .keyBy(1) // Key by filmId
                .window(TumblingEventTimeWindows.of(Time.days(30))) // Tumbling window of 30 days
                .aggregate(new MovieAggregationFunction());

        // Filter the aggregated data to find anomalies
        DataStream<Tuple4<String, Integer, Integer, Double>> anomalies = aggregatedData
                .filter(new AnomalyFilterFunction(100, 4.0));



        // Print the result to stdout
        anomalies.print();

        // Execute the job
        env.execute("Movie Anomaly Detection");
    }
}
