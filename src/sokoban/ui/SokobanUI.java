package sokoban.ui;

import application.Main;
import application.Main.SokobanPropertyType;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JEditorPane;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLDocument;

import sokoban.file.SokobanFileLoader;
import sokoban.game.SokobanGameData;
import sokoban.game.SokobanGameStateManager;
import application.Main.SokobanPropertyType;
import static com.sun.org.apache.xalan.internal.lib.ExsltDatetime.time;
import static com.sun.org.apache.xalan.internal.lib.ExsltDatetime.time;
import java.awt.Dimension;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import static java.lang.System.gc;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.AnimationTimer;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import properties_manager.PropertiesManager;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.swing.JScrollPane;

public class SokobanUI extends Pane {

    /**
     * The SokobanUIState represents the four screen states that are possible
     * for the Sokoban game application. Depending on which state is in current
     * use, different controls will be visible.
     */
    public enum SokobanUIState {

        SPLASH_SCREEN_STATE, PLAY_GAME_STATE, VIEW_STATS_STATE, VIEW_HELP_STATE,
        HANG1_STATE, HANG2_STATE, HANG3_STATE, HANG4_STATE, HANG5_STATE, HANG6_STATE,
    }

    // mainStage
    private Stage primaryStage;

    // mainPane
    private BorderPane mainPane;
    private BorderPane hmPane;

    // SplashScreen
    private ImageView splashScreenImageView;
    private Pane splashScreenPane;
    private Label splashScreenImageLabel;
    private FlowPane levelSelectionPane;
    private ArrayList<Button> levelButtons;

    // NorthToolBar
    private HBox northToolbar;
    private Button backButton;
    private Button statsButton;
    private Button undoButton;
    private Button exitButton;

    // GamePane
    private Label SokobanLabel;
    private Button newGameButton;
    private HBox letterButtonsPane;
    private HashMap<Character, Button> letterButtons;
    private BorderPane gamePanel = new BorderPane();

    //StatsPane
    private ScrollPane statsScrollPane;
    private JEditorPane statsPane;

    //HelpPane
    private BorderPane helpPanel;
    private JScrollPane helpScrollPane;
    private JEditorPane helpPane;
    private Button homeButton;
    private Pane workspace;

    // Padding
    private Insets marginlessInsets;

    // Image path
    private String ImgPath = "file:images/";

    // mainPane weight && height
    private int paneWidth;
    private int paneHeigth;

    // GRID Renderer
    private GridRenderer gridRenderer;
    private GraphicsContext gc;

    // AND HERE IS THE GRID WE'RE MAKING
    private int gridColumns;
    private int gridRows;
    private int grid[][];
    private final int INIT_GRID_DIM = 10;
    private FileChooser fileChooser;
    private String currentLevel;

    // THIS CLASS WILL HANDLE ALL ACTION EVENTS FOR THIS PROGRAM
    private SokobanEventHandler eventHandler;
    private SokobanErrorHandler errorHandler;
    private SokobanDocumentManager docManager;

    SokobanGameStateManager gsm;
    Media backGroundMusic = new Media(new File("data/bgMusic.mp3").toURI().toString());
    MediaPlayer bGMusic = new MediaPlayer(backGroundMusic);

    public SokobanUI() {
        gsm = new SokobanGameStateManager(this);
        eventHandler = new SokobanEventHandler(this);
        errorHandler = new SokobanErrorHandler(primaryStage);
        docManager = new SokobanDocumentManager(this);
        initMainPane();
        initSplashScreen();
    }

    public void SetStage(Stage stage) {
        primaryStage = stage;
    }

    public BorderPane GetMainPane() {
        return this.mainPane;
    }

    public SokobanGameStateManager getGSM() {
        return gsm;
    }

    public SokobanDocumentManager getDocManager() {
        return docManager;
    }

    public SokobanErrorHandler getErrorHandler() {
        return errorHandler;
    }

    public SokobanEventHandler getEventHandler() {
        return eventHandler;
    }

    public JEditorPane getHelpPane() {
        return helpPane;
    }

    public int getGridColumns() {
        return gridColumns;
    }

    public int getGridRows() {
        return gridRows;
    }

    public int[][] getGrid() {
        return grid;
    }

    public void setGrid(int[][] grid) {
        this.grid = grid;
    }

    public GridRenderer getGridRenderer() {
        return gridRenderer;
    }

    /**
     * Initializes the app data.
     */
    public void initData() {
        // START OUT OUR GRID WITH DEFAULT DIMENSIONS
        gridColumns = INIT_GRID_DIM;
        gridRows = INIT_GRID_DIM;

        // NOW MAKE THE INITIALLY EMPTY GRID
        grid = new int[gridColumns][gridRows];
        for (int i = 0; i < gridColumns; i++) {
            for (int j = 0; j < gridRows; j++) {
                grid[i][j] = 0;
            }
        }
    }

    public void initMainPane() {
        marginlessInsets = new Insets(5, 5, 5, 5);
        mainPane = new BorderPane();

        PropertiesManager props = PropertiesManager.getPropertiesManager();
        paneWidth = Integer.parseInt(props
                .getProperty(SokobanPropertyType.WINDOW_WIDTH));
        paneHeigth = Integer.parseInt(props
                .getProperty(SokobanPropertyType.WINDOW_HEIGHT));
        mainPane.resize(660, 660);
        mainPane.setPadding(marginlessInsets);
    }

    public void initSplashScreen() {
        bGMusic.play();
        // INIT THE SPLASH SCREEN CONTROLS
        PropertiesManager props = PropertiesManager.getPropertiesManager();
        String splashScreenImagePath = props
                .getProperty(SokobanPropertyType.SPLASH_SCREEN_IMAGE_NAME);
        props.addProperty(SokobanPropertyType.INSETS, "5");
        String str = props.getProperty(SokobanPropertyType.INSETS);

        splashScreenPane = new GridPane();

        Image splashScreenImage = loadImage(splashScreenImagePath);
        splashScreenImageView = new ImageView(splashScreenImage);

        splashScreenImageLabel = new Label();
        splashScreenImageLabel.setGraphic(splashScreenImageView);
        // move the label position to fix the pane
        splashScreenImageLabel.setLayoutX(-45);
        splashScreenPane.getChildren().add(splashScreenImageLabel);

        // GET THE LIST OF LEVEL OPTIONS
        ArrayList<String> levels = props
                .getPropertyOptionsList(SokobanPropertyType.LEVEL_OPTIONS);
        ArrayList<String> levelImages = props
                .getPropertyOptionsList(SokobanPropertyType.LEVEL_IMAGE_NAMES);
        ArrayList<String> levelFiles = props
                .getPropertyOptionsList(SokobanPropertyType.LEVEL_FILES);

        levelSelectionPane = new FlowPane();
        levelSelectionPane.setAlignment(Pos.BOTTOM_CENTER);
        // add key listener
        levelButtons = new ArrayList<Button>();
        for (int i = 0; i < levels.size(); i++) {

            // GET THE LIST OF LEVEL OPTIONS
            String level = levels.get(i);
            String levelImageName = levelImages.get(i);
            Image levelImage = loadImage(levelImageName);
            ImageView levelImageView = new ImageView(levelImage);

            // AND BUILD THE BUTTON
            Button levelButton = new Button();
            levelButton.setText("Level " + (i + 1));
            levelButton.setMaxHeight(500);
            levelButton.setGraphic(levelImageView);

            // CONNECT THE BUTTON TO THE EVENT HANDLER
            levelButton.setOnAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent event) {
                    // TODO
                    bGMusic.stop();
                    eventHandler.respondToSelectLevelRequest(level);
                    TimeApplication timer = new TimeApplication();
                    timer.startTimer();
                    //Open Level
                    String fileName = "level_" + levelButton.getText().charAt(levelButton.getText().length() - 1) + ".sok";
                    currentLevel = levelButton.getText();
                    Path path = Paths.get(fileName);
                    System.out.println(path.toAbsolutePath());
                    File fileToOpen = path.toFile();
                    try {
                        if (fileToOpen != null) {
                            // LET'S USE A FAST LOADING TECHNIQUE. WE'LL LOAD ALL OF THE
                            // BYTES AT ONCE INTO A BYTE ARRAY, AND THEN PICK THAT APART.
                            // THIS IS FAST BECAUSE IT ONLY HAS TO DO FILE READING ONCE
                            byte[] bytes = new byte[Long.valueOf(fileToOpen.length()).intValue()];
                            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                            FileInputStream fis = new FileInputStream(fileToOpen);
                            BufferedInputStream bis = new BufferedInputStream(fis);

                            // HERE IT IS, THE ONLY READY REQUEST WE NEED
                            bis.read(bytes);
                            bis.close();

                            // NOW WE NEED TO LOAD THE DATA FROM THE BYTE ARRAY
                            DataInputStream dis = new DataInputStream(bais);

                            // NOTE THAT WE NEED TO LOAD THE DATA IN THE SAME
                            // ORDER AND FORMAT AS WE SAVED IT
                            // FIRST READ THE GRID DIMENSIONS
                            int initGridColumns = dis.readInt();
                            int initGridRows = dis.readInt();
                            int[][] newGrid = new int[initGridColumns][initGridRows];

                            // AND NOW ALL THE CELL VALUES
                            for (int i = 0; i < initGridColumns; i++) {
                                for (int j = 0; j < initGridRows; j++) {
                                    newGrid[i][j] = dis.readInt();
                                }
                            }
                            grid = newGrid;
                            gridColumns = initGridColumns;
                            gridRows = initGridRows;
                            System.out.println(gridColumns + " " + gridRows);
                            gridRenderer.repaint();

                            mainPane.setOnKeyPressed(new EventHandler<KeyEvent>() {

                                @Override
                                public void handle(KeyEvent t) {
                                    eventHandler.keyPressed(t);
                                    gamePanel.requestFocus();
                                }
                            });
                            mainPane.setOnMouseClicked(new EventHandler<MouseEvent>() {

                                @Override
                                public void handle(MouseEvent event) {
                                    eventHandler.mouseClicked(event);
                                    gamePanel.requestFocus();
                                }
                            });
                            mainPane.setOnMouseDragged(new EventHandler<MouseEvent>() {

                                @Override
                                public void handle(MouseEvent event) {
                                    eventHandler.mouseDragged(event);
                                    gamePanel.requestFocus();
                                }
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    gamePanel.setCenter(gridRenderer);
                    primaryStage.setTitle(currentLevel);
                }
            });
            // TODO

            levelSelectionPane.getChildren().add(levelButton);
            // TODO: enable only the first level
            //levelButton.setDisable(true);
        }
        mainPane.setCenter(splashScreenPane);
        //splashScreenPane.getChildren().add(pane);
        splashScreenPane.getChildren().add(levelSelectionPane);

    }

    /**
     * This method initializes the language-specific game controls, which
     * includes the three primary game screens.
     */
    public void initSokobanUI() {
        // FIRST REMOVE THE SPLASH SCREEN
        mainPane.getChildren().clear();

        // GET THE UPDATED TITLE
        PropertiesManager props = PropertiesManager.getPropertiesManager();
        String title = props.getProperty(SokobanPropertyType.GAME_TITLE_TEXT);

        // THEN ADD ALL THE STUFF WE MIGHT NOW USE
        initNorthToolbar();

        // OUR WORKSPACE WILL STORE EITHER THE GAME, STATS,
        // OR HELP UI AT ANY ONE TIME
        initWorkspace();
        initStatsPane();
        initData();
        gridRenderer = new GridRenderer();
        gridRenderer.setWidth(680);
        gridRenderer.setHeight(580);
        // WE'LL START OUT WITH THE GAME SCREEN
        changeWorkspace(SokobanUIState.PLAY_GAME_STATE);

    }

    private void initStatsPane() {
        // WE'LL DISPLAY ALL STATS IN A JEditorPane
        statsPane = new JEditorPane();
        statsPane.setEditable(false);
        statsPane.setContentType("text/html");
        statsPane.setPreferredSize(new Dimension(800, 600));
        SwingNode swingNode = new SwingNode();
        swingNode.setContent(statsPane);
        statsScrollPane = new ScrollPane();
        statsScrollPane.setContent(swingNode);
    }

    /**
     * This function initializes all the controls that go in the north toolbar.
     */
    private void initNorthToolbar() {
        // MAKE THE NORTH TOOLBAR, WHICH WILL HAVE FOUR BUTTONS
        northToolbar = new HBox();
        northToolbar.setStyle("-fx-background-color:lightgray");
        northToolbar.setAlignment(Pos.CENTER);
        northToolbar.setPadding(marginlessInsets);
        northToolbar.setSpacing(10.0);

        // MAKE AND INIT THE GAME BUTTON
        backButton = initToolbarButton(northToolbar,
                SokobanPropertyType.GAME_IMG_NAME);
        //setTooltip(backButton, SokobanPropertyType.GAME_TOOLTIP);
        backButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                // TODO Auto-generated method stub
                eventHandler
                        .respondToSwitchScreenRequest(SokobanUIState.SPLASH_SCREEN_STATE);
            }
        });

        // MAKE AND INIT THE HELP BUTTON
        undoButton = initToolbarButton(northToolbar,
                SokobanPropertyType.HELP_IMG_NAME);
        //setTooltip(undoButton, SokobanPropertyType.HELP_TOOLTIP);
        undoButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                // TODO Auto-generated method stub
                eventHandler.undo();
            }

        });
        //Timer

        // MAKE AND INIT THE STATS BUTTON
        statsButton = initToolbarButton(northToolbar,
                SokobanPropertyType.STATS_IMG_NAME);
        //setTooltip(statsButton, SokobanPropertyType.STATS_TOOLTIP);

        statsButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                // TODO Auto-generated method stub
                eventHandler.respondToSwitchScreenRequest(SokobanUIState.VIEW_STATS_STATE);
            }

        });
        // MAKE AND INIT THE EXIT BUTTON
        exitButton = initToolbarButton(northToolbar,
                SokobanPropertyType.EXIT_IMG_NAME);
        //setTooltip(exitButton, SokobanPropertyType.EXIT_TOOLTIP);
        exitButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                // TODO Auto-generated method stub
                eventHandler.respondToExitRequest(primaryStage);
            }

        });

        // AND NOW PUT THE NORTH TOOLBAR IN THE FRAME
        mainPane.setTop(northToolbar);
        //mainPane.getChildren().add(northToolbar);
    }

    /**
     * This method helps to initialize buttons for a simple toolbar.
     *
     * @param toolbar The toolbar for which to add the button.
     *
     * @param prop The property for the button we are building. This will
     * dictate which image to use for the button.
     *
     * @return A constructed button initialized and added to the toolbar.
     */
    private Button initToolbarButton(HBox toolbar, SokobanPropertyType prop) {
        // GET THE NAME OF THE IMAGE, WE DO THIS BECAUSE THE
        // IMAGES WILL BE NAMED DIFFERENT THINGS FOR DIFFERENT LANGUAGES
        PropertiesManager props = PropertiesManager.getPropertiesManager();
        String imageName = props.getProperty(prop);

        // LOAD THE IMAGE
        Image image = loadImage(imageName);
        ImageView imageIcon = new ImageView(image);

        // MAKE THE BUTTON
        Button button = new Button();
        button.setGraphic(imageIcon);
        button.setPadding(marginlessInsets);

        // PUT IT IN THE TOOLBAR
        toolbar.getChildren().add(button);

        // AND SEND BACK THE BUTTON
        return button;
    }

    /**
     * The workspace is a panel that will show different screens depending on
     * the user's requests.
     */
    private void initWorkspace() {
        // THE WORKSPACE WILL GO IN THE CENTER OF THE WINDOW, UNDER THE NORTH
        // TOOLBAR
        workspace = new Pane();
        mainPane.setCenter(workspace);
        //mainPane.getChildren().add(workspace);
        System.out.println("in the initWorkspace");
    }

    public Image loadImage(String imageName) {
        Image img = new Image(ImgPath + imageName);
        return img;
    }

    /**
     * This function selects the UI screen to display based on the uiScreen
     * argument. Note that we have 3 such screens: game, stats, and help.
     *
     * @param uiScreen The screen to be switched to.
     */
    public void changeWorkspace(SokobanUIState uiScreen) {
        switch (uiScreen) {
            case SPLASH_SCREEN_STATE:
                bGMusic.play();
                mainPane.getChildren().clear();
                primaryStage.setTitle("Select a Level");
                //Open Level   
                mainPane.setCenter(splashScreenPane);
                break;
            case VIEW_STATS_STATE:
                bGMusic.stop();
                workspace.getChildren().clear();
                if (mainPane.getCenter() == statsScrollPane) {
                    workspace.getChildren().clear();
                    workspace.getChildren().add(gamePanel);
                    mainPane.setCenter(gamePanel);
                } else {
                    workspace.getChildren().add(statsScrollPane);
                    mainPane.setCenter(statsScrollPane);
                }
                break;
            case PLAY_GAME_STATE:
                bGMusic.stop();
                workspace.getChildren().clear();
                workspace.getChildren().add(gamePanel);
                mainPane.setCenter(gamePanel);
                break;
        }
    }

    /**
     * This class renders the grid for us. Note that we also listen for mouse
     * clicks on it.
     */
    class GridRenderer extends Canvas {

        // PIXEL DIMENSIONS OF EACH CELL
        int cellWidth;
        int cellHeight;

        // images
        Image wallImage = new Image("file:images/wall.png");
        Image boxImage = new Image("file:images/box.png");
        Image placeImage = new Image("file:images/place.png");
        Image sokobanImage = new Image("file:images/Sokoban.png");

        /**
         * Default constructor.
         */
        public GridRenderer() {
            this.setWidth(500);
            this.setHeight(500);
        }

        public void repaint() {
            gc = this.getGraphicsContext2D();
            gc.clearRect(0, 0, this.getWidth(), this.getHeight());

            // CALCULATE THE GRID CELL DIMENSIONS
            double w = this.getWidth() / gridColumns;
            double h = this.getHeight() / gridRows;

            gc = this.getGraphicsContext2D();

            // NOW RENDER EACH CELL
            int x = 0, y = 0;
            for (int i = 0; i < gridColumns; i++) {
                y = 0;
                for (int j = 0; j < gridRows; j++) {
                    // DRAW THE CELL
                    gc.setFill(Color.LIGHTBLUE);
                    gc.strokeRoundRect(x, y, w, h, 10, 10);

                    switch (grid[i][j]) {
                        case 0:
                            gc.strokeRoundRect(x, y, w, h, 10, 10);
                            break;
                        case 1:
                            gc.drawImage(wallImage, x, y, w, h);
                            break;
                        case 2:
                            gc.drawImage(boxImage, x, y, w, h);
                            break;
                        case 3:
                            gc.drawImage(placeImage, x, y, w, h);
                            break;
                        case 4:
                            gc.drawImage(sokobanImage, x, y, w, h);
                            break;
                    }

                    // THEN RENDER THE TEXT
                    // ON TO THE NEXT ROW
                    y += h;
                }
                // ON TO THE NEXT COLUMN
                x += w;
            }
        }

    }

    class TimeApplication extends Application {

        private int sec = 0;
        private int min = 0;
        private int hr = 0;
        private boolean running;

        @Override
        public void start(Stage primaryStage) throws Exception {

        }

        private void startTimer() {
            if (running == true) {
                running = true;
                java.util.Timer timer = new java.util.Timer();
                TimerTask timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        System.out.println("working" + hr + " " + min + " " + sec);
                        sec++;
                        Platform.runLater(new Runnable() {
                            public void run() {
                                if (sec == 60) {
                                    sec = 0;
                                    min++;
                                }
                                if (min == 60) {
                                    min = 0;
                                    hr++;
                                }
                                String seconds = Integer.toString(sec);
                                String minutes = Integer.toString(min);
                                String hours = Integer.toString(hr);

                                if (sec <= 9) {
                                    seconds = "0" + Integer.toString(sec);
                                }
                                if (min <= 9) {
                                    minutes = "0" + Integer.toString(min);
                                }
                                if (hr <= 9) {
                                    hours = "0" + Integer.toString(hr);
                                }
                                //time.setText(hours + ":" + minutes + ":" + seconds);
                            }
                        });

                    }

                };
                timer.schedule(timerTask, 50, 50);
            }
        }

    }

    public void initFileControls() {
        // INIT THE FILE CHOOSER CONTROL
        fileChooser = new FileChooser();

        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Sokoban Files", "*.sok"));

        //File selectedFile = fileChooser.showOpenDialog(primaryStage);
    }
}
