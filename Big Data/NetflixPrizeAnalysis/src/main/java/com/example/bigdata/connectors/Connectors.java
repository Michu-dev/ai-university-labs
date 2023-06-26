package com.example.bigdata.connectors;

import com.example.bigdata.model.NetflixPrizeAgg;
import com.example.bigdata.model.SensorDataAgg;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.connector.file.src.FileSource;
import org.apache.flink.connector.file.src.reader.TextLineInputFormat;
import org.apache.flink.connector.jdbc.JdbcConnectionOptions;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.JdbcSink;
import org.apache.flink.connector.jdbc.JdbcStatementBuilder;
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;

public class Connectors {
    public static FileSource<String> getFileSource(ParameterTool properties) {
        return FileSource
                .forRecordStreamFormat(new TextLineInputFormat(),
                        new Path(properties.getRequired("fileInput.uri")))
                .monitorContinuously(Duration.ofMillis(
                        Long.parseLong(properties.getRequired("fileInput.interval"))))
                .build();
    }

    public static SinkFunction<NetflixPrizeAgg> getMySQLSink(ParameterTool properties) {
        JdbcStatementBuilder<NetflixPrizeAgg> statementBuilder =
                new JdbcStatementBuilder<NetflixPrizeAgg>() {
                    @Override
                    public void accept(PreparedStatement ps, NetflixPrizeAgg data) throws SQLException {
                        ps.setInt(1, data.getFilmId());
                        ps.setString(2, data.getTitle());
                        ps.setString(3, data.getMonth());
                        ps.setLong(4, data.getRanksCount());
                        ps.setLong(5, data.getRanksSum());
                        ps.setLong(6, data.getUniquePeopleCount());
                    }
                };
        JdbcConnectionOptions connectionOptions = new
                JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                .withUrl(properties.getRequired("mysql.url"))
                .withDriverName("com.mysql.jdbc.Driver")
                .withUsername(properties.getRequired("mysql.username"))
                .withPassword(properties.getRequired("mysql.password"))
                .build();
        JdbcExecutionOptions executionOptions = JdbcExecutionOptions.builder()
                .withBatchSize(100)
                .withBatchIntervalMs(200)
                .withMaxRetries(5)
                .build();

        SinkFunction<NetflixPrizeAgg> jdbcSink =
                JdbcSink.sink("insert into netflix_prize_sink" +
                                "(film_id, title, month, " +
                                "ranks_count, ranks_sum, unique_people_count) \n" +
                                "values (?, ?, ?, ?, ?, ?)",
                        statementBuilder,
                        executionOptions,
                        connectionOptions);
        return jdbcSink;

    }





}
