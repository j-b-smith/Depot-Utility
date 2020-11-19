package JosephSmith.view;

import JosephSmith.API.DellAPI;
import JosephSmith.API.DispatchAPI;
import JosephSmith.model.InputValidator;
import jakarta.xml.soap.SOAPException;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class TechDirectLoginDialogController implements Initializable {
    @FXML
    public Button loginCancelButton;
    @FXML
    public Button loginButton;
    @FXML
    public PasswordField techDirectPassField;
    @FXML
    public Label alertLabel;
    @FXML
    public TextField techDirectWwidField;

    public HomePageController mainController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setEventListeners();
    }

    public void setEventListeners(){
        //Set focus to password field on enter
        techDirectWwidField.setOnKeyPressed(e -> {
            if (e.getCode().equals(KeyCode.ENTER)){
                techDirectPassField.requestFocus();
            }
        });

        //Initiate login on enter
        techDirectPassField.setOnKeyPressed(e -> {
            if (e.getCode().equals(KeyCode.ENTER)) {
                loginButton.fire();
            }
        });
    }

    //Extract user credentials and call performWarranty method from main controller
    public void performWarranty() throws IOException, SOAPException {

        //Validate
        if (InputValidator.techDirectLoginIsValid(this)) {

            //Call performWarranty on separate thread
            String techDirectEmail = techDirectWwidField.getText() + "@cummins.com";
            String techDirectPass = techDirectPassField.getText();

            if (DispatchAPI.checkLogin(techDirectEmail, techDirectPass)){
                Stage stage = (Stage) loginButton.getScene().getWindow();
                stage.close();
                mainController.submitDispatches(techDirectEmail, techDirectPass);

                //Close login dialog
            } else {
                alertLabel.setText("*The Login and/or Password is Invalid");
                alertLabel.setVisible(true);
            }

        }
    }

    //Close login dialog when cancel is clicked
    public void returnToWarrantyForm(){
        Stage stage = (Stage) loginCancelButton.getScene().getWindow();
        stage.close();
    }

}
