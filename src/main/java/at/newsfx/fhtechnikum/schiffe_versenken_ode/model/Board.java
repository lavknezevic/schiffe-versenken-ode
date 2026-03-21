package at.newsfx.fhtechnikum.schiffe_versenken_ode.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Board {

    public static final int SIZE = 10;

    private final CellState[][] grid;
    private final List<Ship> ships;

    public Board() {
        grid = new CellState[SIZE][SIZE];
        ships = new ArrayList<>();
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                grid[r][c] = CellState.EMPTY;
            }
        }
    }

    public boolean placeShip(Ship ship, int row, int col, boolean horizontal) {
        if (!canPlace(ship, row, col, horizontal)) {
            return false;
        }
        ship.setPosition(row, col, horizontal);
        for (int i = 0; i < ship.getLength(); i++) {
            int r = horizontal ? row : row + i;
            int c = horizontal ? col + i : col;
            grid[r][c] = CellState.SHIP;
        }
        ships.add(ship);
        return true;
    }

    public boolean canPlace(Ship ship, int row, int col, boolean horizontal) {
        for (int i = 0; i < ship.getLength(); i++) {
            int r = horizontal ? row : row + i;
            int c = horizontal ? col + i : col;
            if (r < 0 || r >= SIZE || c < 0 || c >= SIZE) {
                return false;
            }
            if (grid[r][c] != CellState.EMPTY) {
                return false;
            }
        }
        return true;
    }

    public ShotResult shootAt(int row, int col) {
        if (row < 0 || row >= SIZE || col < 0 || col >= SIZE) {
            return new ShotResult(false, false, null);
        }
        if (grid[row][col] == CellState.HIT || grid[row][col] == CellState.MISS) {
            return new ShotResult(false, false, null);
        }
        if (grid[row][col] == CellState.SHIP) {
            grid[row][col] = CellState.HIT;
            Ship hitShip = getShipAt(row, col);
            if (hitShip != null) {
                hitShip.hit();
                return new ShotResult(true, hitShip.isSunk(), hitShip.getName());
            }
            return new ShotResult(true, false, null);
        }
        grid[row][col] = CellState.MISS;
        return new ShotResult(false, false, null);
    }

    private Ship getShipAt(int row, int col) {
        for (Ship ship : ships) {
            if (ship.occupies(row, col)) {
                return ship;
            }
        }
        return null;
    }

    public boolean allShipsSunk() {
        for (Ship ship : ships) {
            if (!ship.isSunk()) {
                return false;
            }
        }
        return !ships.isEmpty();
    }

    public void placeShipsRandomly() {
        clear();
        Ship[] toPlace = {
                new Carrier(), new Battleship(), new Cruiser(),
                new Submarine(), new Destroyer()
        };
        Random random = new Random();
        for (Ship ship : toPlace) {
            boolean placed = false;
            while (!placed) {
                int row = random.nextInt(SIZE);
                int col = random.nextInt(SIZE);
                boolean horizontal = random.nextBoolean();
                placed = placeShip(ship, row, col, horizontal);
            }
        }
    }

    public void clear() {
        ships.clear();
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                grid[r][c] = CellState.EMPTY;
            }
        }
    }

    public CellState getCell(int row, int col) {
        return grid[row][col];
    }

    public List<Ship> getShips() {
        return ships;
    }

    public int getShipCount() {
        return ships.size();
    }
}
