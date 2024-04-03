package edu.estu.model;

import java.util.List;

public class Movie {
    private String title;
    private List<Schedule> dates;

    public Movie(String title, List<Schedule> dates) {
        this.title = title;
        this.dates = dates;
    }

    public String getTitle() {
        return title;
    }

    public List<Schedule> getDates() {
        return dates;
    }
}
