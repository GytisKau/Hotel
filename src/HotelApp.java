import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

public class HotelApp {
    private static final int ROOM_COUNT = 6;
    private static final BigDecimal STANDARD_PRICE = new BigDecimal("20.00");
    private static final HotelFileStorage STORAGE = new HotelFileStorage(Path.of("hotel-data.bin"));

    public static void main(String[] args) {
        Hotel hotel;

        try {
            hotel = STORAGE.load();
            System.out.println("Saved hotel data loaded.");
        } catch (NoSuchFileException exception) {
            hotel = new Hotel(ROOM_COUNT, STANDARD_PRICE);
            System.out.println("Starting a new hotel.");
        } catch (IOException exception) {
            System.err.println(
                    "Cannot load hotel data: " + exception.getMessage()
            );
            return;
        }

        try (Scanner scanner = new Scanner(System.in)) {
            boolean running = true;

            while (running) {
                System.out.println("""
                        
                        === Hotel registration ===
                        1. Register guest
                        2. Check out guest
                        3. Review room availability
                        4. View room history and status
                        5. View occupancy report
                        0. Exit
                        """);

                int choice = readInt(scanner, "Choose an action: ");

                switch (choice) {
                    case 1 -> {
                        registerGuest(scanner, hotel);
                        saveHotel(hotel);
                    }
                    case 2 -> {
                        checkOutGuest(scanner, hotel);
                        saveHotel(hotel);
                    }
                    case 3 -> showAvailability(hotel);
                    case 4 -> showHistory(scanner, hotel);
                    case 5 -> showReport(hotel);
                    case 0 -> {
                        saveHotel(hotel);
                        running = false;
                    }
                    default -> System.out.println("Choose a number from 0 to 5.");
                }
            }
        }

        System.out.println("Goodbye!");
    }

    private static void saveHotel(Hotel hotel) {
        try {
            STORAGE.save(hotel);
        } catch (IOException exception) {
            System.err.println(
                    "Could not save hotel data: " + exception.getMessage()
            );
            System.err.println(
                    "Recent changes exist only in memory."
            );
        }
    }

    private static void registerGuest(Scanner scanner, Hotel hotel) {
        String name = readRequiredText(scanner, "Guest name: ");
        String surname = readRequiredText(scanner, "Guest surname: ");

        Guest guest = new Guest(name, surname);
        Optional<Room> assignedRoom = hotel.register(guest);

        if (assignedRoom.isEmpty()) {
            System.out.println("Registration failed: no rooms are available.");
            return;
        }

        Room room = assignedRoom.orElseThrow();

        System.out.printf(
                "Registration successful: %s, room %d (%s), charge %s EUR.%n",
                guest.fullName(),
                room.getNumber(),
                room.getRoomClass(),
                room.getPrice().toPlainString()
        );
    }

    private static void checkOutGuest(Scanner scanner, Hotel hotel) {
        Room room = selectRoom(scanner, hotel);

        if (room == null) {
            return;
        }

        Optional<Guest> departingGuest = room.checkOut();

        if (departingGuest.isEmpty()) {
            System.out.println("Checkout failed: that room is already free.");
            return;
        }

        System.out.printf(
                "%s checked out of room %d.%n",
                departingGuest.orElseThrow().fullName(),
                room.getNumber()
        );
    }

    private static void showAvailability(Hotel hotel) {
        System.out.println("\nRoom availability:");

        for (Room room : hotel.getRooms()) {
            String status = room.getCurrentGuest()
                    .map(guest -> "Occupied by " + guest.fullName())
                    .orElse("Free");

            System.out.printf("Room %d | %s | %s EUR | %s%n",
                    room.getNumber(), room.getRoomClass(),
                    room.getPrice().toPlainString(), status
            );
        }
    }

    private static void showHistory(Scanner scanner, Hotel hotel) {
        Room room = selectRoom(scanner, hotel);

        if (room == null) {
            return;
        }

        System.out.printf("%nRoom %d | Status: %s%n",
            room.getNumber(), room.isFree() ? "Free" : "Occupied"
        );

        List<Guest> history = room.getHistory();

        if (history.isEmpty()) {
            System.out.println("This room has no occupancy history.");
            return;
        }

        System.out.println("Guests in registration order:");

        for (int i = 0; i < history.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, history.get(i).fullName());
        }
    }

    private static void showReport(Hotel hotel) {
        System.out.println("\nOccupancy report:");
        System.out.printf("%-8s %-12s %-12s %s%n",
                "Room", "Class", "Occupancies", "Revenue (EUR)"
        );

        for (Room room : hotel.getRoomsByOccupancy()) {
            System.out.printf("%-8d %-12s %-12d %s%n",
                    room.getNumber(), room.getRoomClass(),
                    room.getOccupancyCount(),
                    room.getRevenue().toPlainString()
            );
        }
    }

    private static Room selectRoom(Scanner scanner, Hotel hotel) {
        int number = readInt(scanner, "Room number: ");
        Optional<Room> room = hotel.findRoom(number);

        if (room.isEmpty()) {
            System.out.println("No room exists with that number.");
            return null;
        }

        return room.orElseThrow();
    }

    private static int readInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().strip();

            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException exception) {
                System.out.println("Please enter a whole number.");
            }
        }
    }

    private static String readRequiredText(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().strip();

            if (!input.isBlank()) {
                return input;
            }

            System.out.println("This value cannot be empty.");
        }
    }
}

