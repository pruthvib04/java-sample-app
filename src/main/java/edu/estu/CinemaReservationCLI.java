package edu.estu;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.estu.manager.ReservationManager;
import edu.estu.manager.UserManager;
import edu.estu.model.User;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import java.io.IOException;
import java.util.Scanner;

public class CinemaReservationCLI {

    private static final Scanner scanner = new Scanner(System.in);
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final UserManager userManager = new UserManager(scanner, gson);
    private static final ReservationManager reservationManager = new ReservationManager(userManager, scanner, gson);
    public static User currentUser = null;

    public static void showMessage(String message, Ansi.Color color) {
        System.out.println(Ansi.ansi().fg(color).a(message).reset());
    }

    public static void main(String[] args) {
        AnsiConsole.systemInstall();

        showMessage("╔═══════════════════════════════════════════════╗", Ansi.Color.YELLOW);
        showMessage("║        Welcome to the Cinema Reservation      ║", Ansi.Color.YELLOW);
        showMessage("║                     System                    ║", Ansi.Color.YELLOW);
        showMessage("╚═══════════════════════════════════════════════╝", Ansi.Color.YELLOW);

        displayMainMenu();
        AnsiConsole.systemUninstall();
    }

    public static void displayMainMenu() {
        showMessage("1. Login\n2. Register\n3. Exit", Ansi.Color.YELLOW);
        int choice = getIntInput();
        switch (choice) {
            case 1:
                userManager.login();
                break;
            case 2:
                userManager.register();
                break;
            case 3:
                exit();
                break;
            default:
                showMessage("Invalid option. Please choose again.", Ansi.Color.RED);
                displayMainMenu();
                break;
        }
    }

    public static void displayReservationMenu() throws IOException {
        showMessage("1. Make a reservation\n2. List reservations\n3. Exit", Ansi.Color.YELLOW);
        int choice = getIntInput();
        switch (choice) {
            case 1:
                try {
                    reservationManager.makeReservation();
                } catch (IOException e) {
                    showMessage("An error occurred while trying to make the reservation. Error details: " + e.getMessage(), Ansi.Color.RED);
                }
                break;
            case 2:
                reservationManager.listReservations();
                break;
            case 3:
                exit();
                break;
            default:
                showMessage("Invalid option. Please choose again.", Ansi.Color.RED);
                displayReservationMenu();
                break;
        }
    }

    static void exit() {
        showMessage("Goodbye!", Ansi.Color.GREEN);
        System.exit(0);
    }

    public static int getIntInput() {
        try {
            int input = scanner.nextInt();
            scanner.nextLine(); // Consume the newline character
            return input;
        } catch (Exception e) {
            showMessage("Invalid input. Please enter a valid number.", Ansi.Color.RED);
            scanner.nextLine();
            return getIntInput();
        }
    }

}