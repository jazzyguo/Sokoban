package sokoban.ui;

import java.io.IOException;
import java.util.ArrayList;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import application.Main.SokobanPropertyType;
import java.awt.event.KeyEvent;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Stack;
import javafx.scene.input.KeyCode;
import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.LEFT;
import static javafx.scene.input.KeyCode.RIGHT;
import static javafx.scene.input.KeyCode.UP;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import properties_manager.PropertiesManager;
import xml_utilities.InvalidXMLFileFormatException;
import sokoban.file.SokobanFileLoader;
import sokoban.game.SokobanGameStateManager;

public class SokobanEventHandler {

    private Coordinates characterPos = new Coordinates();
    Stack<int[][]> gridHistory = new Stack<int[][]>();
    public ArrayList<Coordinates> dots = new ArrayList<Coordinates>();
    public ArrayList<Coordinates> blocks = new ArrayList<Coordinates>();
    private String currentLevel;
    private SokobanUI ui;
    Media move = new Media(new File("data/move1.mp3").toURI().toString());
    Media moveBlock = new Media(new File("data/1.mp3").toURI().toString());
    Media backGroundMusic = new Media(new File("data/bgMusic.mp3").toURI().toString());
    MediaPlayer moveSound = new MediaPlayer(move);
    MediaPlayer moveBlockSound = new MediaPlayer(moveBlock);
    MediaPlayer bGMusic = new MediaPlayer(backGroundMusic);

    /**
     * Constructor that simply saves the ui for later.
     *
     * @param initUI
     */
    public SokobanEventHandler(SokobanUI initUI) {
        ui = initUI;
    }

    /**
     * This method responds to when the user wishes to switch between the Game,
     * Stats, and Help screens.
     *
     * @param uiState The ui state, or screen, that the user wishes to switch
     * to.
     */
    public void respondToSwitchScreenRequest(SokobanUI.SokobanUIState uiState) {
        ui.changeWorkspace(uiState);
    }

    public ArrayList<Coordinates> getDots() {
        return dots;
    }

    /**
     * This method responds to when the user presses the new game method.
     */
    public void respondToNewGameRequest() {
        SokobanGameStateManager gsm = ui.getGSM();
        gsm.startNewGame();
    }

    /**
     * This method responds to when the user requests to exit the application.
     *
     * @param window The window that the user has requested to close.
     */
    public void respondToExitRequest(Stage primaryStage) {
        // ENGLIS IS THE DEFAULT
        String options[] = new String[]{"Yes", "No"};
        PropertiesManager props = PropertiesManager.getPropertiesManager();
        options[0] = props.getProperty(SokobanPropertyType.DEFAULT_YES_TEXT);
        options[1] = props.getProperty(SokobanPropertyType.DEFAULT_NO_TEXT);
        String verifyExit = props.getProperty(SokobanPropertyType.DEFAULT_EXIT_TEXT);

        // NOW WE'LL CHECK TO SEE IF LANGUAGE SPECIFIC VALUES HAVE BEEN SET
        if (props.getProperty(SokobanPropertyType.YES_TEXT) != null) {
            options[0] = props.getProperty(SokobanPropertyType.YES_TEXT);
            options[1] = props.getProperty(SokobanPropertyType.NO_TEXT);
            verifyExit = props.getProperty(SokobanPropertyType.EXIT_REQUEST_TEXT);
        }

        // FIRST MAKE SURE THE USER REALLY WANTS TO EXIT
        /*int selection = JOptionPane.showOptionDialog(   window, 
         verifyExit, 
         verifyExit, 
         JOptionPane.YES_NO_OPTION, 
         JOptionPane.ERROR_MESSAGE,
         null,
         options,
         null);
         // WHAT'S THE USER'S DECISION?
         if (selection == JOptionPane.YES_NO_OPTION)
         {
         // YES, LET'S EXIT
         System.exit(0);
         }*/
        // FIRST MAKE SURE THE USER REALLY WANTS TO EXIT
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(primaryStage);
        BorderPane exitPane = new BorderPane();
        HBox optionPane = new HBox();
        Button yesButton = new Button(options[0]);
        Button noButton = new Button(options[1]);
        optionPane.setSpacing(10.0);
        optionPane.getChildren().addAll(yesButton, noButton);
        Label exitLabel = new Label(verifyExit);
        exitPane.setCenter(exitLabel);
        exitPane.setBottom(optionPane);
        Scene scene = new Scene(exitPane, 250, 100);
        dialogStage.setScene(scene);
        dialogStage.show();
        // WHAT'S THE USER'S DECISION?
        yesButton.setOnAction(e -> {
            // YES, LET'S EXIT
            System.exit(0);
        });
        noButton.setOnAction(e -> {
            dialogStage.close();
        });

    }

    public void respondToSelectLevelRequest(String level) {
        SokobanGameStateManager gsm = ui.getGSM();
        currentLevel = level;
        System.out.println(level);
        ui.initSokobanUI();
        // WE'LL START THE GAME TOO
        gridHistory.clear();
        dots.clear();
        blocks.clear();
        gsm.startNewGame();
        //Open Level      
    }

    void mouseClicked(KeyCode keyCode) {
        int[][] grid = ui.getGrid();
        switch (keyCode) {
            case UP:
                // handle up 
                if (grid[characterPos.getX()][characterPos.getY() - 1] == 0
                        || grid[characterPos.getX()][characterPos.getY() - 1] == 3) {
                    int[][] previousStep = new int[grid.length][];
                    for (int x = 0; x < grid.length; x++) {
                        previousStep[x] = new int[grid[x].length];
                        for (int j = 0; j < grid[x].length; j++) {
                            previousStep[x][j] = grid[x][j];
                        }
                    }
                    gridHistory.push(previousStep);
                    moveSound.stop();
                    moveSound.play();
                    grid[characterPos.getX()][characterPos.getY()] = 0;
                    grid[characterPos.getX()][characterPos.getY() - 1] = 4;
                    for (int i = 0; i < dots.size(); i++) {
                        if (grid[dots.get(i).getX()][dots.get(i).getY()] == 0) {
                            grid[dots.get(i).getX()][dots.get(i).getY()] = 3;
                        }
                    }
                    ui.getGridRenderer().repaint();
                    if (win(grid) == true) {
                        System.out.println("YOU WIN!");
                    }
                    if (lose(grid) == true) {
                        System.out.println("YOU LOSE!");
                    }
                    break;
                }
                if (grid[characterPos.getX()][characterPos.getY() - 1] == 2
                        && (grid[characterPos.getX()][characterPos.getY() - 2] == 0)
                        || grid[characterPos.getX()][characterPos.getY() - 2] == 3) {
                    int[][] previousStep = new int[grid.length][];
                    for (int x = 0; x < grid.length; x++) {
                        previousStep[x] = new int[grid[x].length];
                        for (int j = 0; j < grid[x].length; j++) {
                            previousStep[x][j] = grid[x][j];
                        }
                    }
                    gridHistory.push(previousStep);
                    moveBlockSound.stop();
                    moveBlockSound.play();
                    grid[characterPos.getX()][characterPos.getY()] = 0;
                    grid[characterPos.getX()][characterPos.getY() - 1] = 4;
                    grid[characterPos.getX()][characterPos.getY() - 2] = 2;
                    //retain dots
                    for (int i = 0; i < dots.size(); i++) {
                        if (grid[dots.get(i).getX()][dots.get(i).getY()] == 0) {
                            grid[dots.get(i).getX()][dots.get(i).getY()] = 3;
                        }
                    }
                    ui.getGridRenderer().repaint();
                    if (win(grid) == true) {
                        System.out.println("YOU WIN!");
                    }
                    if (lose(grid) == true) {
                        System.out.println("YOU LOSE!");
                    }
                    break;
                } else {
                    break;
                }
            case DOWN:
                if (grid[characterPos.getX()][characterPos.getY() + 1] == 0
                        || grid[characterPos.getX()][characterPos.getY() + 1] == 3) {
                    int[][] previousStep = new int[grid.length][];
                    for (int x = 0; x < grid.length; x++) {
                        previousStep[x] = new int[grid[x].length];
                        for (int j = 0; j < grid[x].length; j++) {
                            previousStep[x][j] = grid[x][j];
                        }
                    }
                    gridHistory.push(previousStep);
                    moveSound.stop();
                    moveSound.play();
                    grid[characterPos.getX()][characterPos.getY()] = 0;
                    grid[characterPos.getX()][characterPos.getY() + 1] = 4;
                    for (int i = 0; i < dots.size(); i++) {
                        if (grid[dots.get(i).getX()][dots.get(i).getY()] == 0) {
                            grid[dots.get(i).getX()][dots.get(i).getY()] = 3;
                        }
                    }
                    ui.getGridRenderer().repaint();
                    if (win(grid) == true) {
                        System.out.println("YOU WIN!");
                    }
                    if (lose(grid) == true) {
                        System.out.println("YOU LOSE!");
                    }
                    break;
                }
                if (grid[characterPos.getX()][characterPos.getY() + 1] == 2
                        && (grid[characterPos.getX()][characterPos.getY() + 2] == 0)
                        || grid[characterPos.getX()][characterPos.getY() + 2] == 3) {
                    int[][] previousStep = new int[grid.length][];
                    for (int x = 0; x < grid.length; x++) {
                        previousStep[x] = new int[grid[x].length];
                        for (int j = 0; j < grid[x].length; j++) {
                            previousStep[x][j] = grid[x][j];
                        }
                    }
                    gridHistory.push(previousStep);
                    moveBlockSound.stop();
                    moveBlockSound.play();
                    grid[characterPos.getX()][characterPos.getY()] = 0;
                    grid[characterPos.getX()][characterPos.getY() + 1] = 4;
                    grid[characterPos.getX()][characterPos.getY() + 2] = 2;
                    for (int i = 0; i < dots.size(); i++) {
                        if (grid[dots.get(i).getX()][dots.get(i).getY()] == 0) {
                            grid[dots.get(i).getX()][dots.get(i).getY()] = 3;
                        }
                    }
                    ui.getGridRenderer().repaint();
                    if (win(grid) == true) {
                        System.out.println("YOU WIN!");
                    }
                    if (lose(grid) == true) {
                        System.out.println("YOU LOSE!");
                    }
                    break;
                } else {
                    break;
                }
            case LEFT:
                if (grid[characterPos.getX() - 1][characterPos.getY()] == 0
                        || grid[characterPos.getX() - 1][characterPos.getY()] == 3) {
                    int[][] previousStep = new int[grid.length][];
                    for (int x = 0; x < grid.length; x++) {
                        previousStep[x] = new int[grid[x].length];
                        for (int j = 0; j < grid[x].length; j++) {
                            previousStep[x][j] = grid[x][j];
                        }
                    }
                    gridHistory.push(previousStep);
                    moveSound.stop();
                    moveSound.play();
                    grid[characterPos.getX()][characterPos.getY()] = 0;
                    grid[characterPos.getX() - 1][characterPos.getY()] = 4;
                    for (int i = 0; i < dots.size(); i++) {
                        if (grid[dots.get(i).getX()][dots.get(i).getY()] == 0) {
                            grid[dots.get(i).getX()][dots.get(i).getY()] = 3;
                        }
                    }
                    ui.getGridRenderer().repaint();
                    if (win(grid) == true) {
                        System.out.println("YOU WIN!");
                    }
                    if (lose(grid) == true) {
                        System.out.println("YOU LOSE!");
                    }
                    break;
                }
                if (grid[characterPos.getX() - 1][characterPos.getY()] == 2
                        && (grid[characterPos.getX() - 2][characterPos.getY()] == 0)
                        || grid[characterPos.getX() - 2][characterPos.getY()] == 3) {
                    int[][] previousStep = new int[grid.length][];
                    for (int x = 0; x < grid.length; x++) {
                        previousStep[x] = new int[grid[x].length];
                        for (int j = 0; j < grid[x].length; j++) {
                            previousStep[x][j] = grid[x][j];
                        }
                    }
                    gridHistory.push(previousStep);
                    moveBlockSound.stop();
                    moveBlockSound.play();
                    grid[characterPos.getX()][characterPos.getY()] = 0;
                    grid[characterPos.getX() - 1][characterPos.getY()] = 4;
                    grid[characterPos.getX() - 2][characterPos.getY()] = 2;
                    for (int i = 0; i < dots.size(); i++) {
                        if (grid[dots.get(i).getX()][dots.get(i).getY()] == 0) {
                            grid[dots.get(i).getX()][dots.get(i).getY()] = 3;
                        }
                    }
                    ui.getGridRenderer().repaint();
                    if (win(grid) == true) {
                        System.out.println("YOU WIN!");
                    }
                    if (lose(grid) == true) {
                        System.out.println("YOU LOSE!");
                    }
                    break;
                } else {
                    break;
                }
            case RIGHT:
                if (grid[characterPos.getX() + 1][characterPos.getY()] == 0
                        || grid[characterPos.getX() + 1][characterPos.getY()] == 3) {
                    int[][] previousStep = new int[grid.length][];
                    for (int x = 0; x < grid.length; x++) {
                        previousStep[x] = new int[grid[x].length];
                        for (int j = 0; j < grid[x].length; j++) {
                            previousStep[x][j] = grid[x][j];
                        }
                    }
                    gridHistory.push(previousStep);
                    moveSound.stop();
                    moveSound.play();
                    grid[characterPos.getX()][characterPos.getY()] = 0;
                    grid[characterPos.getX() + 1][characterPos.getY()] = 4;
                    for (int i = 0; i < dots.size(); i++) {
                        if (grid[dots.get(i).getX()][dots.get(i).getY()] == 0) {
                            grid[dots.get(i).getX()][dots.get(i).getY()] = 3;
                        }
                    }
                    ui.getGridRenderer().repaint();
                    if (win(grid) == true) {
                        System.out.println("YOU WIN!");
                    }
                    if (lose(grid) == true) {
                        System.out.println("YOU LOSE!");
                    }
                    break;
                }
                if (grid[characterPos.getX() + 1][characterPos.getY()] == 2
                        && (grid[characterPos.getX() + 2][characterPos.getY()] == 0)
                        || grid[characterPos.getX() + 2][characterPos.getY()] == 3) {
                    int[][] previousStep = new int[grid.length][];
                    for (int x = 0; x < grid.length; x++) {
                        previousStep[x] = new int[grid[x].length];
                        for (int j = 0; j < grid[x].length; j++) {
                            previousStep[x][j] = grid[x][j];
                        }
                    }
                    gridHistory.push(previousStep);
                    moveBlockSound.stop();
                    moveBlockSound.play();
                    grid[characterPos.getX()][characterPos.getY()] = 0;
                    grid[characterPos.getX() + 1][characterPos.getY()] = 4;
                    grid[characterPos.getX() + 2][characterPos.getY()] = 2;
                    for (int i = 0; i < dots.size(); i++) {
                        if (grid[dots.get(i).getX()][dots.get(i).getY()] == 0) {
                            grid[dots.get(i).getX()][dots.get(i).getY()] = 3;
                        }
                    }
                    ui.getGridRenderer().repaint();
                    if (win(grid) == true) {
                        System.out.println("YOU WIN!");
                    }
                    if (lose(grid) == true) {
                        System.out.println("YOU LOSE!");
                    }
                    break;
                } else {
                    break;
                }
        }
    }

    void keyPressed(javafx.scene.input.KeyEvent t) {
        KeyCode keyCode = t.getCode();
        int[][] grid = ui.getGrid();
        for (int i = 0; i < ui.getGridColumns(); i++) {
            for (int j = 0; j < ui.getGridRows(); j++) {
                if (grid[i][j] == 4) {
                    characterPos.setX(i);
                    characterPos.setY(j);
                }
                if (grid[i][j] == 3) {
                    Coordinates dot = new Coordinates(i, j);
                    dots.add(dot);
                }
                if (grid[i][j] == 2) {
                    Coordinates block = new Coordinates(i, j);
                    blocks.add(block);
                }
            }
        }
        switch (keyCode) {
            case UP:
                // handle up 
                if (grid[characterPos.getX()][characterPos.getY() - 1] == 0
                        || grid[characterPos.getX()][characterPos.getY() - 1] == 3) {
                    int[][] previousStep = new int[grid.length][];
                    for (int x = 0; x < grid.length; x++) {
                        previousStep[x] = new int[grid[x].length];
                        for (int j = 0; j < grid[x].length; j++) {
                            previousStep[x][j] = grid[x][j];
                        }
                    }
                    gridHistory.push(previousStep);
                    moveSound.stop();
                    moveSound.play();
                    grid[characterPos.getX()][characterPos.getY()] = 0;
                    grid[characterPos.getX()][characterPos.getY() - 1] = 4;
                    for (int i = 0; i < dots.size(); i++) {
                        if (grid[dots.get(i).getX()][dots.get(i).getY()] == 0) {
                            grid[dots.get(i).getX()][dots.get(i).getY()] = 3;
                        }
                    }
                    ui.getGridRenderer().repaint();
                    if (win(grid) == true) {
                        System.out.println("YOU WIN!");
                    }
                    if (lose(grid) == true) {
                        System.out.println("YOU LOSE!");
                    }
                    break;
                }
                if (grid[characterPos.getX()][characterPos.getY() - 1] == 2
                        && (grid[characterPos.getX()][characterPos.getY() - 2] == 0)
                        || grid[characterPos.getX()][characterPos.getY() - 2] == 3) {
                    int[][] previousStep = new int[grid.length][];
                    for (int x = 0; x < grid.length; x++) {
                        previousStep[x] = new int[grid[x].length];
                        for (int j = 0; j < grid[x].length; j++) {
                            previousStep[x][j] = grid[x][j];
                        }
                    }
                    gridHistory.push(previousStep);
                    moveBlockSound.stop();
                    moveBlockSound.play();
                    grid[characterPos.getX()][characterPos.getY()] = 0;
                    grid[characterPos.getX()][characterPos.getY() - 1] = 4;
                    grid[characterPos.getX()][characterPos.getY() - 2] = 2;
                    //retain dots
                    for (int i = 0; i < dots.size(); i++) {
                        if (grid[dots.get(i).getX()][dots.get(i).getY()] == 0) {
                            grid[dots.get(i).getX()][dots.get(i).getY()] = 3;
                        }
                    }
                    ui.getGridRenderer().repaint();
                    if (win(grid) == true) {
                        System.out.println("YOU WIN!");
                    }
                    if (lose(grid) == true) {
                        System.out.println("YOU LOSE!");
                    }
                    break;
                } else {
                    break;
                }
            case DOWN:
                if (grid[characterPos.getX()][characterPos.getY() + 1] == 0
                        || grid[characterPos.getX()][characterPos.getY() + 1] == 3) {
                    int[][] previousStep = new int[grid.length][];
                    for (int x = 0; x < grid.length; x++) {
                        previousStep[x] = new int[grid[x].length];
                        for (int j = 0; j < grid[x].length; j++) {
                            previousStep[x][j] = grid[x][j];
                        }
                    }
                    gridHistory.push(previousStep);
                    moveSound.stop();
                    moveSound.play();
                    grid[characterPos.getX()][characterPos.getY()] = 0;
                    grid[characterPos.getX()][characterPos.getY() + 1] = 4;
                    for (int i = 0; i < dots.size(); i++) {
                        if (grid[dots.get(i).getX()][dots.get(i).getY()] == 0) {
                            grid[dots.get(i).getX()][dots.get(i).getY()] = 3;
                        }
                    }
                    ui.getGridRenderer().repaint();
                    if (win(grid) == true) {
                        System.out.println("YOU WIN!");
                    }
                    if (lose(grid) == true) {
                        System.out.println("YOU LOSE!");
                    }
                    break;
                }
                if (grid[characterPos.getX()][characterPos.getY() + 1] == 2
                        && (grid[characterPos.getX()][characterPos.getY() + 2] == 0)
                        || grid[characterPos.getX()][characterPos.getY() + 2] == 3) {
                    int[][] previousStep = new int[grid.length][];
                    for (int x = 0; x < grid.length; x++) {
                        previousStep[x] = new int[grid[x].length];
                        for (int j = 0; j < grid[x].length; j++) {
                            previousStep[x][j] = grid[x][j];
                        }
                    }
                    gridHistory.push(previousStep);
                    moveBlockSound.stop();
                    moveBlockSound.play();
                    grid[characterPos.getX()][characterPos.getY()] = 0;
                    grid[characterPos.getX()][characterPos.getY() + 1] = 4;
                    grid[characterPos.getX()][characterPos.getY() + 2] = 2;
                    for (int i = 0; i < dots.size(); i++) {
                        if (grid[dots.get(i).getX()][dots.get(i).getY()] == 0) {
                            grid[dots.get(i).getX()][dots.get(i).getY()] = 3;
                        }
                    }
                    ui.getGridRenderer().repaint();
                    if (win(grid) == true) {
                        System.out.println("YOU WIN!");
                    }
                    if (lose(grid) == true) {
                        System.out.println("YOU LOSE!");
                    }
                    break;
                } else {
                    break;
                }
            case LEFT:
                if (grid[characterPos.getX() - 1][characterPos.getY()] == 0
                        || grid[characterPos.getX() - 1][characterPos.getY()] == 3) {
                    int[][] previousStep = new int[grid.length][];
                    for (int x = 0; x < grid.length; x++) {
                        previousStep[x] = new int[grid[x].length];
                        for (int j = 0; j < grid[x].length; j++) {
                            previousStep[x][j] = grid[x][j];
                        }
                    }
                    gridHistory.push(previousStep);
                    moveSound.stop();
                    moveSound.play();
                    grid[characterPos.getX()][characterPos.getY()] = 0;
                    grid[characterPos.getX() - 1][characterPos.getY()] = 4;
                    for (int i = 0; i < dots.size(); i++) {
                        if (grid[dots.get(i).getX()][dots.get(i).getY()] == 0) {
                            grid[dots.get(i).getX()][dots.get(i).getY()] = 3;
                        }
                    }
                    ui.getGridRenderer().repaint();
                    if (win(grid) == true) {
                        System.out.println("YOU WIN!");
                    }
                    if (lose(grid) == true) {
                        System.out.println("YOU LOSE!");
                    }
                    break;
                }
                if (grid[characterPos.getX() - 1][characterPos.getY()] == 2
                        && (grid[characterPos.getX() - 2][characterPos.getY()] == 0)
                        || grid[characterPos.getX() - 2][characterPos.getY()] == 3) {
                    int[][] previousStep = new int[grid.length][];
                    for (int x = 0; x < grid.length; x++) {
                        previousStep[x] = new int[grid[x].length];
                        for (int j = 0; j < grid[x].length; j++) {
                            previousStep[x][j] = grid[x][j];
                        }
                    }
                    gridHistory.push(previousStep);
                    moveBlockSound.stop();
                    moveBlockSound.play();
                    grid[characterPos.getX()][characterPos.getY()] = 0;
                    grid[characterPos.getX() - 1][characterPos.getY()] = 4;
                    grid[characterPos.getX() - 2][characterPos.getY()] = 2;
                    for (int i = 0; i < dots.size(); i++) {
                        if (grid[dots.get(i).getX()][dots.get(i).getY()] == 0) {
                            grid[dots.get(i).getX()][dots.get(i).getY()] = 3;
                        }
                    }
                    ui.getGridRenderer().repaint();
                    if (win(grid) == true) {
                        System.out.println("YOU WIN!");
                    }
                    if (lose(grid) == true) {
                        System.out.println("YOU LOSE!");
                    }
                    break;
                } else {
                    break;
                }
            case RIGHT:
                if (grid[characterPos.getX() + 1][characterPos.getY()] == 0
                        || grid[characterPos.getX() + 1][characterPos.getY()] == 3) {
                    int[][] previousStep = new int[grid.length][];
                    for (int x = 0; x < grid.length; x++) {
                        previousStep[x] = new int[grid[x].length];
                        for (int j = 0; j < grid[x].length; j++) {
                            previousStep[x][j] = grid[x][j];
                        }
                    }
                    gridHistory.push(previousStep);
                    moveSound.stop();
                    moveSound.play();
                    grid[characterPos.getX()][characterPos.getY()] = 0;
                    grid[characterPos.getX() + 1][characterPos.getY()] = 4;
                    for (int i = 0; i < dots.size(); i++) {
                        if (grid[dots.get(i).getX()][dots.get(i).getY()] == 0) {
                            grid[dots.get(i).getX()][dots.get(i).getY()] = 3;
                        }
                    }
                    ui.getGridRenderer().repaint();
                    if (win(grid) == true) {
                        System.out.println("YOU WIN!");
                    }
                    if (lose(grid) == true) {
                        System.out.println("YOU LOSE!");
                    }
                    break;
                }
                if (grid[characterPos.getX() + 1][characterPos.getY()] == 2
                        && (grid[characterPos.getX() + 2][characterPos.getY()] == 0)
                        || grid[characterPos.getX() + 2][characterPos.getY()] == 3) {
                    int[][] previousStep = new int[grid.length][];
                    for (int x = 0; x < grid.length; x++) {
                        previousStep[x] = new int[grid[x].length];
                        for (int j = 0; j < grid[x].length; j++) {
                            previousStep[x][j] = grid[x][j];
                        }
                    }
                    gridHistory.push(previousStep);
                    moveBlockSound.stop();
                    moveBlockSound.play();
                    grid[characterPos.getX()][characterPos.getY()] = 0;
                    grid[characterPos.getX() + 1][characterPos.getY()] = 4;
                    grid[characterPos.getX() + 2][characterPos.getY()] = 2;
                    for (int i = 0; i < dots.size(); i++) {
                        if (grid[dots.get(i).getX()][dots.get(i).getY()] == 0) {
                            grid[dots.get(i).getX()][dots.get(i).getY()] = 3;
                        }
                    }
                    ui.getGridRenderer().repaint();
                    if (win(grid) == true) {
                        System.out.println("YOU WIN!");
                    }
                    if (lose(grid) == true) {
                        System.out.println("YOU LOSE!");
                    }
                    break;
                } else {
                    break;
                }
            case U:
                undo();
                break;
        }
    }

    void undo() {
        if (!gridHistory.isEmpty()) {
            int[][] previousStep = gridHistory.pop();
            int[][] grid = ui.getGrid();
            for (int i = 0; i < grid.length; i++) {
                for (int j = 0; j < grid[i].length; j++) {
                    grid[i][j] = previousStep[i][j];
                }
            }
        }
        ui.getGridRenderer().repaint();
    }

    public boolean win(int[][] grid) {
        for (int i = 0; i < dots.size(); i++) {
            if (!(grid[dots.get(i).getX()][dots.get(i).getY()] == 2)) {
                return false;
            }
        }
        // ENGLIS IS THE DEFAULT
        PropertiesManager props = PropertiesManager.getPropertiesManager();
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.WINDOW_MODAL);
        BorderPane exitPane = new BorderPane();
        HBox optionPane = new HBox();
        Button yesButton = new Button();
        Button restart = new Button();
        restart.setText("Play Again");
        yesButton.setText("YOU WIN!");
        exitPane.setCenter(yesButton);
        Scene scene = new Scene(exitPane, 250, 100);
        dialogStage.setScene(scene);
        dialogStage.show();
        // WHAT'S THE USER'S DECISION?
        yesButton.setOnAction(e -> {
            respondToSwitchScreenRequest(SokobanUI.SokobanUIState.SPLASH_SCREEN_STATE);
            dialogStage.close();
        });
        restart.setOnAction(e -> {
            respondToSelectLevelRequest(currentLevel);
            dialogStage.close();
        }
        );
        //makes sure YOU WIN box doesn't reappear 
        grid = ui.getGrid();
        for (int i = 0;
                i < ui.getGridColumns();
                i++) {
            for (int j = 0; j < ui.getGridRows(); j++) {
                grid[i][j] = 0;
            }
        }
        return true;
    }

    void mouseClicked(MouseEvent event) {
        int[][] grid = ui.getGrid();
        for (int i = 0; i < ui.getGridColumns(); i++) {
            for (int j = 0; j < ui.getGridRows(); j++) {
                if (grid[i][j] == 4) {
                    characterPos.setX(i);
                    characterPos.setY(j);
                }
                if (grid[i][j] == 3) {
                    Coordinates dot = new Coordinates(i, j);
                    dots.add(dot);
                }
                if (grid[i][j] == 2) {
                    Coordinates block = new Coordinates(i, j);
                    blocks.add(block);
                }
            }
        }
        int cX = characterPos.getX();
        int cY = characterPos.getY();
        Coordinates mouseCoordinates = new Coordinates((int) (event.getX()) / 90, (int) (event.getY() - 40) / 90);
        String position = "";
        if (mouseCoordinates.getX() - 1 == characterPos.getX()) {
            position = "right";
        }
        if (mouseCoordinates.getX() + 1 == characterPos.getX()) {
            position = "left";
        }
        if (mouseCoordinates.getY() + 1 == characterPos.getY()) {
            position = "up";
        }
        if (mouseCoordinates.getY() - 1 == characterPos.getY()) {
            position = "down";
        }
        switch (position) {
            case "right":
                mouseClicked(RIGHT);
                break;
            case "left":
                mouseClicked(LEFT);
                break;
            case "up":
                mouseClicked(UP);
                break;
            case "down":
                mouseClicked(DOWN);
                break;
        }

    }

    void mouseDragged(MouseEvent event) {
    }

    public void showLose() {
        // ENGLIS IS THE DEFAULT
        PropertiesManager props = PropertiesManager.getPropertiesManager();
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.WINDOW_MODAL);
        BorderPane exitPane = new BorderPane();
        HBox optionPane = new HBox();
        Button yesButton = new Button();
        Button restart = new Button();
        restart.setText("Play Again");
        yesButton.setText("YOU LOSE!");
        exitPane.setCenter(yesButton);
        Scene scene = new Scene(exitPane, 250, 100);
        dialogStage.setScene(scene);
        dialogStage.show();
        // WHAT'S THE USER'S DECISION?
        yesButton.setOnAction(e -> {
            respondToSwitchScreenRequest(SokobanUI.SokobanUIState.SPLASH_SCREEN_STATE);
            dialogStage.close();
        });
        restart.setOnAction(e -> {
            respondToSelectLevelRequest(currentLevel);
            dialogStage.close();
        }
        );
        //makes sure YOU WIN box doesn't reappear 
        int[][] grid = ui.getGrid();
        for (int i = 0;
                i < ui.getGridColumns();
                i++) {
            for (int j = 0; j < ui.getGridRows(); j++) {
                grid[i][j] = 0;
            }
        }
    }

    private boolean lose(int[][] grid) {
        for (int i = 0; i < blocks.size(); i++) {
            //E + S
            if ((grid[blocks.get(i).getX() + 1][blocks.get(i).getY()] == 1
                    || grid[blocks.get(i).getX() + 1][blocks.get(i).getY()] == 1)
                    && (grid[blocks.get(i).getX()][blocks.get(i).getY() + 1] == 1
                    || grid[blocks.get(i).getX()][blocks.get(i).getY() + 1] == 1)) {
                if (!isDot(blocks.get(i))) {
                    showLose();
                    return true;
                }
            }
            //W + S
            if ((grid[blocks.get(i).getX() - 1][blocks.get(i).getY()] == 1
                    || grid[blocks.get(i).getX() - 1][blocks.get(i).getY()] == 1)
                    && (grid[blocks.get(i).getX()][blocks.get(i).getY() + 1] == 1
                    || grid[blocks.get(i).getX()][blocks.get(i).getY() + 1] == 1)) {
                if (!isDot(blocks.get(i))) {
                    showLose();
                    return true;
                }
            }
            //N+W
            if ((grid[blocks.get(i).getX()][blocks.get(i).getY() - 1] == 1
                    || grid[blocks.get(i).getX()][blocks.get(i).getY() - 1] == 1)
                    && (grid[blocks.get(i).getX() - 1][blocks.get(i).getY()] == 1
                    || grid[blocks.get(i).getX() - 1][blocks.get(i).getY()] == 1)) {
                if (!isDot(blocks.get(i))) {
                    showLose();
                    return true;
                }
            }//N+E
            if ((grid[blocks.get(i).getX()][blocks.get(i).getY() - 1] == 1
                    || grid[blocks.get(i).getX()][blocks.get(i).getY() - 1] == 1)
                    && (grid[blocks.get(i).getX() + 1][blocks.get(i).getY()] == 1
                    || grid[blocks.get(i).getX() + 1][blocks.get(i).getY()] == 1)) {
                if (!isDot(blocks.get(i))) {
                    showLose();
                    return true;
                }
            }
        }
        return false;
    }

    boolean isDot(Coordinates coord) {
        for (int i = 0; i < dots.size(); i++) {
            if (dots.get(i).getX() == coord.getX()
                    && dots.get(i).getY() == coord.getY()) {
                return true;
            }
        }
        return false;
    }

    public class Coordinates {

        private int X;
        private int Y;

        public Coordinates() {
            this(0, 0);
        }

        public Coordinates(int X, int Y) {
            this.X = X;
            this.Y = Y;
        }

        public int getX() {
            return X;
        }

        public int getY() {
            return Y;
        }

        public void setX(int X) {
            this.X = X;
        }

        public void setY(int Y) {
            this.Y = Y;
        }
    }
}
