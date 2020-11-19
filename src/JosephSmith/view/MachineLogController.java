package JosephSmith.view;

import JosephSmith.Database.Database;
import JosephSmith.model.LogEntry;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ResourceBundle;

/** Handles all functionality for Log UI */
public class MachineLogController implements Initializable {

    @FXML
    public TableColumn<LogEntry, String> dateColumn;
    @FXML
    public TableColumn<LogEntry, String> requestNumberColumn;
    @FXML
    public TableColumn<LogEntry, String> serviceTagColumn;
    @FXML
    public TableColumn<LogEntry, String> modelColumn;
    @FXML
    public TableColumn<LogEntry, String> machineIssueColumn;
    @FXML
    public TableColumn<LogEntry, String> partNeededColumn;
    @FXML
    public TableView<LogEntry> logUITableView;
    @FXML
    public TextField logSearch;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        setCellFactories();
        setEventListeners();
        //Set table data
        logUITableView.setItems(Database.getLogMachines());
    }

    /** Set the cell factories for the machine log table*/
    public void setCellFactories(){

            //Create cell factories for machine log table
            dateColumn.setCellValueFactory(
                    new PropertyValueFactory<>("date"));
            requestNumberColumn.setCellValueFactory(
                    new PropertyValueFactory<>("requestNumber"));

            requestNumberColumn.setCellFactory(tc -> new TableCell<LogEntry, String>(){
                @Override
                protected void updateItem(String s, boolean b) {
                    super.updateItem(s, b);
                    if (b){
                        setText(null);
                    } else {
                        setText("Request Number");
                    }
                }
            });
            serviceTagColumn.setCellValueFactory(
                    new PropertyValueFactory<>("serviceTag"));

        serviceTagColumn.setCellFactory(tc -> new TableCell<LogEntry, String>(){
            @Override
            protected void updateItem(String s, boolean b) {
                super.updateItem(s, b);
                if (b){
                    setText(null);
                } else {
                    setText("Service Tag");
                }
            }
        });
            modelColumn.setCellValueFactory(
                    new PropertyValueFactory<>("model"));
            machineIssueColumn.setCellValueFactory(
                    new PropertyValueFactory<>("machineIssue"));

            //Allow machine issue column to wrap text
            machineIssueColumn.setCellFactory(tc -> {
                TableCell<LogEntry, String> cell = new TableCell<>();
                Text text = new Text();
                text.setFill(Color.WHITE);
                cell.setGraphic(text);
                cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
                text.wrappingWidthProperty().bind(machineIssueColumn.widthProperty().divide(1.1));
                text.textProperty().bind(cell.itemProperty());
                return cell;
            });
            partNeededColumn.setCellValueFactory(
                    new PropertyValueFactory<>("partNeeded"));

            //Allow part needed column to wrap text
            partNeededColumn.setCellFactory(tc -> {
                TableCell<LogEntry, String> cell = new TableCell<>();
                Text text = new Text();
                text.setFill(Color.WHITE);
                cell.setGraphic(text);
                cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
                text.wrappingWidthProperty().bind(machineIssueColumn.widthProperty().divide(1.1));
                text.textProperty().bind(cell.itemProperty());
                return cell;
            });


            
    }

    /** Set the event listeners */
    private void setEventListeners(){

        //Set key event for search field
        logSearch.setOnKeyReleased(e -> searchLog());
    }

    /** Search the machine log based on the search criteria */
    private void searchLog(){

        //Get text from search box for criteria
        String searchCriteria = logSearch.getText().trim();

        //Check if search field is blank
        if (!searchCriteria.equals("")) {

            //Set table items from search result
            logUITableView.setItems(Database.searchMachineLog(searchCriteria));
        } else {

            //Set table items to all log machines
            logUITableView.setItems(Database.getLogMachines());
        }
    }

}

