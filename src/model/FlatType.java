package model;

/**
* Represents a flat type available in a BTO project.
* 
* Stores unit availability, price, and flat category (e.g., "2-Room", "3-Room").
* Provides utility methods to manage bookings and cancellations.
* 
* Used by the {@code Project} class to represent flat distributions.
* 
* @author Javier
* @version 1.0
*/
public class FlatType {
    private String type; // "2-Room" or "3-Room"
    private int totalUnits;
    private int remainingUnits;
    private int price;

    /**
    * Constructs a flat type with its configuration.
    *
    * @param type The name of the flat type (e.g., "2-Room", "3-Room").
    * @param totalUnits The total number of units available.
    * @param price The price of each unit.
    */
    public FlatType(String type, int totalUnits, int price) {
        this.type = type;
        this.totalUnits = totalUnits;
        this.price = price;
        this.remainingUnits = totalUnits;
    }

    /**
    * Gets the flat type name.
    *
    * @return The type of flat (e.g., "2-Room").
    */
    public String getType() {
        return type;
    }

    /**
    * Gets the total number of units for this flat type.
    *
    * @return The total unit count.
    */
    public int getTotalUnits() {
        return totalUnits;
    }

    /**
    * Sets the total number of units.
    *
    * @param totalUnits New total unit count.
    */
    public void setTotalUnits(int totalUnits) {
        this.totalUnits = totalUnits;
    }

    /**
    * Gets the number of units still available.
    *
    * @return The number of remaining units.
    */
    public int getRemainingUnits() {
        return remainingUnits;
    }

    /**
    * Updates the number of available units.
    *
    * @param remainingUnits New count of remaining units.
    */
    public void setRemainingUnits(int remainingUnits) {
        this.remainingUnits = remainingUnits;
    }

    /**
    * Gets the price of a single unit.
    *
    * @return Unit price.
    */
    public int getPrice() {
        return price;
    }

    /**
    * Sets the price of the flat type.
    *
    * @param price The new price.
    */
    public void setPrice(int price) {
        this.price = price;
    }

    /**
    * Checks if there are available units for booking.
    *
    * @return True if remaining units > 0.
    */
    public boolean isAvailable() {
        return remainingUnits > 0;
    }

    /**
    * Books one unit of this flat type.
    * Decrements remaining units if available.
    */
    public void bookUnit() {
        if (remainingUnits > 0) {
            remainingUnits--;
        }
    }

    /**
    * Cancels one booking, returns available units.
    */
    public void cancelBooking() {
        remainingUnits++;
    }
}
