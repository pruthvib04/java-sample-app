package edu.estu.model;

public class Reservation {
    private String movieTitle;
    private String date;
    private int seatNumber;
    private String review;

    public Reservation(String movieTitle, String date, int seatNumber) {
        this.movieTitle = movieTitle;
        this.date = date;
        this.seatNumber = seatNumber;
        this.review = null;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public String getDate() {
        return date;
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }
}
