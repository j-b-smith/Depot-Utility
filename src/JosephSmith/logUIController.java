package JosephSmith;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

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
    public TableView<StatusEntry> statusTable;
    public TableColumn<Object, Object> serviceTag;
    public TableColumn<Object, Object> status;

    @FXML
    Button returnToMainButton;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        populateLogTable();
        Thread statusThread = new Thread(this::populateStatusTable);
        statusThread.start();
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

    /*
   Get the status of the last 100 machines in the dispatch summary and display in status table
   Runs every time the main UI is reloaded, run in intervals? Save Observable list and repopulate when main UI is loaded?
   Set target page to dispatch list to save time, it will redirect to main to login, then go directly to dispatch list
   This can also work for first login of warranties, go directly to dreate dispatch page
    */
    public void populateStatusTable() {
        //Create WebDriver object
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\jsmit\\chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--disable-gpu");
        WebDriver driver = new ChromeDriver(options);


        // Navigate to Dell Tech Direct Website
        driver.get("https://www.dell.com/Identity/global/Login/\\\n" +
                "\t\t7b0aec96-623a-4393-b6d0-a595d7d897ef?Ctx=bXAR5%2FJWR\\\n" +
                "\t\tYQAwJHNtR9DwN92sZVPfDGk%2BXHHHAdnENeURzRs12i%2FKkH46J\\\n" +
                "\t\tTWGwRs&feir=1");

        //Input Email Address and Password
        driver.findElement(By.id("EmailAddress")).sendKeys("rv355@cummins.com");
        driver.findElement(By.id("Password")).sendKeys("Kayla0626!$");

        //Sign in
        WebElement signIn = new WebDriverWait(driver, 30).until(
                ExpectedConditions.elementToBeClickable(By.id("sign-in-button")));
        signIn.click();

        //Navigate to Self-Dispatch page
        WebElement selfDispatch = new WebDriverWait(driver, 30).until(
                ExpectedConditions.elementToBeClickable(By.id("_ctl0_BodyContent__ctl2__ctl0_btnVisit")));
        selfDispatch.click();

        //Navigate to dispatch summary
        //Navigate to Self-Dispatch page
        WebElement dispatchSummary = new WebDriverWait(driver, 30).until(
                ExpectedConditions.elementToBeClickable(By.id("_ctl0_BodyContent_common_boxes_rptBoxes__ctl0_btnBox")));
        dispatchSummary.click();

        //Store status entries in list
        ArrayList<StatusEntry> tempStatusData = new ArrayList<>();

        //Create status entries for first page of dispatch summary
        for (int i = 0; i < 100; i++) {
            String status = driver.findElement(By.cssSelector("#_ctl0_BodyContent__ctl0_lvgA9578C48_adg_dgiRow" + i + " > td:nth-child(3)")).getText();
            if (status.toLowerCase().equals("under review") || status.toLowerCase().equals("unable to process") || status.toLowerCase().equals("service complete")) {
                StatusEntry entry = new StatusEntry(
                        driver.findElement(By.cssSelector("#_ctl0_BodyContent__ctl0_lvgA9578C48_adg_dgiRow" + i + " > td:nth-child(4)")).getText(), status);
                tempStatusData.add(entry);
            }
        }

        //Close chromedriver
        driver.close();

        //Convert data to observable list
        ObservableList<StatusEntry> statusData = FXCollections.observableArrayList(tempStatusData);


        //Create cell factories for table
        serviceTag.setCellValueFactory(
                new PropertyValueFactory<>("serviceTag"));
        status.setCellValueFactory(
                new PropertyValueFactory<>("status")
        );

        //Populate table
        statusTable.setItems(statusData);

    }
}

