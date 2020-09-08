package JosephSmith;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class TechDirectLoginDialogController implements Initializable {
    @FXML
    public Button loginCancelButton;
    public Button loginButton;
    public TextField wwidField;
    public PasswordField techDirectPassField;
    public Thread warrantyThread;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    //Extract user credentials and call performWarranty method from main controller
    public void performWarranty() throws IOException, InterruptedException {

        //Get main controller resource
        FXMLLoader loader = new FXMLLoader(getClass().getResource("mainUIGridPane.fxml"));

        //Load parent
        Parent root = (Parent) loader.load();

        //Get controller
        MainUIGridPaneController mainController = loader.getController();

        //Call performWarranty on separate thread
            warrantyThread = new Thread(() -> {
                String techDirectEmail = wwidField.getText() + "@cummins.com";
                String techDirectPass = techDirectPassField.getText();
                mainController.performWarranty(techDirectEmail, techDirectPass);
            });
        warrantyThread.start();

        //Close login dialog
        Stage stage = (Stage) loginButton.getScene().getWindow();
        stage.close();

        warrantyThread.join();


    }

    //Close login dialog when cancel is clicked
    public void returnToWarrantyForm(){
        Stage stage = (Stage) loginCancelButton.getScene().getWindow();
        stage.close();
    }

}
