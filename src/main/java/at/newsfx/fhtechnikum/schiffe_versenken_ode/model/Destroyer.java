package at.newsfx.fhtechnikum.schiffe_versenken_ode.model;

public class Destroyer extends Ship {

    public Destroyer() {
        super("Destroyer");
    }

    public Destroyer(int startRow, int startCol, boolean horizontal) {
        super("Destroyer", startRow, startCol, horizontal);
    }

    @Override
    public int getLength() {
        return 2;
    }
}
