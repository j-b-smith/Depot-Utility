package JosephSmith;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class NewIssueDialogController implements Initializable {
    public TextField newMachineIssue;
    public TextArea newTroubleshootingSteps;
    public ComboBox<String> newPartNeededComboBox;
    public Label alertLabel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        //Connect to database
        DatabaseHelper database = new DatabaseHelper();
        database.connect();

        //Create lists from description sheet
        ArrayList<String> partsList = database.createUniqueValueList("Part_Needed", "DescriptionSheet");

        //Populate Machine Issue Combo Box
        ObservableList<String> partsComboList = FXCollections.observableList(partsList);
        newPartNeededComboBox.setItems(partsComboList);

        //Close database connection
        database.closeConnection();
    }
}
