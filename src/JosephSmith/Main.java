package JosephSmith;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{

        Parent root = FXMLLoader.load(getClass().getResource("mainUIGridPane.fxml"));
        primaryStage.setTitle("Depot Utility");

        //Create main window scene
        Scene mainUI = new Scene(root, 1300, 750);

        URL stylesheetUrl = getClass().getResource("css.css");
        mainUI.getStylesheets().add(stylesheetUrl.toExternalForm());

        //Set main window scene
        primaryStage.setScene(mainUI);
        //
        // primaryStage.setResizable(false);
        primaryStage.show();


    }

    public static void main(String[] args) { launch(args);}}
