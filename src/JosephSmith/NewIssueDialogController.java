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

        //Update combo box
        //partsList.add(addNewPartInput.getText());
        //ObservableList<String> partsComboList = FXCollections.observableArrayList(partsList);

        //Check if add new part is selected and write information to description sheet
        if (newPartNeededComboBox.getValue().equals("Add New Part")) {
            database.addNewRowToDescriptionSheet(newMachineIssue.getText(),
                    newTroubleshootingSteps.getText(), addNewPartInput.getText());
        } else {
            database.addNewRowToDescriptionSheet(newMachineIssue.getText(),
                    newTroubleshootingSteps.getText(), newPartNeededComboBox.getValue());
        }

        database.closeConnection();


        //Clear fields and hide New Part fields
        newMachineIssue.clear();
        newTroubleshootingSteps.clear();
        newPartNeededComboBox.getSelectionModel().clearSelection();
        addNewPartLabel.setVisible(false);
        addNewPartInput.setVisible(false);
        addNewPartInput.setDisable(true);


    }

    /*
    Get unique part values from the DescriptionSheet table and populate the combo box with parts
     */
    public void populatePartNeededComboBox(){
        //Connect to database
        DatabaseHelper database = new DatabaseHelper();
        database.connect();

        //Create lists from description sheet
        partsList = database.createUniqueValueList("Part_Needed", "DescriptionSheet");

        //Add option to input a new part
        partsList.add("Add New Part");

        //Populate Machine Issue Combo Box
        ObservableList<String> partsComboList = FXCollections.observableList(partsList);
        newPartNeededComboBox.setItems(partsComboList);

        //Close database connection
        database.closeConnection();
    }

    /*
    Display New Part label and Input if the combo box selection is
    Hide the Label and Input if anything else is selected
     */
    public void partComboBoxChange(){
        if (newPartNeededComboBox.getValue().equals("Add New Part")){
            addNewPartLabel.setVisible(true);
            addNewPartInput.setVisible(true);
            addNewPartInput.setDisable(false);
        } else {
            addNewPartLabel.setVisible(false);
            addNewPartInput.setVisible(false);
            addNewPartInput.setDisable(true);
        }
    }
}
