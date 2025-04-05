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

    public GameModel() {
        grid = new int[GRID_SIZE][GRID_SIZE];
        ships = new ArrayList<>();
        hits = 0;
        misses = 0;
    }

    void initializeGame(){
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                grid[i][j] = 0;
            }
        }
        ships.clear();
        hits = 0;
        misses = 0;
        placeShipsRandomly();
        notifyModelChanged();
    }

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
        notifyModelChanged();
    }

    private boolean canPlaceShip(int row, int col, int length, boolean horizontal) {
        if(row < 0 || col < 0 || length <= 0){
            return false;
        }
        if (horizontal) {
            if (col + length > GRID_SIZE) return false;
            for (int c = col; c < col + length; c++) {
                if (grid[row][c] != 0) return false;
            }
        } else {
            if (row + length > GRID_SIZE) return false;
            for (int r = row; r < row + length; r++) {
                if (grid[r][col] != 0) return false;
            }
        }
        return true;
    }

    private void placeShip(int row, int col, int length, boolean horizontal) {
        if(row < 0 || col < 0 || row >= GRID_SIZE || col >= GRID_SIZE || length <= 0){
            throw new IllegalArgumentException("Invalid ship placement parameters");
        }
        Ship ship = new Ship(row, col, length, horizontal);
        ships.add(ship);
        if (horizontal) {
            for (int c = col; c < col + length; c++) {
                grid[row][c] = 1;
            }
        } else {
            for (int r = row; r < row + length; r++) {
                grid[r][col] = 1;
            }
        }
    }

    public void loadShipsFromFile(String file){
        if (file == null || file.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                grid[i][j] = 0;
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
        notifyModelChanged();
    }

    private void markSunkShip(Ship ship) {
        if (ship == null) throw new IllegalArgumentException("Ship cannot be null");
        int row = ship.getStartRow();
        int col = ship.getStartCol();
        int length = ship.getLength();
        boolean horizontal = ship.isHorizontal();
        if (horizontal) {
            for (int c = col; c < col + length; c++) {
                if (grid[row][c] == 2) {
                    grid[row][c] = 4;
                }
            }
        } else {
            for (int r = row; r < row + length; r++) {
                if (grid[r][col] == 2) {
                    grid[r][col] = 4;
                }
            }
        }
    }

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
        if (grid[row][col] == 1) {
            grid[row][col] = 2;
            hits++;
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
            grid[row][col] = 3;
            misses++;
        }
        notifyModelChanged();
        return hit;
    }

    public boolean isGameOver() {
        return hits == ships.stream().mapToInt(Ship::getLength).sum();
    }

    int[][] getGrid(){
        return grid;
    }

    public int getShotsNumber(){
        return (hits + misses);
    }

    private void notifyModelChanged(){
        setChanged();
        notifyObservers(getGrid());
    }
}
