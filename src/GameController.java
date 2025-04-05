import java.util.Observer;

public class GameController {
    private GameModel model;
    private GameView view;

    public GameController(GameModel model, GameView view) {
        this.model = model;
        this.view = view;
        if (view instanceof Observer) {
            model.addObserver((Observer) view);
        }
    }

    public void startGame() {
//        model.initializeGame(); //when you want ships be placed randomly
        model.loadShipsFromFile("ships.txt"); //when you want ships be placed from file
        view.showMessage("Welcome to Ships! Guess a coordinate.");
        playGame();
    }

    private void playGame() {
        while (!model.isGameOver()) {
            String input = view.getInput();
            if (input == null || input.equalsIgnoreCase("quit")) {
                view.showMessage("Game ended by player.");
                break;
            }
            boolean hit = model.processGuess(input);
            if (hit) {
                view.showMessage("Hit at " + input + "!");

            } else {
                view.showMessage("Miss at " + input + ".");
            }
        }
        if (model.isGameOver()) {
            view.showMessage("Game Over! You sank all ships in " + model.getShotsNumber() + " shots");
        }
    }

    public void handleInput(String input) {
        model.processGuess(input);
    }

}
