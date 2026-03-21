package at.newsfx.fhtechnikum.schiffe_versenken_ode.model;

public class Submarine extends Ship {

    public Submarine() {
        super("Submarine");
    }

    public Submarine(int startRow, int startCol, boolean horizontal) {
        super("Submarine", startRow, startCol, horizontal);
    }

    @Override
    public int getLength() {
        return 3;
    }
}
