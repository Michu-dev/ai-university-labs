package com.example.bigdata;

import com.example.bigdata.model.NetflixPrize;
import com.example.bigdata.model.NetflixPrizeAgg;
import org.apache.flink.api.common.functions.AggregateFunction;

import java.util.HashSet;

public class MyAggregateFunction implements AggregateFunction<NetflixPrize, NetflixPrizeAgg, NetflixPrizeAgg> {

    @Override
    public NetflixPrizeAgg createAccumulator() {
        return new NetflixPrizeAgg(-1, "", "", -1L, -1L, -1L, new HashSet<Long>(), -1L);
    }

    @Override
    public NetflixPrizeAgg add(NetflixPrize netflixPrize, NetflixPrizeAgg netflixPrizeAgg) {

        return new NetflixPrizeAgg(
                netflixPrize.getFilmId(),
                netflixPrize.getTitle(),
                netflixPrize.getDate().substring(0, 7),
                Long.valueOf(1),
                Long.valueOf(netflixPrize.getRate()),
                Long.valueOf(1),
                new HashSet<Long>(),
                netflixPrize.getUserId()
        );
    }

    @Override
    public NetflixPrizeAgg getResult(NetflixPrizeAgg netflixPrizeAgg) {
        return netflixPrizeAgg;
    }

    @Override
    public NetflixPrizeAgg merge(NetflixPrizeAgg acc1, NetflixPrizeAgg acc2) {
        return new NetflixPrizeAgg(
                acc1.getFilmId(),
                acc1.getTitle(),
                acc1.getMonth(),
                acc1.getRanksCount() + 1,
                acc1.getRanksSum() + acc2.getRanksSum(),
                (acc1.getPeople().contains(acc2.getUserId()) ? acc1.getUniquePeopleCount() + Long.valueOf(0) : acc1.getUniquePeopleCount() + Long.valueOf(1)),
                acc1.getPeople(),
                acc2.getUserId()
        );
    }
}
