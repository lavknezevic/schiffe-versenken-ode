package at.newsfx.fhtechnikum.schiffe_versenken_ode.model;

public class Carrier extends Ship {

    public Carrier() {
        super("Carrier");
    }

    public Carrier(int startRow, int startCol, boolean horizontal) {
        super("Carrier", startRow, startCol, horizontal);
    }

    @Override
    public int getLength() {
        return 5;
    }
}
