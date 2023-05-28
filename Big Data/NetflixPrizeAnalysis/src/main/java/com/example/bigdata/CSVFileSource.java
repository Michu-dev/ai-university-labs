package com.example.bigdata;

import org.apache.flink.streaming.api.functions.source.SourceFunction;

import java.io.BufferedReader;
import java.io.FileReader;

public class CSVFileSource implements SourceFunction<String> {
    private final String filePath;

    public CSVFileSource(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public void run(SourceContext<String> sourceContext) throws Exception {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sourceContext.collect(line);
            }
        }
    }

    @Override
    public void cancel() {

    }
}
