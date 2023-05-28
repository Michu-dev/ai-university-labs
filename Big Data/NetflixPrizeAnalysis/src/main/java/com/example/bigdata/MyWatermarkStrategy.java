package com.example.bigdata;

import org.apache.flink.api.common.eventtime.*;

public class MyWatermarkStrategy<Object> implements WatermarkStrategy<Object> {

    @Override
    public WatermarkGenerator<Object> createWatermarkGenerator(WatermarkGeneratorSupplier.Context context) {
        return new MyWatermarkGenerator<Object>();
    }

    @Override
    public TimestampAssigner<Object> createTimestampAssigner(TimestampAssignerSupplier.Context context) {
        return new MyTimestampAssigner<Object>();
    }
}
