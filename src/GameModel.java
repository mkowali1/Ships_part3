import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Random;

public class GameModel extends Observable {

    private int[][] grid;
    private List<Ship> ships;
    private int hits;
    private int misses;
    private static final int GRID_SIZE = 10;
    private static final int[] SHIP_LENGTHS = {5, 4, 3, 2, 2};
    public static final int WATER = 0, SHIP = 1, HIT = 2, MISS = 3, SUNK = 4;
    private static final int TOTAL_SHIP_SQUARES = 17;

    public GameModel() {
        grid = new int[GRID_SIZE][GRID_SIZE];
        ships = new ArrayList<>();
        hits = 0;
        misses = 0;
    }

    /**
     * Initializes a new game by clearing the grid and placing ships randomly.
     */
    void initializeGame(){
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                grid[i][j] = WATER;
            }
        }
        ships.clear();
        hits = 0;
        misses = 0;
        placeShipsRandomly();
        checkInvariants();
        notifyModelChanged();
    }

    /**
     * Places ships randomly on the grid without overlap.
     */
    void placeShipsRandomly(){
        Random rand = new Random();
        for (int shipLength : SHIP_LENGTHS) {
            boolean placed = false;
            while (!placed) {
                int row = rand.nextInt(GRID_SIZE);
                int col = rand.nextInt(GRID_SIZE);
                boolean horizontal = rand.nextBoolean();
                if (canPlaceShip(row, col, shipLength, horizontal)) {
                    placeShip(row, col, shipLength, horizontal);
                    placed = true;
                }
            }
        }
        checkInvariants();
        notifyModelChanged();
    }

    private boolean canPlaceShip(int row, int col, int length, boolean horizontal) {
        if(row < 0 || col < 0 || length <= 0){
            return false;
        }
        if (horizontal) {
            if (col + length > GRID_SIZE) return false;
            for (int c = col; c < col + length; c++) {
                if (grid[row][c] != WATER) return false;
            }
        } else {
            if (row + length > GRID_SIZE) return false;
            for (int r = row; r < row + length; r++) {
                if (grid[r][col] != WATER) return false;
            }
        }
        return true;
    }

    private void placeShip(int row, int col, int length, boolean horizontal) {
        assert canPlaceShip(row, col, length, horizontal) : "Invalid ship placement";
        if(row < 0 || col < 0 || row >= GRID_SIZE || col >= GRID_SIZE || length <= 0){
            throw new IllegalArgumentException("Invalid ship placement parameters");
        }
        Ship ship = new Ship(row, col, length, horizontal);
        ships.add(ship);
        if (horizontal) {
            for (int c = col; c < col + length; c++) {
                grid[row][c] = SHIP;
            }
        } else {
            for (int r = row; r < row + length; r++) {
                grid[r][col] = SHIP;
            }
        }
    }

    /**
     * Loads ship positions from a file and places them on the grid.
     * @param file Path to the ship configuration file
     * @throws IllegalArgumentException if file format or ship placement is invalid
     * @throws RuntimeException if file reading fails
     */
    public void loadShipsFromFile(String file){
        if (file == null || file.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                grid[i][j] = WATER;
            }
        }
        ships.clear();
        hits = 0;
        misses = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                System.out.println(line);
                if (line.isEmpty()) continue;

                String[] parts = line.split(",");
                if (parts.length != 3) {
                    throw new IllegalArgumentException("Invalid ship format in file: " + line);
                }

                String position = parts[0].trim().toUpperCase();
                if (!position.matches("[A-J](10|[1-9])")) {
                    throw new IllegalArgumentException("Invalid position: " + position);
                }
                int row = position.charAt(0) - 'A';
                int col = Integer.parseInt(position.substring(1)) - 1;

                int length = Integer.parseInt(parts[1].trim());
                if (length < 2 || length > 5) {
                    throw new IllegalArgumentException("Invalid ship length: " + length);
                }

                String orientation = parts[2].trim().toUpperCase();
                boolean horizontal = orientation.equals("H");
                if (!horizontal && !orientation.equals("V")) {
                    throw new IllegalArgumentException("Invalid orientation: " + orientation);
                }

                if (!canPlaceShip(row, col, length, horizontal)) {
                    throw new IllegalArgumentException("Cannot place ship at " + position + ": out of bounds or overlap");
                }
                placeShip(row, col, length, horizontal);
            }

            if (ships.size() != SHIP_LENGTHS.length) {
                throw new IllegalArgumentException("File must contain exactly " + SHIP_LENGTHS.length + " ships");
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading ship configuration file: " + file, e);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format in file: " + file, e);
        }
        checkInvariants();
        notifyModelChanged();
    }

    /**
     * Marks a sunk ship's positions on the grid as sunk.
     * @param ship The ship to mark as sunk
     * @throws IllegalArgumentException if ship is null
     */
    private void markSunkShip(Ship ship) {
        if (ship == null) throw new IllegalArgumentException("Ship cannot be null");
        int row = ship.getStartRow();
        int col = ship.getStartCol();
        int length = ship.getLength();
        boolean horizontal = ship.isHorizontal();
        if (horizontal) {
            for (int c = col; c < col + length; c++) {
                if (grid[row][c] == HIT) {
                    grid[row][c] = SUNK;
                }
            }
        } else {
            for (int r = row; r < row + length; r++) {
                if (grid[r][col] == HIT) {
                    grid[r][col] = SUNK;
                }
            }
        }
    }

    /**
     * Processes a player's guess and updates the game state.
     * @param guess Coordinate (e.g., "A1") to guess
     * @return true if the guess hits a ship, false otherwise
     */
    public boolean processGuess(String guess){
        if (guess == null || !guess.matches("[A-J](10|[1-9])")) {
            return false;
        }
        int row = guess.charAt(0) - 'A';
        int col = Integer.parseInt(guess.substring(1)) - 1;
        if (row < 0 || row >= GRID_SIZE || col < 0 || col >= GRID_SIZE || grid[row][col] > 1) {
            return false;
        }
        boolean hit = false;
        if (grid[row][col] == SHIP) {
            grid[row][col] = HIT;
            hits++;
            assert hits >= 0 : "Hits cannot be negative";
            for (Ship ship : ships) {
                if (ship.isHit(row, col)) {
                    if (ship.isSunk()) {
                        markSunkShip(ship);
                    }
                    hit = true;
                    break;
                }
            }
        } else {
            grid[row][col] = MISS;
            misses++;
        }
        checkInvariants();
        notifyModelChanged();
        return hit;
    }

    /**
     * Checks if the game is over (all ships sunk).
     * @return true if all ships are sunk, false otherwise
     */
    public boolean isGameOver() {
        assert hits >= 0 : "Hits cannot be negative";
        return hits == ships.stream().mapToInt(Ship::getLength).sum();
    }

    /**
     * Gets the current game grid.
     * @return The 10x10 grid array
     */
    int[][] getGrid(){
        checkInvariants();
        return grid;
    }

    /**
     * Gets a copy of the ship list.
     * @return List of ships
     */
    public List<Ship> getShips(){
        checkInvariants();
        return ships;
    }

    public int getShotsNumber(){
        return (hits + misses);
    }

    /**
     * Notifies observers with the current grid state.
     */
    private void notifyModelChanged(){
        setChanged();
        notifyObservers(getGrid());
    }

    private void checkInvariants() {
        assert hits >= 0 : "Hits cannot be negative: " + hits;
        assert misses >= 0 : "Misses cannot be negative: " + misses;

        assert hits <= TOTAL_SHIP_SQUARES : "Hits (" + hits + ") cannot exceed total ship squares (" + TOTAL_SHIP_SQUARES + ")";

        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                assert grid[i][j] >= 0 && grid[i][j] <= 4 : "Invalid grid value at [" + i + "," + j + "]: " + grid[i][j];
            }
        }

        assert ships.size() <= SHIP_LENGTHS.length : "Too many ships: " + ships.size() + " (expected " + SHIP_LENGTHS.length + ")";
        int totalShipLength = ships.stream().mapToInt(Ship::getLength).sum();
        assert totalShipLength <= TOTAL_SHIP_SQUARES : "Total ship length (" + totalShipLength + ") exceeds expected (" + TOTAL_SHIP_SQUARES + ")";
    }
}
