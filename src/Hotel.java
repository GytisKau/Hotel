import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

class Hotel {
    private final List<Room> rooms = new ArrayList<>();

    public Hotel(int roomCount, BigDecimal standardPrice) {
        if (roomCount <= 0 || roomCount % 2 != 0) {
            throw new IllegalArgumentException("Room count must be a positive even number.");
        }

        for (int number = 1; number <= roomCount; number++) {
            if (number <= roomCount / 2) {
                rooms.add(new Room(number, standardPrice));
            } else {
                rooms.add(new BusinessRoom(number, standardPrice));
            }
        }
    }

    public Optional<Room> register(Guest guest) {
        for (Room room : rooms) {
            if (room.checkIn(guest)) {
                return Optional.of(room);
            }
        }

        return Optional.empty();
    }

    public Optional<Room> findRoom(int number) {
        return rooms.stream()
                .filter(room -> room.getNumber() == number)
                .findFirst();
    }

    public List<Room> getRooms() {
        return List.copyOf(rooms);
    }

    public List<Room> getRoomsByOccupancy() {
        return rooms.stream()
                .sorted(
                        Comparator.comparingInt(Room::getOccupancyCount)
                                .reversed()
                                .thenComparingInt(Room::getNumber)
                )
                .toList();
    }
}
