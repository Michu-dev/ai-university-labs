package com.example.bigdata.agg;

import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.java.tuple.Tuple4;

public class AnomalyFilterFunction implements FilterFunction<Tuple4<String, Integer, Integer, Double>> {
    private final int minNumRatings;
    private final double minAverageRating;

    public AnomalyFilterFunction(int minNumRatings, double minAverageRating) {
        this.minNumRatings = minNumRatings;
        this.minAverageRating = minAverageRating;
    }

    @Override
    public boolean filter(Tuple4<String, Integer, Integer, Double> value) throws Exception {
        int numRatings = value.f2;
        double averageRating = value.f3 / numRatings;
        return numRatings >= minNumRatings && averageRating >= minAverageRating;
    }
}
