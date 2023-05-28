package com.example.bigdata;

import com.example.bigdata.model.NetflixPrizeAgg;
import com.example.bigdata.model.SensorDataAgg;
import org.apache.flink.api.common.functions.ReduceFunction;

public class NetflixReduceFunction implements ReduceFunction<NetflixPrizeAgg> {
    @Override
    public NetflixPrizeAgg reduce(NetflixPrizeAgg sd1, NetflixPrizeAgg sd2) throws Exception {
        return new NetflixPrizeAgg(
                sd1.getFilmId(),
                sd1.getTitle(),
                sd1.getMonth(),
                sd1.getRanksCount() + 1,
                sd1.getRanksSum() + sd2.getRanksSum(),
                sd1.getUniquePeopleCount() + (sd1.getPeople().contains(sd2.getUserId()) ? Long.valueOf(0) : Long.valueOf(1)),
                sd1.getPeople(),
                sd2.getUserId()
        );
    }
}

