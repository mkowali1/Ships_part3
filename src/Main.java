import java.util.logging.Level;
import java.util.logging.Logger;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {

        Logger.getLogger("").setLevel(Level.FINE);
        Logger.getLogger("").getHandlers()[0].setLevel(Level.FINE);
        
        GameModel model = new GameModel();
        GameView view = new GUIGameView(); //when you want GUI interface
//        GameView view = new CLIGameView(); //when you want CLI interface
        GameController controller = new GameController(model, view);
        controller.startGame();


    }
}