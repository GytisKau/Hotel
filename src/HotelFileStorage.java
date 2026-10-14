import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

public final class HotelFileStorage {
    private static final String FILE_HEADER = "HOTEL";
    private static final int FILE_VERSION = 1;

    private final Path file;

    public HotelFileStorage(Path file) {
        this.file = file.toAbsolutePath();
    }

    public void save(Hotel hotel) throws IOException {
        Path temporary = Files.createTempFile(
                file.getParent(), "hotel-", ".tmp"
        );

        try {
            writeHotel(hotel, temporary);
            replaceSaveFile(temporary);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    public Hotel load() throws IOException {
        try (DataInputStream input = new DataInputStream(
                new BufferedInputStream(Files.newInputStream(file))
        )) {
            if (!FILE_HEADER.equals(input.readUTF())) {
                throw new IOException("This is not a hotel save file.");
            }

            int version = input.readInt();

            if (version != FILE_VERSION) {
                throw new IOException(
                        "Unsupported save file version: " + version
                );
            }

            int roomCount = input.readInt();

            BigDecimal standardPrice = new BigDecimal(input.readUTF());
            Hotel hotel = new Hotel(roomCount, standardPrice);

            for (Room room : hotel.getRooms()) {
                restoreRoom(input, room);
            }

            if (input.read() != -1) {
                throw new IOException(
                        "Unexpected data in the save file."
                );
            }

            return hotel;
        } catch (IllegalArgumentException | ArithmeticException exception) {
            throw new IOException(
                    "The save file contains invalid hotel data.",
                    exception
            );
        }
    }

    private void writeHotel(Hotel hotel, Path destination)
            throws IOException {
        try (DataOutputStream output = new DataOutputStream(
                new BufferedOutputStream(
                        Files.newOutputStream(destination)
                )
        )) {
            List<Room> rooms = hotel.getRooms();

            output.writeUTF(FILE_HEADER);
            output.writeInt(FILE_VERSION);
            output.writeInt(rooms.size());

            output.writeUTF(
                    rooms.getFirst().getPrice().toPlainString()
            );

            for (Room room : rooms) {
                List<Guest> history = room.getHistory();

                output.writeInt(history.size());

                for (Guest guest : history) {
                    output.writeUTF(guest.name());
                    output.writeUTF(guest.surname());
                }

                output.writeBoolean(!room.isFree());
            }
        }
    }

    private void restoreRoom(DataInputStream input, Room room) throws IOException {
        int historySize = input.readInt();

        if (historySize < 0) {
            throw new IOException("Invalid guest history size.");
        }

        for (int i = 0; i < historySize; i++) {
            Guest guest = new Guest(
                    input.readUTF(),
                    input.readUTF()
            );

            room.checkIn(guest);

            if (i < historySize - 1) {
                room.checkOut();
            }
        }

        boolean occupied = input.readBoolean();

        if (occupied && historySize == 0) {
            throw new IOException(
                    "An occupied room must have a guest."
            );
        }

        if (!occupied) {
            room.checkOut();
        }
    }

    private void replaceSaveFile(Path temporary) throws IOException {
        try {
            Files.move(
                    temporary,
                    file,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
            );
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(
                    temporary,
                    file,
                    StandardCopyOption.REPLACE_EXISTING
            );
        }
    }
}