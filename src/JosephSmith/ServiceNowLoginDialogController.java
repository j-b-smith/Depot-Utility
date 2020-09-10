package JosephSmith;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;

public class ServiceNowLoginDialogController  implements Initializable {
    public TextField serviceNowWwidField;
    public PasswordField serviceNowPassField;
    public Button loginButton;
    public Button loginCancelButton;
    public ComboBox<String> techNameComboBox;
    public Thread taskThread;
    public ArrayList<String> techNameList = new ArrayList<>(Arrays.asList("Tech1", "Tech2", "Tech3"));
    public Label alertLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //Populate techname combobox
        ObservableList<String> techNameData = FXCollections.observableArrayList(techNameList);
        techNameComboBox.setItems(techNameData);
    }

    public void completeTasks() throws IOException, InterruptedException {
        //Get service now controller resource
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ServiceNowUI.fxml"));

        //Load parent
        Parent root = (Parent) loader.load();

        //Get controller
        ServiceNowController controller = loader.getController();

        //Create input validator
        InputValidator validator = new InputValidator(this);

        //Validate
        if (validator.serviceNowLoginValidation()) {
            //Call complete tasks on separate thread
            taskThread = new Thread(() -> {
                String serviceNowEmail = serviceNowWwidField.getText() + "@cummins.com";
                String serviceNowPass = serviceNowPassField.getText();
                String techName = techNameComboBox.getSelectionModel().getSelectedItem();
                try {
                    controller.completeTasks(serviceNowEmail, serviceNowPass, techName);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            taskThread.start();
            //Close login dialog
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.close();

            taskThread.join();
        }

    }

    public void returnToServiceNowForm() {
        Stage stage = (Stage) loginCancelButton.getScene().getWindow();
        stage.close();
    }


}
