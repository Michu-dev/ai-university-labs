package com.example.bigdata;

import com.example.bigdata.connectors.Connectors;
import com.example.bigdata.model.*;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.api.java.tuple.Tuple5;
import org.apache.flink.api.java.utils.ParameterTool;

import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Random;


public class SensorDataAnalysis {
    public static void main(String[] args) throws Exception {

        ParameterTool propertiesFromFile = ParameterTool.fromPropertiesFile("src/main/resources/flink.properties");
        ParameterTool propertiesFromArgs = ParameterTool.fromArgs(args);
        ParameterTool properties = propertiesFromFile.mergeWith(propertiesFromArgs);

        KafkaSource<String> source = KafkaSource.<String>builder()
                .setBootstrapServers(args[0])
                .setTopics(args[1])
                .setGroupId("my-group")
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();

        WatermarkStrategy<NetflixPrizeAgg> strategy = WatermarkStrategy.<NetflixPrizeAgg>forBoundedOutOfOrderness(Duration.ofDays(1))
                .withIdleness(Duration.ofSeconds(1))
                .withTimestampAssigner((event, timestamp) -> event.getTimestamp());

        if (args.length > 2 && args[2] == "C") {
            strategy = WatermarkStrategy.<NetflixPrizeAgg>forBoundedOutOfOrderness(Duration.ofSeconds(1))
                    .withIdleness(Duration.ofSeconds(1))
                    .withTimestampAssigner((event, timestamp) -> event.getTimestamp());
        }


        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        DataStream<String> inputStream = env
                .fromSource(source,
                        WatermarkStrategy.noWatermarks(), "Kafka Source");

        String filePath = "input_data\\movie_titles.csv";
        CSVFileSource sourceFunction = new CSVFileSource(filePath);

        DataStream<String> titlesStream = env.addSource(sourceFunction);

//        DataStream<String> inputStream = env
//                .fromSource(Connectors.getFileSource(properties),
//                        WatermarkStrategy.noWatermarks(), "SensorData");

        DataStream<MovieTitles> movieTitles = titlesStream.map((MapFunction<String, String[]>) txt -> txt.split(",") )
                .filter(array -> array.length == 3)
                .filter(array -> array[0].matches("\\d+") && array[1].matches("\\d+"))
                .map(array -> new MovieTitles(Integer.parseInt(array[0]), Integer.parseInt(array[1]), array[2]));

        DataStream<NetflixPrize> netflixPrizeDS = inputStream.map((MapFunction<String, String[]>) txt -> txt.split(",") )
                .filter(array -> array.length == 4)
                .filter(array -> array[1].matches("\\d+") && array[2].matches("\\d+") && array[3].matches("\\d+"))
                .map(array -> new NetflixPrize(array[0], Integer.parseInt(array[1]), Long.parseLong(array[2]), Integer.parseInt(array[3])));

        DataStream<NetflixPrize> rr = netflixPrizeDS.join(movieTitles)
                .where(netflixPrize -> netflixPrize.getFilmId())
                .equalTo(movieTitles1 -> movieTitles1.getFilmId())
                .window(TumblingProcessingTimeWindows.of(Time.seconds(1)))
                .apply((r, s) -> new NetflixPrize(r.getDate(), r.getFilmId(), r.getUserId(), r.getRate(), s.getTitle(), s.getYear()));




        DataStream<NetflixPrizeAgg> netflixPrizeExtDS = rr
                .map(sd -> new NetflixPrizeAgg(
                        sd.getFilmId(),
                        sd.getTitle(),
                        sd.getDate().substring(0, 7),
                        Long.valueOf(1),
                        Long.valueOf(sd.getRate()),
                        Long.valueOf(1),
                        new HashSet<Long>(),
                        sd.getUserId()))
                .assignTimestampsAndWatermarks(strategy);
//                .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<NetflixPrizeAgg>() {
//
//                    @Override
//                    public long extractAscendingTimestamp(NetflixPrizeAgg netflixPrizeAgg) {
//                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//                        try {
//                            Random r = new Random();
//                            int low = 10;
//                            int high = 30;
//                            int result = r.nextInt(high-low) + low;
//                            return dateFormat.parse(netflixPrizeAgg.getMonth() + "-" + String.valueOf(result)).getTime();
//                        } catch (ParseException e) {
//                            return 0;
//                        }
//                    }
//                });

        KeyedStream<NetflixPrizeAgg, Object> dataKeyedByFilmId = netflixPrizeExtDS.keyBy(event -> new Tuple3<Integer, String, String>(event.getFilmId(), event.getTitle(), event.getMonth()));



        DataStream<NetflixPrizeAgg> result = dataKeyedByFilmId.window(TumblingEventTimeWindows.of(Time.days(720))).reduce(new NetflixReduceFunction());


//
//        DataStream<NetflixPrize> rr1 = rr.assignTimestampsAndWatermarks(new MyWatermarkStrategy<NetflixPrize>());

        result.addSink(Connectors.getMySQLSink(properties));

        env.execute("NetflixDataAnalysis");
    }
}
