package sokoban.file;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import application.Main.SokobanPropertyType;
import properties_manager.PropertiesManager;
import sokoban.ui.SokobanUI;

public class SokobanFileLoader {

    public static String loadTextFile(String textFile) throws IOException {
        // ADD THE PATH TO THE FILE
        PropertiesManager props = PropertiesManager.getPropertiesManager();
        textFile = props.getProperty(SokobanPropertyType.DATA_PATH) + textFile;

        // WE'LL ADD ALL THE CONTENTS OF THE TEXT FILE TO THIS STRING
        String textToReturn = "";

        // OPEN A STREAM TO READ THE TEXT FILE
        FileReader fr = new FileReader(textFile);
        BufferedReader reader = new BufferedReader(fr);

        // READ THE FILE, ONE LINE OF TEXT AT A TIME
        String inputLine = reader.readLine();
        while (inputLine != null) {
            // APPEND EACH LINE TO THE STRING
            textToReturn += inputLine + "\n";

            // READ THE NEXT LINE
            inputLine = reader.readLine();
        }

        // RETURN THE TEXT
        return textToReturn;
    }
}
