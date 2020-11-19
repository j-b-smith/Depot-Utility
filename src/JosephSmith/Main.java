package JosephSmith;

import JosephSmith.API.TechnicalSupportAPI;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.net.URL;

public class Main extends Application {

    public static URL stylesheetUrl;

    @Override
    public void start(Stage primaryStage) throws Exception{

        //Load root scene
        Parent root = FXMLLoader.load(getClass().getResource("view/HomePage.fxml"));

        //Set title
        primaryStage.setTitle("Warranty Utility");

        //Create main window scene
        Scene mainUI = new Scene(root, 1300, 750);

        //Load stylesheet
        URL stylesheetUrl = getClass().getResource("Resources/Stylesheets/WarrantyUtility.css");
        Main.stylesheetUrl = stylesheetUrl;

        mainUI.getStylesheets().add(stylesheetUrl.toExternalForm());

        //Set main window scene
        primaryStage.setScene(mainUI);

        //Show stage
        primaryStage.show();

    }

    public static void main(String[] args) { launch(args);}}
