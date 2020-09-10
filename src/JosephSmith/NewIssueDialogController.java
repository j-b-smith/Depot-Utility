package JosephSmith;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class NewIssueDialogController implements Initializable {
    public TextField newMachineIssue;
    public TextArea newTroubleshootingSteps;
    public ComboBox<String> newPartNeededComboBox;
    public Label alertLabel;
    public Button addNewMachineIssueButton;
    public TextField addNewPartInput;
    public Label addNewPartLabel;
    public ArrayList<String> partsList = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        populatePartNeededComboBox();
    }

    public void addNewMachineIssue(){
        DatabaseHelper database = new DatabaseHelper();
        database.connect();

        //Get input validator
        InputValidator validator = new InputValidator(this);

        if (validator.addNewIssueValidation()) {
            database.addNewRowToIssueDescriptions(newMachineIssue.getText(),
                    newTroubleshootingSteps.getText(), newPartNeededComboBox.getValue());

            database.closeConnection();

            //Clear fields and hide New Part fields
            newMachineIssue.clear();
            newTroubleshootingSteps.clear();
            newPartNeededComboBox.getSelectionModel().clearSelection();
        }


    }

    /*
    Get unique part values from the DescriptionSheet table and populate the combo box with parts
     */
    public void populatePartNeededComboBox(){
        //Connect to database
        DatabaseHelper database = new DatabaseHelper();
        database.connect();

        //Create lists from description sheet
        partsList = database.createUniqueValueList("Part_Needed", "IssueDescriptions");


        //Populate Machine Issue Combo Box
        ObservableList<String> partsComboList = FXCollections.observableList(partsList);
        newPartNeededComboBox.setItems(partsComboList);

        //Close database connection
        database.closeConnection();
    }
}
