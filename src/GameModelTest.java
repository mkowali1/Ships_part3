import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
}
