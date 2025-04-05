import java.util.Observable;
import java.util.Observer;
import java.util.Scanner;
import java.util.logging.Logger;

public class CLIGameView implements GameView, Observer {

    private static final Logger LOGGER = Logger.getLogger(CLIGameView.class.getName());
    private Scanner scanner;

    public CLIGameView() {
        scanner = new Scanner(System.in);
    }

    @Override
    public void displayGrid(int[][] grid) {
        if (grid == null || grid.length != 10 || grid[0].length != 10) {
            throw new IllegalArgumentException("Grid must be 10x10");
        }
        System.out.println("  1 2 3 4 5 6 7 8 9 10");
        for (int row = 0; row < 10; row++) {
            System.out.print((char)('A' + row) + " ");
            for (int col = 0; col < 10; col++) {
                if (grid[row][col] == 0 || grid[row][col] == 1) {
                    System.out.print("~ "); // Water or hidden ship
                } else if (grid[row][col] == 2) {
                    System.out.print("X "); // Hit
                } else if (grid[row][col] == 3) {
                    System.out.print("O "); // Miss
                }else if (grid[row][col] == 4) {
                    System.out.print("S "); // Ship - sunk
                }
            }
            System.out.println();
        }
    }

    @Override
    public void showMessage(String msg) {
        if (msg == null) msg = "";
        System.out.println(msg);
    }

    @Override
    public String getInput() {
        System.out.print("Enter your guess ");
        String input = scanner.nextLine().trim().toUpperCase();
        if (input.matches("[A-J](10|[1-9])")) {
            return input;
        } else {
            showMessage("Invalid input!");
            return getInput();
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        System.out.println("[Observer] Update triggered by " + o.getClass().getSimpleName());
        LOGGER.fine("CLIGameView received update");

//        if (!(o instanceof GameModel)) { //Uncomment if you want to see grid in command line
//            LOGGER.warning("Received update from unexpected observable: " + o.getClass().getName());
//            throw new IllegalStateException("Observer expected GameModel, got " + o.getClass().getName());
//        }
//        GameModel model = (GameModel) o;
//        int[][] grid = (arg != null) ? (int[][]) arg : model.getGrid();
//        displayGrid(grid);
    }
}
