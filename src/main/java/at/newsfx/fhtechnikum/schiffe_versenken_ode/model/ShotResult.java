package at.newsfx.fhtechnikum.schiffe_versenken_ode.model;

public class ShotResult {

    private final boolean hit;
    private final boolean sunk;
    private final String shipName;

    public ShotResult(boolean hit, boolean sunk, String shipName) {
        this.hit = hit;
        this.sunk = sunk;
        this.shipName = shipName;
    }

    public boolean isHit() {
        return hit;
    }

    public boolean isSunk() {
        return sunk;
    }

    public String getShipName() {
        return shipName;
    }

    @Override
    public String toString() {
        if (sunk) {
            return "SUNK:" + shipName;
        }
        return hit ? "HIT" : "MISS";
    }
}
