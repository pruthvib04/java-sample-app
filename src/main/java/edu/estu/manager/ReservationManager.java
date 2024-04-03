package edu.estu.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import edu.estu.CinemaReservationCLI;
import edu.estu.model.Movie;
import edu.estu.model.Reservation;
import edu.estu.model.Schedule;
import edu.estu.model.User;
import org.fusesource.jansi.Ansi;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import static edu.estu.CinemaReservationCLI.*;

public class ReservationManager {

    static final String MOVIES_JSON = "movies.json";

    private final Gson gson;
    private final Scanner scanner;
    private final UserManager userManager;

    public ReservationManager(UserManager userManager, Scanner scanner, Gson gson) {
        this.userManager = userManager;
        this.scanner = scanner;
        this.gson = gson;
    }

    List<Movie> readMovies() throws IOException {
        TypeToken<List<Movie>> token = new TypeToken<>() {
        };

        try (FileReader reader = new FileReader(MOVIES_JSON)) {
            List<Movie> movies = gson.fromJson(reader, token.getType());

            // Check and initialize reservedSeats list if it is missing
            for (Movie movie : movies) {
                if (movie.getDates() != null) {
                    for (Schedule schedule : movie.getDates()) {
                        if (schedule.getReservedSeats() == null) {
                            schedule.setReservedSeats(new ArrayList<>());
                        }
                    }
                }
            }
            return movies;
        } catch (FileNotFoundException e) {
            return Collections.emptyList();
        } catch (IOException e) {
            throw new IOException(e.getMessage());
        }
    }

    void writeMovies(List<Movie> movies) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(movies);
        Files.writeString(Path.of(MOVIES_JSON), json);
    }

    public void makeReservation() throws IOException {
        showMessage("Available movies:", Ansi.Color.YELLOW);
        List<Movie> movies = readMovies();

        if (movies.isEmpty()) {
            showMessage("Currently, there are no movies. Please come back later.", Ansi.Color.RED);
            displayReservationMenu();
            return;
        }

        Movie selectedMovie = selectMovie(movies);
        showMessage("You have selected: " + selectedMovie.getTitle(), Ansi.Color.GREEN);

        int selectedDateIndex = selectDate(selectedMovie);
        List<Integer> reservedSeats = selectedMovie.getDates().get(selectedDateIndex).getReservedSeats();

        showMessage("Available seats (1-20) - Reserved seats: " + reservedSeats, Ansi.Color.YELLOW);

        int seatNumber = selectSeat();

        while (reservedSeats.contains(seatNumber)) {
            showMessage("Seat number " + seatNumber + " is already reserved. Please choose another seat.", Ansi.Color.RED);
            seatNumber = selectSeat();
        }

        // Insert the seat number in the reserved seats list in sorted order
        int insertIndex = Collections.binarySearch(reservedSeats, seatNumber);
        insertIndex = (insertIndex < 0) ? -insertIndex - 1 : insertIndex;
        reservedSeats.add(insertIndex, seatNumber);
        writeMovies(movies);

        String movieTitle = selectedMovie.getTitle();
        String date = selectedMovie.getDates().get(selectedDateIndex).getDate();
        Reservation reservation = new Reservation(movieTitle, date, seatNumber);
        currentUser.getReservations().add(reservation);

        updateUsersJSON(currentUser);

        showMessage("Reservation successful! Seat number " + seatNumber + " is reserved for you.", Ansi.Color.GREEN);
        displayReservationMenu();
    }

    private Movie selectMovie(List<Movie> movies) throws IOException {
        for (int i = 0; i < movies.size(); i++) {
            showMessage((i + 1) + ". " + movies.get(i).getTitle(), Ansi.Color.YELLOW);
        }

        int movieNumber = CinemaReservationCLI.getIntInput();

        // Allow the user to go back to the previous menu
        if (movieNumber == 0) {
            displayReservationMenu();
        }

        // Check if the user entered a valid movie number
        while (movieNumber < 1 || movieNumber > movies.size()) {
            showMessage("Invalid movie number. Please choose again.", Ansi.Color.RED);
            movieNumber = CinemaReservationCLI.getIntInput();
        }

        return movies.get(movieNumber - 1);
    }

    private int selectDate(Movie movie) throws IOException {
        for (int i = 0; i < movie.getDates().size(); i++) {
            showMessage((i + 1) + ". " + movie.getDates().get(i).getDate(), Ansi.Color.YELLOW);
        }
        int dateNumber = CinemaReservationCLI.getIntInput();

        // Allow the user to go back to the previous menu
        if (dateNumber == 0) {
            displayReservationMenu();
        }

        // Check if the user entered a valid date number
        while (dateNumber < 1 || dateNumber > movie.getDates().size()) {
            showMessage("Invalid date number. Please choose again.", Ansi.Color.RED);
            dateNumber = CinemaReservationCLI.getIntInput();
        }

        return dateNumber - 1;
    }

    private int selectSeat() throws IOException {
        showMessage("Please select a seat number (1-20): ", Ansi.Color.CYAN);
        showMessage("Enter 0 to go back", Ansi.Color.CYAN);
        int seatNumber = CinemaReservationCLI.getIntInput();

        // Allow the user to go back to the previous menu
        if (seatNumber == 0) {
            displayReservationMenu();
        }

        // Check if the user entered a valid seat number
        while (seatNumber < 1 || seatNumber > 20) {
            showMessage("Invalid seat number. Please choose again.", Ansi.Color.RED);
            seatNumber = selectSeat();
        }

        return seatNumber;
    }

    public void listReservations() throws IOException {
        List<Reservation> userReservations = currentUser.getReservations();

        if (userReservations.isEmpty()) {
            showMessage("You don't have any reservations yet.", Ansi.Color.RED);
            displayReservationMenu();
        } else {
            showMessage("Your reservations:", Ansi.Color.YELLOW);
            for (int i = 0; i < userReservations.size(); i++) {
                Reservation reservation = userReservations.get(i);
                showMessage((i + 1) + ". Movie: " + reservation.getMovieTitle() +
                        ", Date: " + reservation.getDate() +
                        ", Seat Number: " + reservation.getSeatNumber(), Ansi.Color.YELLOW);

                // Check if the reservation has a review
                if (reservation.getReview() != null && !reservation.getReview().isEmpty()) {
                    showMessage("   - Review: " + reservation.getReview(), Ansi.Color.YELLOW);
                }
            }

            // Add an option to go back at the end of the list
            showMessage((userReservations.size() + 1) + ". Go back", Ansi.Color.YELLOW);

            int choice = getIntInput();

            if (choice == userReservations.size() + 1) {
                displayReservationMenu();
            } else if (choice > 0 && choice <= userReservations.size()) {
                reviewReservation(userReservations.get(choice - 1));
            } else {
                showMessage("Invalid option. Please choose again.", Ansi.Color.RED);
                listReservations();
            }
        }
    }

    private void reviewReservation(Reservation reservation) throws IOException {
        showMessage("Your reservation details:", Ansi.Color.YELLOW);
        showMessage("Movie: " + reservation.getMovieTitle() +
                ", Date: " + reservation.getDate() +
                ", Seat Number: " + reservation.getSeatNumber(), Ansi.Color.YELLOW);

        if (reservation.getReview() != null && !reservation.getReview().isEmpty()) {
            showMessage("Your review: " + reservation.getReview(), Ansi.Color.YELLOW);
            showMessage("1. Edit review\n2. Delete review\n3. Delete reservation\n4. Go back", Ansi.Color.YELLOW);

            int choice = CinemaReservationCLI.getIntInput();

            switch (choice) {
                case 1:
                    editReview(reservation);
                    break;
                case 2:
                    deleteReview(reservation);
                    break;
                case 3:
                    deleteReservation(reservation);
                    break;
                case 4:
                    listReservations();
                    break;
                default:
                    showMessage("Invalid option. Please choose again.", Ansi.Color.RED);
                    reviewReservation(reservation);
                    break;
            }
        } else {
            showMessage("1. Add review\n2. Delete reservation\n3. Go back", Ansi.Color.YELLOW);

            int choice = CinemaReservationCLI.getIntInput();

            switch (choice) {
                case 1:
                    addReview(reservation);
                case 2:
                    deleteReservation(reservation);
                    break;
                case 3:
                    listReservations();
                    break;
                default:
                    showMessage("Invalid option. Please choose again.", Ansi.Color.RED);
                    reviewReservation(reservation);
                    break;
            }
        }
    }

    private void addReview(Reservation reservation) throws IOException {
        showMessage("Enter your review for the reservation:", Ansi.Color.YELLOW);
        String review = scanner.nextLine();

        if (review.isEmpty()) {
            showMessage("Review addition canceled.", Ansi.Color.RED);
            reviewReservation(reservation);
        }

        reservation.setReview(review);
        updateUsersJSON(currentUser); // Save changes to users.json

        showMessage("Review added successfully!", Ansi.Color.GREEN);
        listReservations();
    }

    private void editReview(Reservation reservation) throws IOException {
        if (reservation.getReview() == null || reservation.getReview().isEmpty()) {
            showMessage("You haven't written a review for this reservation yet.", Ansi.Color.YELLOW);
            reviewReservation(reservation);
        } else {
            showMessage("Your current review: " + reservation.getReview(), Ansi.Color.YELLOW);
            showMessage("Enter your updated review:", Ansi.Color.YELLOW);
            String updatedReview = scanner.nextLine();

            if (updatedReview.isEmpty()) {
                showMessage("Review update canceled.", Ansi.Color.YELLOW);
                reviewReservation(reservation);
            }

            reservation.setReview(updatedReview);
            updateUsersJSON(currentUser); // Save changes to users.json

            showMessage("Review updated successfully!", Ansi.Color.GREEN);
            listReservations();
        }
    }

    private void deleteReview(Reservation reservation) throws IOException {
        showMessage("Are you sure you want to delete this review?\n1. Yes\n2. No", Ansi.Color.YELLOW);

        int confirmationChoice = getIntInput();

        if (confirmationChoice == 1) { // Delete review
            reservation.setReview(null);
            updateUsersJSON(currentUser); // Save changes to users.json
            showMessage("Review deleted successfully!", Ansi.Color.GREEN);
            listReservations();
        } else if (confirmationChoice == 2) { // Cancel deletion
            showMessage("Review deletion canceled.", Ansi.Color.RED);
            listReservations();
        } else { // Invalid option
            showMessage("Invalid option. Please choose again.", Ansi.Color.RED);
            deleteReview(reservation);
        }
    }

    private void deleteReservation(Reservation reservation) throws IOException {
        showMessage("Are you sure you want to delete this reservation?\n1. Yes\n2. No", Ansi.Color.YELLOW);

        int confirmationChoice = getIntInput();

        if (confirmationChoice == 1) { // Delete reservation
            List<Reservation> userReservations = currentUser.getReservations();
            userReservations.remove(reservation);

            // Remove the reserved seat from the corresponding schedule
            List<Movie> movies = readMovies();
            for (Movie movie : movies) {
                for (Schedule schedule : movie.getDates()) {
                    if (schedule.getDate().equals(reservation.getDate())) {
                        schedule.getReservedSeats().remove(Integer.valueOf(reservation.getSeatNumber()));
                        break;
                    }
                }
            }

            writeMovies(movies); // Save changes to movies.json
            updateUsersJSON(currentUser); // Save changes to users.json

            showMessage("Reservation deleted successfully!", Ansi.Color.GREEN);
            listReservations();
        } else if (confirmationChoice == 2) { // Cancel deletion
            showMessage("Reservation deletion canceled.", Ansi.Color.RED);
            listReservations();
        } else { // Invalid option
            showMessage("Invalid option. Please choose again.", Ansi.Color.RED);
            deleteReservation(reservation);
        }
    }

    private void updateUsersJSON(User currentUser) {
        try { // Update the user's reservations in users.json
            List<User> users = userManager.readUsersFromJSON();
            for (int i = 0; i < users.size(); i++) {
                if (users.get(i).getUsername().equals(currentUser.getUsername())) {
                    users.set(i, currentUser);
                    break;
                }
            }
            userManager.writeUsersToJSON(users);
        } catch (IOException e) {
            showMessage("An error occurred while trying to update user data. Error details: " + e.getMessage(), Ansi.Color.RED);
        }
    }

}
