package JosephSmith.view;

import JosephSmith.Database.Database;
import JosephSmith.model.InputValidator;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

public class NewIssueController implements Initializable {

    @FXML
    public TextField newMachineIssue;
    @FXML
    public TextArea newTroubleshootingSteps;
    @FXML
    public ComboBox<String> newPartNeededComboBox;
    @FXML
    public Label alertLabel;
    @FXML
    public Button addNewMachineIssueButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        newPartNeededComboBox.setItems(Database.createUniqueValueList("Part_Needed", "IssueDescriptions"));
    }

    public void addNewMachineIssue(){

        if (InputValidator.newIssueFormIsValid(this)) {
            Database.addNewIssue(newMachineIssue.getText(),
                    newTroubleshootingSteps.getText(), newPartNeededComboBox.getValue());

            //Clear fields and hide New Part fields
            newMachineIssue.clear();
            newTroubleshootingSteps.clear();
            newPartNeededComboBox.getSelectionModel().clearSelection();
        }


    }

}
