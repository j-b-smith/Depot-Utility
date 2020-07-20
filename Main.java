package JosephSmith;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

public class Main extends Application {
    ArrayList<String> credentials;

    @Override
    public void start(Stage primaryStage) throws Exception{

        //Get login credentials
        try {
            credentials = showLoginDialog();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Parent root = FXMLLoader.load(getClass().getResource("mainUI.fxml"));
        //root.setStyle("-fx-background-color: lightgray;");

        //Debug
        //root.setStyle("-fx-background-color: yellow;");
        primaryStage.setTitle("Dell Warranty Utility");

        //Create main window scene
        Scene mainUI = new Scene(root, 1200, 700);

        //Set main window scene
        primaryStage.setScene(mainUI);
        primaryStage.show();

    }

    //Display the dialog when "Add New Machine Issue" is clicked in mainUI
    public ArrayList<String> showLoginDialog() throws IOException {

        //Create dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Dell Tech Direct Login");

        //Get resource for loader and set dialog
        FXMLLoader loginDialogLoader = new FXMLLoader(getClass().getResource("loginDialog.fxml"));
        dialog.setDialogPane(loginDialogLoader.load());

        //Get controller for dialog, add buttons and display dialog
        LoginDialog controller = loginDialogLoader.getController();
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);

        //Get the result from the dialog button
        Optional<ButtonType> dialogResult = dialog.showAndWait();
        //List to store login information
        ArrayList<String> credentials = new ArrayList<>();

        //Check for null values
        if (controller.loginEmail.getText() != null &&
                controller.loginPassword.getText() != null) {

            //If OK button is clicked, write to database
            if (dialogResult.isPresent() && dialogResult.get() == ButtonType.OK) {
                String email = controller.loginEmail.getText();
                String password = controller.loginPassword.getText();
                credentials.add(email);
                credentials.add(password);
            }
        }
        return credentials;
    }

    public static void main(String[] args) { launch(args);}}
