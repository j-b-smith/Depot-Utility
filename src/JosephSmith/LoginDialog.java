package JosephSmith;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginDialog {
    public TextField loginEmail;
    public PasswordField loginPassword;
    public Label alertLabel;
    public Button loginSubmitButton;

    public void processCredentials(ActionEvent actionEvent) {
        if (loginEmail.getText() != null && loginPassword.getText() != null){
            try {
                Stage stage = (Stage) loginSubmitButton.getScene().getWindow();
                GridPane root = FXMLLoader.load(getClass().getResource("mainUI.fxml"));
                Scene scene = new Scene(root, 1200, 700);
                stage.setScene(scene);
            } catch (NullPointerException | IOException e){
                e.printStackTrace();
            }
        } else alertLabel.setText("Please fill in all required fields");
    }
}
