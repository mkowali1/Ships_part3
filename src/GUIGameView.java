import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Logger;

public class GUIGameView implements GameView, Observer {
    private static final Logger LOGGER = Logger.getLogger(GUIGameView.class.getName());
    private JFrame frame;
    private JButton[][] gridButtons;
    private JLabel messageLabel;
    private String lastInput;
    private boolean inputReceived;

    public GUIGameView(){
        frame = new JFrame("Ships");
        gridButtons = new JButton[10][10];
        messageLabel = new JLabel("Welcome to Ships! Click to guess.");
        lastInput = "";
        inputReceived = false;
        initializeGUI();
    }

    private void initializeGUI() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JPanel gridPanel = new JPanel(new GridLayout(10, 10));
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                gridButtons[row][col] = new JButton();
                gridButtons[row][col].setPreferredSize(new Dimension(50, 50));
                final String coord = "" + (char)('A' + row) + (col + 1);
                gridButtons[row][col].addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        lastInput = coord;
                        inputReceived = true;
                        synchronized (GUIGameView.this) {
                            GUIGameView.this.notify();
                        }
                    }
                });
                gridPanel.add(gridButtons[row][col]);
            }
        }

        JPanel messagePanel = new JPanel();
        messagePanel.add(messageLabel);

        frame.add(gridPanel, BorderLayout.CENTER);
        frame.add(messagePanel, BorderLayout.SOUTH);
        frame.pack();
        frame.setVisible(true);
    }

    @Override
    public void displayGrid(int[][] grid) {
        if (grid == null || grid.length != 10 || grid[0].length != 10) {
            throw new IllegalArgumentException("Grid must be 10x10");
        }
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                if (grid[row][col] == 0) {
                    gridButtons[row][col].setBackground(Color.BLUE); // Water
                    gridButtons[row][col].setText("");
                } else if (grid[row][col] == 1) {
                    gridButtons[row][col].setBackground(Color.BLUE); // Ship - hidden
                    gridButtons[row][col].setText("");
                }else if (grid[row][col] == 2) {
                    gridButtons[row][col].setBackground(Color.RED); // Hit
                    gridButtons[row][col].setText("X");
                } else if (grid[row][col] == 3) {
                    gridButtons[row][col].setBackground(Color.WHITE); // Miss
                    gridButtons[row][col].setText("O");
                }else if (grid[row][col] == 4) {
                    gridButtons[row][col].setBackground(Color.GRAY); // Ship - sunk
                    gridButtons[row][col].setText("X");
                }
            }
        }
    }

    @Override
    public void showMessage(String msg) {
        if (msg == null){
            msg = "";
        }
        messageLabel.setText(msg);
    }

    @Override
    public String getInput() {
        inputReceived = false;
        while (!inputReceived) {
            synchronized (this) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
        }
        return lastInput;
    }

    /**
     * Updates GUI when Model changes.
     * @param o Must be GameModel (throws IllegalStateException otherwise)
     * @param arg Optional grid data; falls back to Model.getGrid()
     */
    @Override
    public void update(Observable o, Object arg) {
        System.out.println("[Observer] Update triggered by " + o.getClass().getSimpleName());
        if (!(o instanceof GameModel)) {
            LOGGER.warning("Received update from unexpected observable: " + o.getClass().getName());
            throw new IllegalStateException("Observer expected GameModel, got " + o.getClass().getName());
        }
        GameModel model = (GameModel) o;
        int[][] grid = (arg != null) ? (int[][]) arg : model.getGrid();
        LOGGER.fine("GUIGameView received update from GameModel");
        displayGrid(grid);
        if (model.isGameOver()) {
            showMessage("Game Over! All ships sunk!");
        }
    }
}
