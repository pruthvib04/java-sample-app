package edu.estu.model;

import java.util.ArrayList;
import java.util.List;

public class User {
    private final String username;
    private final String password;
    private List<Reservation> reservations;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.reservations = new ArrayList<>();
    }

    public User(String username, String password, List<Reservation> reservations) {
        this.username = username;
        this.password = password;
        this.reservations = reservations;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public List<Reservation> getReservations() {
        return reservations;
    }

}
