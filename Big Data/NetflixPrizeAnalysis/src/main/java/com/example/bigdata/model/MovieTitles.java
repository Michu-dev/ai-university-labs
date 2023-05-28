package com.example.bigdata.model;

public class MovieTitles {
    private Integer filmId;
    private Integer year;
    private String title;

    public MovieTitles() {
        this(-1, -1, "");
    }

    public MovieTitles(Integer filmId, Integer year, String title) {
        this.filmId = filmId;
        this.year = year;
        this.title = title;
    }

    public Integer getFilmId() {
        return filmId;
    }

    public void setFilmId(Integer filmId) {
        this.filmId = filmId;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "MovieTitles{" +
                "filmId=" + filmId +
                ", year=" + year +
                ", title='" + title + '\'' +
                '}';
    }
}
