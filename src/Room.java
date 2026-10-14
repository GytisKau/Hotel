import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class Room {
    private final int number;
    private final BigDecimal standardPrice;
    private final List<Guest> history = new ArrayList<>();

    private Guest currentGuest;
    private BigDecimal revenue = new BigDecimal("0.00");

    public Room(int number, BigDecimal standardPrice) {
        if (number <= 0) {
            throw new IllegalArgumentException("Room number must be positive.");
        }

        if (standardPrice == null || standardPrice.signum() <= 0) {
            throw new IllegalArgumentException("Room price must be positive.");
        }

        this.standardPrice = standardPrice.setScale(2, RoundingMode.UNNECESSARY);
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    public BigDecimal getPrice() {
        return standardPrice;
    }

    public String getRoomClass() {
        return "Standard";
    }

    public boolean isFree() {
        return currentGuest == null;
    }

    public Optional<Guest> getCurrentGuest() {
        return Optional.ofNullable(currentGuest);
    }

    public boolean checkIn(Guest guest) {
        if (guest == null) {
            throw new IllegalArgumentException("Guest must not be null.");
        }

        if (!isFree()) {
            return false;
        }

        currentGuest = guest;
        history.add(guest);
        revenue = revenue.add(getPrice());

        return true;
    }

    public Optional<Guest> checkOut() {
        Guest departingGuest = currentGuest;
        currentGuest = null;
        return Optional.ofNullable(departingGuest);
    }

    public List<Guest> getHistory() {
        return List.copyOf(history);
    }

    public int getOccupancyCount() {
        return history.size();
    }

    public BigDecimal getRevenue() {
        return revenue;
    }
}
