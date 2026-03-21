package at.newsfx.fhtechnikum.schiffe_versenken_ode.model;

public class Cruiser extends Ship {

    public Cruiser() {
        super("Cruiser");
    }

    public Cruiser(int startRow, int startCol, boolean horizontal) {
        super("Cruiser", startRow, startCol, horizontal);
    }

    @Override
    public int getLength() {
        return 3;
    }
}
