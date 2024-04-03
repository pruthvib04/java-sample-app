package edu.estu.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import edu.estu.CinemaReservationCLI;
import edu.estu.model.User;
import org.fusesource.jansi.Ansi;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static edu.estu.CinemaReservationCLI.showMessage;

public class UserManager {

    static String USERS_JSON = "users.json";
    private final Scanner scanner;
    private final Gson gson;

    public UserManager(Scanner scanner, Gson gson) {
        this.scanner = scanner;
        this.gson = gson;
    }

    public void register() {
        System.out.print("Enter your username: ");
        String username = scanner.next();
        System.out.print("Enter your password: ");
        String password = scanner.next();

        User newUser = new User(username, password);

        try {
            List<User> users = readUsersFromJSON();
            boolean userExists = false;
            for (User user : users) {
                if (user.getUsername().equals(username)) {
                    showMessage("User already exists. Please choose another username.", Ansi.Color.RED);
                    userExists = true;
                    break;
                }
            }

            if (!userExists) {
                users.add(newUser);
                writeUsersToJSON(users);
                showMessage("User registered successfully.", Ansi.Color.GREEN);
            }
        } catch (IOException e) {
            showMessage("An error occurred while registering. Please try again. Error details: " + e.getMessage(), Ansi.Color.RED);
        }

        CinemaReservationCLI.displayMainMenu();
    }

    public void login() {
        Path usersFilePath = Paths.get(USERS_JSON);
        if (!Files.exists(usersFilePath)) {
            showMessage("Users file not found. Please register first.", Ansi.Color.RED);
            CinemaReservationCLI.displayMainMenu();
            return;
        }

        System.out.print("Enter your username: ");
        String username = scanner.next();
        System.out.print("Enter your password: ");
        String password = scanner.next();

        try {
            List<User> users = readUsersFromJSON();
            boolean loggedIn = false;
            for (User user : users) {
                if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                    showMessage("Login successful.", Ansi.Color.GREEN);
                    CinemaReservationCLI.currentUser = user;
                    CinemaReservationCLI.displayReservationMenu();
                    loggedIn = true;
                    break;
                }
            }

            if (!loggedIn) {
                showMessage("Invalid username or password.", Ansi.Color.RED);
                CinemaReservationCLI.displayMainMenu();
            }
        } catch (IOException e) {
            showMessage("An error occurred while logging in. Please try again. Error details: " + e.getMessage(), Ansi.Color.RED);
        }
    }

    List<User> readUsersFromJSON() throws IOException {
        File jsonFile = new File(USERS_JSON);
        if (!jsonFile.exists() || jsonFile.length() == 0) {
            return new ArrayList<>(); // Return an empty list if the file doesn't exist or is empty
        }

        TypeToken<List<User>> token = new TypeToken<>() {
        };
        try (FileReader reader = new FileReader(USERS_JSON)) {
            return gson.fromJson(reader, token.getType());
        } catch (IOException | JsonParseException e) {
            // In case of file reading errors or JSON parsing errors, return an empty list
            return new ArrayList<>();
        }
    }

    void writeUsersToJSON(List<User> users) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(users);
        Files.writeString(Path.of(USERS_JSON), json);
    }
}
