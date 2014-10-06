package sokoban.game;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Iterator;

/**
 * SokobanGameData stores the data necessary for a single Sokoban game. Note
 * that this class works in concert with the SokobanGameStateManager, so all
 * instance variables have default (package-level) access.
 */
public class SokobanGameData {

    // START AND END TIME WILL BE USED TO CALCULATE THE
    // TIME IT TAKES TO PLAY THIS GAME
    GregorianCalendar startTime;
    GregorianCalendar endTime;

    // THESE ARE USED FOR FORMATTING THE TIME OF GAME
    final long MILLIS_IN_A_SECOND = 1000;
    final long MILLIS_IN_A_MINUTE = 1000 * 60;
    final long MILLIS_IN_AN_HOUR = 1000 * 60 * 60;

    /*
     * Construct this object when a game begins.
     */
    public SokobanGameData(int level) {
        startTime = new GregorianCalendar();
        endTime = null;
    }

    // ACCESSOR METHODS
    /**
     * Gets the total time (in milliseconds) that this game took.
     *
     * @return The time of the game in milliseconds.
     */
    public long getTimeOfGame() {
        // IF THE GAME ISN'T OVER YET, THERE IS NO POINT IN CONTINUING
        if (endTime == null) {
            return -1;
        }

        // THE TIME OF THE GAME IS END-START
        long startTimeInMillis = startTime.getTimeInMillis();
        long endTimeInMillis = endTime.getTimeInMillis();

        // CALC THE DIFF AND RETURN IT
        long diff = endTimeInMillis - startTimeInMillis;
        return diff;
    }

    /**
     * Called when a player quits a game before ending the game.
     */
    public void giveUp() {
        endTime = new GregorianCalendar();
    }

    /**
     * Builds and returns a textual summary of this game.
     *
     * @return A textual summary of this game, including the secred word, the
     * time of the game, and a listing of all the guesses.
     */
    @Override
    public String toString() {
        // CALCULATE GAME TIME USING HOURS : MINUTES : SECONDS
        long timeInMillis = this.getTimeOfGame();
        long hours = timeInMillis / MILLIS_IN_AN_HOUR;
        timeInMillis -= hours * MILLIS_IN_AN_HOUR;
        long minutes = timeInMillis / MILLIS_IN_A_MINUTE;
        timeInMillis -= minutes * MILLIS_IN_A_MINUTE;
        long seconds = timeInMillis / MILLIS_IN_A_SECOND;

        // THEN ADD THE TIME OF GAME SUMMARIZED IN PARENTHESES
        String minutesText = "" + minutes;
        if (minutes < 10) {
            minutesText = "0" + minutesText;
        }
        String secondsText = "" + seconds;
        if (seconds < 10) {
            secondsText = "0" + secondsText;
        }
        String time = hours + ":" + minutesText + ":" + secondsText;
        // TODO add game data
        return time;
    }

    /**
     * Check if the game was won.
     */
    public boolean isWon() {
        // TODO
        return false;
    }

        /**
     * Check if the game was lost.
     */
    public boolean isLost() {
        // TODO
        return true;
    }

}
