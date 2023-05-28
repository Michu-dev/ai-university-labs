package com.example.bigdata;

import com.example.bigdata.model.NetflixPrize;
import com.example.bigdata.model.NetflixPrizeAgg;
import org.apache.flink.api.common.eventtime.TimestampAssigner;

public class MyTimestampAssigner<Object> implements TimestampAssigner<Object> {


    @Override
    public long extractTimestamp(Object o, long l) {
        if (o instanceof NetflixPrize) {
            NetflixPrize np = (NetflixPrize) o;
            return np.getTimestamp().longValue();
        } else if (o instanceof NetflixPrizeAgg) {
            NetflixPrizeAgg np = (NetflixPrizeAgg) o;
            return np.getTimestamp().longValue();
        }
        return -1;
    }
}
