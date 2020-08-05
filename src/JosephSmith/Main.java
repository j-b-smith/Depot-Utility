package JosephSmith;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{

        Parent root = FXMLLoader.load(getClass().getResource("MainUITabPane.fxml"));
        primaryStage.setTitle("Dell Warranty Utility");

        //Create main window scene
        Scene mainUI = new Scene(root, 1200, 700);

        //Set main window scene
        primaryStage.setScene(mainUI);
        primaryStage.show();

    }

    public static void main(String[] args) { launch(args);}}
