import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GameModelTest {
    private GameModel model;

    @BeforeEach
    public void setUp() {
        model = new GameModel();
        model.initializeGame(); // Sets up a new game with randomly placed ships
    }

    @Test
    public void testInitialGridIsEmpty() {
        // Test that grid starts with ships but no hits or misses
        int[][] grid = model.getGrid();
        int shipSquares = 0;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                assertTrue(grid[i][j] == 0 || grid[i][j] == 1, "Grid should only contain water (0) or ships (1) initially");
                if (grid[i][j] == 1) shipSquares++;
            }
        }
        assertEquals(16, shipSquares, "Should have 16 ship squares (5+4+3+2+2)");
    }

    @Test
    public void testProcessGuessHit() {
        // Find a ship position to test a hit
        int[][] grid = model.getGrid();
        String hitGuess = null;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (grid[i][j] == 1) {
                    hitGuess = "" + (char)('A' + i) + (j + 1);
                    break;
                }
            }
            if (hitGuess != null) break;
        }

        assertNotNull(hitGuess, "Should find at least one ship position");
        boolean result = model.processGuess(hitGuess);
        assertTrue(result, "Processing a ship position should return true (hit)");
        assertEquals(2, grid[hitGuess.charAt(0) - 'A'][Integer.parseInt(hitGuess.substring(1)) - 1],
                "Grid should mark position as hit (2)");
    }

    @Test
    public void testGameOver() {
        // Simulate sinking all ships
        int[][] grid = model.getGrid();
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (grid[i][j] == 1) {
                    String guess = "" + (char)('A' + i) + (j + 1);
                    model.processGuess(guess);
                }
            }
        }

        assertTrue(model.isGameOver(), "Game should be over when all ships are sunk");
    }

    @Test
    public void testLoadShipsFromFile() {
        String testFile = "test_ships.txt";
        try (FileWriter writer = new FileWriter(testFile)) {
            writer.write("A1,5,H\n");
            writer.write("C3,4,V\n");
            writer.write("E5,3,H\n");
            writer.write("G7,3,V\n");
            writer.write("I9,2,H\n");
        } catch (IOException e) {
            fail("Failed to create test file: " + e.getMessage());
        }

        GameModel model = new GameModel();
        model.loadShipsFromFile(testFile);

        int[][] grid = model.getGrid();
        List<Ship> ships = model.getShips();

        assertEquals(5, ships.size(), "Should load exactly 5 ships");
        assertEquals(1, grid[0][0], "Ship at A1 should be present");
        assertEquals(1, grid[0][4], "Ship at A1 (length 5, horizontal) should extend to A5");
        assertEquals(1, grid[2][2], "Ship at C3 (vertical) should be present");
        assertEquals(1, grid[5][2], "Ship at C3 (length 4, vertical) should extend to F3");
    }
}
