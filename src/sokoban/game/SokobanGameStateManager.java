package sokoban.game;

import java.util.ArrayList;
import java.util.Iterator;

import sokoban.ui.SokobanUI;

public class SokobanGameStateManager {

    // THE GAME WILL ALWAYS BE IN
    // ONE OF THESE STATES
    public enum SokobanGameState {

        GAME_NOT_STARTED, GAME_IN_PROGRESS, GAME_OVER,
        LEVEL1,
        LEVEL2,
        LEVEL3,
        LEVEL4,
        LEVEL5,
        LEVEL6,
        LEVEL7

    }

    // STORES THE CURRENT STATE OF THIS GAME
    private SokobanGameState currentGameState;

    // WHEN THE STATE OF THE GAME CHANGES IT WILL NEED TO BE
    // REFLECTED IN THE USER INTERFACE, SO THIS CLASS NEEDS
    // A REFERENCE TO THE UI
    private SokobanUI ui;

    // THIS IS THE GAME CURRENTLY BEING PLAYED
    private SokobanGameData gameInProgress;

    // HOLDS ALL OF THE COMPLETED GAMES. NOTE THAT THE GAME
    // IN PROGRESS IS NOT ADDED UNTIL IT IS COMPLETED
    private ArrayList<SokobanGameData> gamesHistory;

    private final String NEWLINE_DELIMITER = "\n";

    public SokobanGameStateManager(SokobanUI initUI) {
        ui = initUI;

        // WE HAVE NOT STARTED A GAME YET
        currentGameState = SokobanGameState.GAME_NOT_STARTED;

        // NO GAMES HAVE BEEN PLAYED YET, BUT INITIALIZE
        // THE DATA STRCUTURE FOR PLACING COMPLETED GAMES
        gamesHistory = new ArrayList();

        // THE FIRST GAME HAS NOT BEEN STARTED YET
        gameInProgress = null;
    }

    // ACCESSOR METHODS
    /**
     * Accessor method for getting the game currently being played.
     *
     * @return The game currently being played.
     */
    public SokobanGameData getGameInProgress() {
        return gameInProgress;
    }

    /**
     * Accessor method for getting the number of games that have been played.
     *
     * @return The total number of games that have been played during this game
     * session.
     */
    public int getGamesPlayed() {
        return gamesHistory.size();
    }

    /**
     * Accessor method for getting all the games that have been completed.
     *
     * @return An Iterator that allows one to go through all the games that have
     * been played so far.
     */
    public Iterator<SokobanGameData> getGamesHistoryIterator() {
        return gamesHistory.iterator();
    }

    /**
     * Accessor method for testing to see if any games have been started yet.
     *
     * @return true if at least one game has already been started during this
     * session, false otherwise.
     */
    public boolean isGameNotStarted() {
        return currentGameState == SokobanGameState.GAME_NOT_STARTED;
    }

    /**
     * Accessor method for testing to see if the current game is over.
     *
     * @return true if the game in progress has completed, false otherwise.
     */
    public boolean isGameOver() {
        return currentGameState == SokobanGameState.GAME_OVER;
    }

    /**
     * Accessor method for testing to see if the current game is in progress.
     *
     * @return true if a game is in progress, false otherwise.
     */
    public boolean isGameInProgress() {
        return currentGameState == SokobanGameState.GAME_IN_PROGRESS;
    }

    /**
     * Counts and returns the number of wins during this game session.
     *
     * @return The number of games in that have been completed that the player
     * won.
     */
    public int getWins() {
        // ITERATE THROUGH ALL THE COMPLETED GAMES
        Iterator<SokobanGameData> it = gamesHistory.iterator();
        int wins = 0;
        while (it.hasNext()) {
            // GET THE NEXT GAME IN THE SEQUENCE
            SokobanGameData game = it.next();

            // TODO
            // IF IT ENDED IN A WIN, INC THE COUNTER
            if (game.isWon()) {
                wins++;
            }
        }
        return wins;
    }

    /**
     * Counts and returns the number of losses during this game session.
     *
     * @return The number of games in that have been completed that the player
     * lost.
     */
    public int getLosses() {
        // ITERATE THROUGH ALL THE COMPLETED GAMES
        Iterator<SokobanGameData> it = gamesHistory.iterator();
        int losses = 0;
        while (it.hasNext()) {
            // GET THE NEXT GAME IN THE SEQUENCE
            SokobanGameData game = it.next();

            // TODO
            // IF IT ENDED IN A LOSS, INC THE COUNTER
            if (game.isLost()) {
                losses++;
            }
        }
        return losses;
    }

    /**
     * Finds the completed game that the player won that required the least
     * amount of time.
     *
     * @return The completed game that the player won requiring the least amount
     * of time.
     */
    public SokobanGameData getFastestWin() {
        // IF NO GAMES HAVE BEEN PLAYED, THERE IS
        // NOTHING TO RETURN
        if (gamesHistory.isEmpty()) {
            return null;
        }

        // NOTE THAT ALL THE GAMES PLAYED MAY BE LOSSES
        SokobanGameData fastest = null;

        // GO THROUGH ALL THE GAMES THAT HAVE BEEN PLAYED
        Iterator<SokobanGameData> it = gamesHistory.iterator();
        while (it.hasNext()) {
            // GET THE NEXT GAME IN THE SEQUENCE
            SokobanGameData game = it.next();

            // WE ONLY CONSIDER GAMES THAT WERE WON
            if (game.isWon()) {
                // IF IT'S THE FIRST WIN FOUND, START OUT
                // WITH IT AS THE FASTEST UNTIL WE FIND ONE BETTER
                if (fastest == null) {
                    fastest = game;
                } // OTHERWISE IF IT IS FASTER THEN
                // MAKE IT THE FASTEST           
                else if (game.getTimeOfGame() < fastest.getTimeOfGame()) {
                    fastest = game;
                }
            }
        }
        // RETURN THE FASTEST GAME
        return fastest;
    }

    /**
     * This method starts a new game, initializing all the necessary data for
     * that new game as well as recording the current game (if it exists) in the
     * games history data structure. It also lets the user interface know about
     * this change of state such that it may reflect this change.
     */
    public void startNewGame() {
        // IS THERE A GAME ALREADY UNDERWAY?
        // YES, SO END THAT GAME AS A LOSS
        if (!isGameNotStarted() && (!gamesHistory.contains(gameInProgress))) {
            gamesHistory.add(gameInProgress);
        }

        // IF THERE IS A GAME IN PROGRESS AND THE PLAYER HASN'T WON, THAT MEANS
        // THE PLAYER IS QUITTING, SO WE NEED TO SAVE THE GAME TO OUR HISTORY
        // DATA STRUCTURE. NOTE THAT IF THE PLAYER WON THE GAME, IT WOULD HAVE
        // ALREADY BEEN SAVED SINCE THERE WOULD BE NO GUARANTEE THE PLAYER WOULD
        // CHOOSE TO PLAY AGAIN
        if (isGameInProgress() && !gameInProgress.isWon()) {
            // QUIT THE GAME, WHICH SETS THE END TIME
            gameInProgress.giveUp();

            // TODO: add game result to stats page
        }

        // AND NOW MAKE A NEW GAME
        makeNewGame();

        // AND MAKE SURE THE UI REFLECTS A NEW GAME
        //ui.resetUI();
    }

    /**
     * This method chooses a secret word and uses it to create a new game,
     * effectively starting it.
     */
    public void makeNewGame() {
        // TODO: create a game for a level
        gameInProgress = new SokobanGameData(1);

        // THE GAME IS OFFICIALLY UNDERWAY
        currentGameState = SokobanGameState.GAME_IN_PROGRESS;
    }

}
