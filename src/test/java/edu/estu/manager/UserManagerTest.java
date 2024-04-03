package edu.estu.manager;

import com.google.gson.Gson;
import edu.estu.CinemaReservationCLI;
import edu.estu.model.User;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.skyscreamer.jsonassert.JSONAssert;

import static org.junit.jupiter.api.Assertions.*;

class UserManagerTest {

    private UserManager userManager;
    private Gson gson;

    @BeforeEach
    void setUp() {
        gson = new Gson();
        userManager = new UserManager(new Scanner(System.in), gson);
    }

    @Test
    void testReadUsersFromJSON() throws IOException {
        // Create a temporary users.json file with sample data
        String jsonContent = "[{\"username\":\"user1\",\"password\":\"pass1\"},{\"username\":\"user2\",\"password\":\"pass2\"}]";
        Files.writeString(Path.of("temp_users.json"), jsonContent);

        // Set the USERS_JSON path to the temporary file
        UserManager.USERS_JSON = "temp_users.json";

        // Test reading users from JSON
        List<User> users = userManager.readUsersFromJSON();

        // Assert that the users list is not null and contains the expected number of users
        assertNotNull(users);
        assertEquals(2, users.size());

        // Clean up: delete the temporary file
        Files.deleteIfExists(Path.of("temp_users.json"));
    }

    @Test
    void testWriteUsersToJSON() throws IOException, JSONException {
        // Create a temporary users list
        List<User> users = List.of(new User("user1", "pass1", new ArrayList<>()), new User("user2", "pass2", new ArrayList<>()));

        // Write users to JSON
        userManager.writeUsersToJSON(users);

        // Read the content of the written JSON file
        String jsonContent = Files.readString(Path.of(UserManager.USERS_JSON));

        // Update the expectations to include the "reservations" field
        String expectedJson = gson.toJson(users);

        // Assert that the content matches the expected JSON using JSONAssert
        JSONAssert.assertEquals(expectedJson, jsonContent, false);
    }
}
