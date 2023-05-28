package com.example.bigdata.model;

import org.jline.utils.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;

public class NetflixPrizeAgg {
    private Integer filmId;
    private String title;
    private String month;
    private Long ranksCount;
    private Set<Long> people;
    private Long ranksSum;
    private Long userId;
    private Long uniquePeopleCount;

    public NetflixPrizeAgg() {
        this(-1, "", "", -1L, -1L, -1L, new HashSet<Long>(), -1L);
    }

    public NetflixPrizeAgg(Integer filmId, String title, String month, Long ranksCount, Long ranksSum, Long uniquePeopleCount, Set<Long> people, Long userId) {
        this.filmId = filmId;
        this.title = title;
        this.month = month;
        this.ranksCount = ranksCount;
        this.ranksSum = ranksSum;
        this.uniquePeopleCount = uniquePeopleCount;
        this.userId = userId;
        this.people = people;
        this.people.add(userId);
    }

    public Integer getFilmId() {
        return filmId;
    }

    public void setFilmId(Integer filmId) {
        this.filmId = filmId;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public Long getRanksCount() {
        return ranksCount;
    }

    public void setRanksCount(Long ranksCount) {
        this.ranksCount = ranksCount;
    }

    public Long getRanksSum() {
        return ranksSum;
    }

    public void setRanksSum(Long ranksSum) {
        this.ranksSum = ranksSum;
    }

    public Set<Long> getPeople() {
        return people;
    }

    public void setPeople(Set<Long> people) {
        this.people = people;
    }

    public Long getUniquePeopleCount() {
        return uniquePeopleCount;
    }

    public void setUniquePeopleCount(Long uniquePeopleCount) {
        this.uniquePeopleCount = uniquePeopleCount;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public Long getTimestamp() {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
        Long timestamp = Long.valueOf(-1);
        try {
            timestamp = f.parse(this.month + "-01").getTime();
        } catch (ParseException ex) {
            ex.printStackTrace();
        }

        return timestamp;
    }

    @Override
    public String toString() {
        return "NetflixPrizeAgg{" +
                "filmId=" + filmId +
                ", month='" + month + '\'' +
                ", ranksCount=" + ranksCount +
                ", ranksSum=" + ranksSum +
                ", uniquePeopleCount=" + uniquePeopleCount +
                '}' +
                String.valueOf(getTimestamp());
    }
}
