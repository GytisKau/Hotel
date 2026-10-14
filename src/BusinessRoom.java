import java.math.BigDecimal;
import java.math.RoundingMode;

class BusinessRoom extends Room {
    private static final BigDecimal PRICE_MULTIPLIER = new BigDecimal("1.50");

    public BusinessRoom(int number, BigDecimal standardPrice) {
        super(number, standardPrice);
    }

    @Override
    public BigDecimal getPrice() {
        return super.getPrice()
                .multiply(PRICE_MULTIPLIER)
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String getRoomClass() {
        return "Business";
    }
}
