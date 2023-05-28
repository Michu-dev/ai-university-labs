package com.example.bigdata;

import com.example.bigdata.model.SensorDataAgg;
import org.apache.flink.api.common.functions.ReduceFunction;

public class MyReduceFunction implements ReduceFunction<SensorDataAgg> {
    @Override
    public SensorDataAgg reduce(SensorDataAgg sd1, SensorDataAgg sd2) throws Exception {
        return new SensorDataAgg(
                sd1.getSensor(),
                Math.max(sd1.getMaxVal(), sd2.getMaxVal()),
                sd1.getMaxVal() > sd2.getMaxVal() ? sd1.getMaxValTimestamp() : sd2.getMaxValTimestamp(),
                Math.min(sd1.getMinVal(), sd2.getMinVal()),
                sd1.getMinVal() < sd2.getMaxVal() ? sd1.getMinValTimestamp() : sd2.getMinValTimestamp(),
                sd1.getCountVal() + 1,
                sd1.getSumVal() + sd2.getSumVal()
        );
    }
}

