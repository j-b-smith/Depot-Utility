package JosephSmith;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import org.openqa.selenium.Keys;

import java.net.URL;
import java.util.*;

public class logUIController implements Initializable {

    public TableColumn<LogEntry, String> dateColumn;
    public TableColumn<LogEntry, String> requestNumberColumn;
    public TableColumn<LogEntry, String> serviceTagColumn;
    public TableColumn<LogEntry, String> modelColumn;
    public TableColumn<LogEntry, String> machineIssueColumn;
    public TableColumn<LogEntry, String> partNeededColumn;
    public TableView<LogEntry> logUITableView;
    public TextField logSearch;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        populateLogTable();
    }

    public void populateLogTable(){

            //Pass arrayList into observable list
            ObservableList<LogEntry> logData = FXCollections.observableList(DatabaseHelper.getLogMachines());

            //Create cell factories for table
            dateColumn.setCellValueFactory(
                    new PropertyValueFactory<>("date"));
            requestNumberColumn.setCellValueFactory(
                    new PropertyValueFactory<>("requestNumber"));
            serviceTagColumn.setCellValueFactory(
                    new PropertyValueFactory<>("serviceTag"));
            modelColumn.setCellValueFactory(
                    new PropertyValueFactory<>("model"));
            machineIssueColumn.setCellValueFactory(
                    new PropertyValueFactory<>("machineIssue"));
            partNeededColumn.setCellValueFactory(
                    new PropertyValueFactory<>("partNeeded"));

            //Set table data
            logUITableView.setItems(logData);

    }

    public void displayLogSearchResult(KeyEvent event) {


        if (event.getCode().equals(KeyCode.ENTER)) {
            //Get text from search box for criteria
            String searchCriteria = logSearch.getText();

            //Verify search criteria is present
            if (searchCriteria != null) {

                //Create temporary arrayList for LogEntries
                ArrayList<LogEntry> logSearchResults = new ArrayList<>();

                ObservableList<LogEntry> logMachines = DatabaseHelper.getLogMachines();

                //Populate arrayList from database
                for (LogEntry entry : logMachines) {
                    //Get string value and add to log data if it contains search criteria
                    String logEntry = DatabaseHelper.logEntryToString(entry);
                    if (logEntry.toLowerCase().contains(searchCriteria.toLowerCase())) {
                        logSearchResults.add(entry);
                    }
                }
                //Pass arrayList into observable list
                ObservableList<LogEntry> logData = FXCollections.observableList(logSearchResults);

                //Create cell factories for table
                dateColumn.setCellValueFactory(
                        new PropertyValueFactory<>("date"));
                requestNumberColumn.setCellValueFactory(
                        new PropertyValueFactory<>("requestNumber"));
                serviceTagColumn.setCellValueFactory(
                        new PropertyValueFactory<>("serviceTag"));
                modelColumn.setCellValueFactory(
                        new PropertyValueFactory<>("model"));
                machineIssueColumn.setCellValueFactory(
                        new PropertyValueFactory<>("machineIssue"));
                partNeededColumn.setCellValueFactory(
                        new PropertyValueFactory<>("partNeeded"));

                //Set table data
                logUITableView.setItems(logData);
            }
        }
    }


}

