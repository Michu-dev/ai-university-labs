package com.example.bigdata.agg;

import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.java.tuple.Tuple4;

public class MovieAggregationFunction implements AggregateFunction<Tuple4<String, Integer, Integer, Double>, Tuple4<String, Integer, Integer, Double>, Tuple4<String, Integer, Integer, Double>> {
    @Override
    public Tuple4<String, Integer, Integer, Double> createAccumulator() {
        return new Tuple4<>("", 0, 0, 0.0);
    }

    @Override
    public Tuple4<String, Integer, Integer, Double> add(Tuple4<String, Integer, Integer, Double> value,
                                                        Tuple4<String, Integer, Integer, Double> accumulator) {
        return new Tuple4<>(value.f0, value.f1, accumulator.f2 + 1, accumulator.f3 + value.f3);
    }

    @Override
    public Tuple4<String, Integer, Integer, Double> getResult(Tuple4<String, Integer, Integer, Double> accumulator) {
        return accumulator;
    }

    @Override
    public Tuple4<String, Integer, Integer, Double> merge(Tuple4<String, Integer, Integer, Double> a,
                                                          Tuple4<String, Integer, Integer, Double> b) {
        return new Tuple4<>(a.f0, a.f1, a.f2 + b.f2, a.f3 + b.f3);
    }
}
