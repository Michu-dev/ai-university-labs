package com.example.bigdata.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class NetflixPrize {
    private String date;
    private Integer filmId;
    private Long userId;
    private Integer rate;
    private String title;
    private Integer year;


    public NetflixPrize() {

        this("1900-01-01", -1, -1L, -1);
    }

    public NetflixPrize(String date, Integer filmId, Long userId, Integer rate) {
        this.date = date;
        this.filmId = filmId;
        this.userId = userId;
        this.rate = rate;

    }

    public NetflixPrize(String date, Integer filmId, Long userId, Integer rate, String title, Integer year) {
        this.date = date;
        this.filmId = filmId;
        this.userId = userId;
        this.rate = rate;
        this.title = title;
        this.year = year;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Integer getFilmId() {
        return filmId;
    }

    public void setFilmId(Integer filmId) {
        this.filmId = filmId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getRate() {
        return rate;
    }

    public void setRate(Integer rate) {
        this.rate = rate;
    }

    public Long getTimestamp() {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
        Long timestamp = Long.valueOf(-1);
        try {
            timestamp = f.parse(this.date).getTime();
        } catch (ParseException ex) {
            ex.printStackTrace();
        }

        return timestamp;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "NetflixPrize{" +
                "date='" + date + '\'' +
                ", filmId=" + filmId +
                ", userId=" + userId +
                ", rate=" + rate +
                ", title='" + title + '\'' +
                ", year=" + year +
                '}';
    }
}
