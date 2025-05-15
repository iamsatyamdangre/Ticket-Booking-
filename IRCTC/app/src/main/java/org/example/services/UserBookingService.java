package org.example.services;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.example.entities.Train;
import org.example.entities.User;
import org.example.util.UserServiceUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class UserBookingService {

    private final ObjectMapper objectMapper = new ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);  // Updated line

    private List<User> userList;

    private User user;

    private final String USER_FILE_PATH = "src/main/java/org/example/localDb/users.json";

    public UserBookingService(User user) throws IOException {
        this.user = user;
        loadUserListFromFile();
    }

    public UserBookingService() throws IOException {
        loadUserListFromFile();
    }

    // Load user list from the users.json file
    private void loadUserListFromFile() throws IOException {
        File file = new File(USER_FILE_PATH);
        if (!file.exists()) {

            file.getParentFile().mkdirs();
            file.createNewFile();
            userList = new ArrayList<>();
        } else {

            userList = objectMapper.readValue(file, new TypeReference<List<User>>() {});
        }
    }


    public Boolean loginUser() {
        Optional<User> foundUser = userList.stream().filter(user1 -> {
            return user1.getName().equals(user.getName()) && UserServiceUtil.checkPassword(user.getPassword(), user1.getHashedPassword());
        }).findFirst();
        return foundUser.isPresent();
    }

    // Sign up a new user
    public Boolean signUp(User user1) {
        try {
            userList.add(user1);
            saveUserListToFile();
            return Boolean.TRUE;
        } catch (IOException ex) {
            System.out.println("Error during sign-up: " + ex.getMessage());
            return Boolean.FALSE;
        }
    }

    // Save the user list back to the file
    private void saveUserListToFile() throws IOException {
        File usersFile = new File(USER_FILE_PATH);
        objectMapper.writeValue(usersFile, userList);
    }

    // Fetch bookings for the logged-in user
    public void fetchBookings() {
        Optional<User> userFetched = userList.stream().filter(user1 -> {
            return user1.getName().equals(user.getName()) && UserServiceUtil.checkPassword(user.getPassword(), user1.getHashedPassword());
        }).findFirst();
        if (userFetched.isPresent()) {
            userFetched.get().printTickets();
        }
    }

    // Cancel a booking for the logged-in user
    public Boolean cancelBooking(String ticketId) {
        // Scanner instance to get user input
        Scanner s = new Scanner(System.in);
        System.out.println("Enter the ticket id to cancel");


        String ticketIdInput = s.next();


        if (ticketIdInput == null || ticketIdInput.isEmpty()) {
            System.out.println("Ticket ID cannot be null or empty.");
            return Boolean.FALSE;
        }


        boolean removed = user.getTicketsBooked().removeIf(ticket -> ticket.getTicketId().equals(ticketIdInput));


        if (removed) {
            System.out.println("Ticket with ID " + ticketIdInput + " has been canceled.");
            return Boolean.TRUE;
        } else {
            System.out.println("No ticket found with ID " + ticketIdInput);
            return Boolean.FALSE;
        }
    }



    public List<Train> getTrains(String source, String destination) {
        try {
            TrainService trainService = new TrainService();
            return trainService.searchTrains(source, destination);
        } catch (IOException ex) {
            return new ArrayList<>();
        }
    }


    public List<List<Integer>> fetchSeats(Train train) {
        return train.getSeats();
    }


    public Boolean bookTrainSeat(Train train, int row, int seat) {
        try {
            TrainService trainService = new TrainService();
            List<List<Integer>> seats = train.getSeats();
            if (row >= 0 && row < seats.size() && seat >= 0 && seat < seats.get(row).size()) {
                if (seats.get(row).get(seat) == 0) {
                    seats.get(row).set(seat, 1);
                    train.setSeats(seats);
                    trainService.addTrain(train);
                    return true; // Booking successful
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } catch (IOException ex) {
            return Boolean.FALSE;
        }
    }
}
