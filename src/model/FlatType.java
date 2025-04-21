package model;

public class FlatType {
    private String type; // "2-Room" or "3-Room"
    private int totalUnits;
    private int remainingUnits;
    private int price;

    public FlatType(String type, int totalUnits, int price) {
        this.type = type;
        this.totalUnits = totalUnits;
        this.price = price;
        this.remainingUnits = totalUnits;
    }

    public String getType() {
        return type;
    }

    public int getTotalUnits() {
        return totalUnits;
    }

    public void setTotalUnits(int totalUnits) {
        this.totalUnits = totalUnits;
    }

    public int getRemainingUnits() {
        return remainingUnits;
    }

    public void setRemainingUnits(int remainingUnits) {
        this.remainingUnits = remainingUnits;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public boolean isAvailable() {
        return remainingUnits > 0;
    }

    public void bookUnit() {
        if (remainingUnits > 0) {
            remainingUnits--;
        }
    }

    public void cancelBooking() {
        remainingUnits++;
    }
}
