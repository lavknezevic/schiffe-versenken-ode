package at.newsfx.fhtechnikum.schiffe_versenken_ode.model;

public abstract class Ship {

    private final String name;
    private int startRow;
    private int startCol;
    private boolean horizontal;
    private int hits;

    protected Ship(String name) {
        this.name = name;
        this.hits = 0;
    }

    protected Ship(String name, int startRow, int startCol, boolean horizontal) {
        this.name = name;
        this.startRow = startRow;
        this.startCol = startCol;
        this.horizontal = horizontal;
        this.hits = 0;
    }

    public abstract int getLength();

    public boolean isSunk() {
        return hits >= getLength();
    }

    public void hit() {
        if (!isSunk()) {
            hits++;
        }
    }

    public boolean occupies(int row, int col) {
        for (int i = 0; i < getLength(); i++) {
            int r = horizontal ? startRow : startRow + i;
            int c = horizontal ? startCol + i : startCol;
            if (r == row && c == col) {
                return true;
            }
        }
        return false;
    }

    public String getName() {
        return name;
    }

    public int getStartRow() {
        return startRow;
    }

    public int getStartCol() {
        return startCol;
    }

    public boolean isHorizontal() {
        return horizontal;
    }

    public void setPosition(int startRow, int startCol, boolean horizontal) {
        this.startRow = startRow;
        this.startCol = startCol;
        this.horizontal = horizontal;
    }

    public int getHits() {
        return hits;
    }

    @Override
    public String toString() {
        return name + " (" + getLength() + ")";
    }
}
