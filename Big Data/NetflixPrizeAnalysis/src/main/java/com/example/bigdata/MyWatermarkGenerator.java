package com.example.bigdata;

import com.example.bigdata.model.NetflixPrize;
import org.apache.flink.api.common.eventtime.Watermark;
import org.apache.flink.api.common.eventtime.WatermarkGenerator;
import org.apache.flink.api.common.eventtime.WatermarkOutput;

public class MyWatermarkGenerator<Object> implements WatermarkGenerator<Object> {
    private long maxOutOfOrderness = 6000L;
    private long currentMaxTimestamp = 0;


    @Override
    public void onEvent(Object object, long l, WatermarkOutput watermarkOutput) {
        if (object instanceof NetflixPrize) {
            NetflixPrize np = (NetflixPrize) object;
            currentMaxTimestamp = Math.max(np.getTimestamp().longValue(), currentMaxTimestamp);
        }
    }

    @Override
    public void onPeriodicEmit(WatermarkOutput watermarkOutput) {
        watermarkOutput.emitWatermark(new Watermark(currentMaxTimestamp - maxOutOfOrderness - 1));
    }
}
