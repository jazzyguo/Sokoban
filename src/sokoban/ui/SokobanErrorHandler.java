package sokoban.ui;

import properties_manager.PropertiesManager;
import application.Main.SokobanPropertyType;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class SokobanErrorHandler {

    private Stage ui;

    public SokobanErrorHandler(Stage initUI) {
        ui = initUI;
    }

    /**
     * This method provides all error feedback. It gets the feedback text, which
     * changes depending on the type of error, and presents it to the user in a
     * dialog box.
     *
     * @param errorType Identifies the type of error that happened, which allows
     * us to get and display different text for different errors.
     */
    public void processError(SokobanPropertyType errorType) {
        // GET THE FEEDBACK TEXT
        PropertiesManager props = PropertiesManager.getPropertiesManager();
        String errorFeedbackText = props.getProperty(errorType);

        // NOTE THAT WE'LL USE THE SAME DIALOG TITLE FOR ALL ERROR TYPES
        String errorTitle = props.getProperty(SokobanPropertyType.ERROR_DIALOG_TITLE_TEXT);

        // POP OPEN A DIALOG TO DISPLAY TO THE USER
        //JOptionPane.showMessageDialog(window, errorFeedbackText, errorTitle, JOptionPane.ERROR_MESSAGE);
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(ui);
        dialogStage.setTitle(errorTitle);
        VBox vbox = new VBox();
        vbox.setSpacing(10.0);
        Label errLabel = new Label(errorFeedbackText);
        Button errButton = new Button("confirm");
        vbox.getChildren().addAll(errLabel, errButton);

        Scene scene = new Scene(vbox, 50, 30);
        dialogStage.setScene(scene);
        dialogStage.show();
    }
}
