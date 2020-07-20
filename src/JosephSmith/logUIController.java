package JosephSmith;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class logUIController implements Initializable {

    public TableColumn<LogEntry, String> dateColumn;
    public TableColumn<LogEntry, String> requestNumberColumn;
    public TableColumn<LogEntry, String> serviceTagColumn;
    public TableColumn<LogEntry, String> modelColumn;
    public TableColumn<LogEntry, String> machineIssueColumn;
    public TableColumn<LogEntry, String> partNeededColumn;
    public HBox logUIButtonHBox;
    public TableView<LogEntry> logUITableView;
    public Button displayPieChart;
    public TextField logSearch;

    @FXML
    Button returnToMainButton;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        AnchorPane.setBottomAnchor(logUIButtonHBox, 20.0);
        populateLogTable();
    }

    public void populateLogTable(){
        //Connect to database
        DatabaseHelper database = new DatabaseHelper();
        database.connect();

        //Get Log row count
        int logRowCount = database.getRowCount("LogSheet");

        //Create temporary arrayList for LogEntries
        ArrayList<LogEntry> temp = new ArrayList<>();

        //Populate arrayList from database
        for (int i = 0; i < logRowCount; i++) {
            LogEntry row = database.getLogRow(i);
            temp.add(row);
        }

        //Pass arrayList into observable list
        ObservableList<LogEntry> logData = FXCollections.observableList(temp);

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

        database.closeConnection();
    }


    @FXML
    private void displayMainUI() throws IOException {
        try {
            Stage stage = (Stage) returnToMainButton.getScene().getWindow();
            GridPane root = FXMLLoader.load(getClass().getResource("mainUI.fxml"));
            Scene scene = new Scene(root, 1200, 700);
            stage.setScene(scene);
        } catch (NullPointerException e){
            e.printStackTrace();
        }

    }

    @FXML
    private void displayLogPieChart() throws IOException {
        try {
            Stage stage = (Stage) displayPieChart.getScene().getWindow();
            GridPane root = FXMLLoader.load(getClass().getResource("logPieChart.fxml"));
            Scene scene = new Scene(root, 1200, 700);
            stage.setScene(scene);
        } catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    public void displayLogSearchResult() {

        //Get text from search box for criteria
        String searchCriteria = logSearch.getText();

        //Verify search criteria is present
        if (searchCriteria != null) {
            //Connect to database
            DatabaseHelper database = new DatabaseHelper();
            database.connect();

            //Get Log row count
            int logRowCount = database.getRowCount("LogSheet");

            //Create temporary arrayList for LogEntries
            ArrayList<LogEntry> tempLogData = new ArrayList<>();

            //Populate arrayList from database
            for (int i = 0; i < logRowCount; i++) {
                LogEntry row = database.getLogRow(i);

                //Get string value and add to log data if it contains search criteria
                String logEntry = database.logEntryToString(row);
                if (logEntry.toLowerCase().contains(searchCriteria.toLowerCase())){
                    tempLogData.add(row);
                }
            }

            //Pass arrayList into observable list
            ObservableList<LogEntry> logData = FXCollections.observableList(tempLogData);

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
            database.closeConnection();
        }
    }
}

