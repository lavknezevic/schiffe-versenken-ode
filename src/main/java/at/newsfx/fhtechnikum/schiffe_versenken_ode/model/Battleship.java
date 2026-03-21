package at.newsfx.fhtechnikum.schiffe_versenken_ode.model;

public class Battleship extends Ship {

    public Battleship() {
        super("Battleship");
    }

    public Battleship(int startRow, int startCol, boolean horizontal) {
        super("Battleship", startRow, startCol, horizontal);
    }

    @Override
    public int getLength() {
        return 4;
    }
}
