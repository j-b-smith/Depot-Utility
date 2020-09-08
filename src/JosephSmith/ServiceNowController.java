package JosephSmith;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class ServiceNowController implements Initializable {

    public TextField taskNumber;
    public WebDriver driver;
    public TableColumn<Object, Object> sctaskNumColumn;
    public TableColumn<Object, Object> sctaskNotesColumn;
    public TableView<SCTask> scTaskTable;
    public TextArea workNotes;
    public PasswordField techPassword;
    public TextField trackingNumber;
    public TableColumn<Object, Object> sctaskTrackingColumn;
    public ArrayList<SCTask> taskObjectList = new ArrayList<>();
    public TextField userWWID;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        //Allow multiple row selections in table
        scTaskTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    public void addToQueue(){

        //Create new SCTask object and add to list
        taskObjectList.add(new SCTask(taskNumber.getText(), trackingNumber.getText(), workNotes.getText()));

        //Cast list to observable list
        ObservableList<SCTask> taskData = FXCollections.observableArrayList(taskObjectList);

        //Set cell factories
        sctaskNumColumn.setCellValueFactory(
                new PropertyValueFactory<>("taskNumber"));
        sctaskTrackingColumn.setCellValueFactory(
                new PropertyValueFactory<>("trackingNumber"));
        sctaskNotesColumn.setCellValueFactory(
                new PropertyValueFactory<>("workNotes"));

        //Populate table
        scTaskTable.setItems(taskData);

        onAddTaskPressed();

    }

    /*
    Remove selected tasks from the table view
     */
    public void removeTask(){
        int numTasks = scTaskTable.getSelectionModel().getSelectedItems().size();

        for (int i = 0; i < numTasks; i++) {
            SCTask selectedItem = scTaskTable.getSelectionModel().getSelectedItem();
            scTaskTable.getItems().remove(selectedItem);
            taskObjectList.remove(selectedItem);
        }
    }

    @FXML
    public void onTaskKeyEnter(javafx.scene.input.KeyEvent keyEvent) {
        if (keyEvent.getCode().equals(KeyCode.ENTER)) {
            trackingNumber.requestFocus();
        }
    }

    public void onAddTaskPressed(){
        taskNumber.clear();
        trackingNumber.clear();
        workNotes.clear();
        taskNumber.requestFocus();
    }

    public void onCloseTaskComplete(){
        userWWID.clear();
        techPassword.clear();
        taskObjectList.clear();
        ObservableList<SCTask> taskData = FXCollections.observableArrayList(taskObjectList);
        scTaskTable.setItems(taskData);
    }

    public void completeTasksButton() throws IOException, InterruptedException {
        openLoginDialog();
    }

    public void openLoginDialog() throws IOException, InterruptedException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ServiceNowLoginDialog.fxml"));
        Parent root = (Parent) loader.load();
        Stage stage = new Stage();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Service Now Login");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();

    }

    public void completeTasks(String email, String password, String name) throws InterruptedException {


        //Create ChromeDriver object
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\jsmit\\chromedriver.exe");
        driver = new ChromeDriver();

        //Navigate to service now
        driver.get("https://cummins.service-now.com");

        //Login Process (May need changed)
        driver.findElement(By.id("i0116")).sendKeys(email);
        Thread.sleep(1000);

        driver.findElement(By.id("idSIButton9")).click();

        WebElement passwordInput = new WebDriverWait(driver, 30).until(
                ExpectedConditions.visibilityOfElementLocated(By.id("passwordInput")));
        passwordInput.sendKeys(password);

        WebElement submitButton = new WebDriverWait(driver, 30).until(
                ExpectedConditions.visibilityOfElementLocated(By.id("submitButton")));
        submitButton.click();

        //Iterate through SCTask Object List
        for (SCTask task : taskObjectList) {

            try {
                //Input task number into search bar and press enter
                WebElement searchBar = new WebDriverWait(driver, 240).until(
                        ExpectedConditions.presenceOfElementLocated(By.id("sysparm_search")));
                searchBar.clear();

                if (task.taskNumber.toLowerCase().contains("sctask")){
                    searchBar.sendKeys(task.taskNumber);
                } else {
                    searchBar.sendKeys("SCTASK" + task.taskNumber);
                }

                Thread.sleep(4000);
                searchBar.sendKeys(Keys.ENTER);
            } catch (NoSuchElementException e) {
                e.printStackTrace();
            }

            //Switch to ticket iFrame
            driver.switchTo().frame(driver.findElement(By.name("gsft_main")));

            Thread.sleep(3000);
            //Check tech name with assigned to name, change if necessary
            WebElement assignedToField = new WebDriverWait(driver, 30).until(
                    ExpectedConditions.visibilityOfElementLocated(By.id("sys_display.sc_task.assigned_to")));

            if (!assignedToField.getText().equals(name)) {
                //Clear assigned to field and input tech name
                assignedToField.clear();
                assignedToField.sendKeys(name);
                assignedToField.sendKeys(Keys.ENTER);
            }


            //Save tech assignment
            WebElement saveButton = new WebDriverWait(driver, 30).until(
                    ExpectedConditions.visibilityOfElementLocated(By.id("sysverb_update_and_stay")));
            saveButton.click();

            Thread.sleep(3000);

            //Change ticket state to "Closed Complete"
            Select ticketState = new Select(new WebDriverWait(driver, 30).until(
                    ExpectedConditions.visibilityOfElementLocated(By.id("sc_task.state"))));
            ticketState.selectByVisibleText("Closed Complete");

            /*
            Enter the tracking number and notes into the "closed notes field"
             */
            if (task.trackingNumber == null) {
                WebElement closeNotesField = new WebDriverWait(driver, 30).until(
                        ExpectedConditions.visibilityOfElementLocated(By.id("sc_task.close_notes")));
                closeNotesField.sendKeys(task.workNotes);
            } else if (task.workNotes == null) {
                WebElement closeNotesField = new WebDriverWait(driver, 30).until(
                        ExpectedConditions.visibilityOfElementLocated(By.id("sc_task.close_notes")));
                closeNotesField.sendKeys("Your tracking number is: " + task.trackingNumber);
            } else {
                WebElement closeNotesField = new WebDriverWait(driver, 30).until(
                        ExpectedConditions.visibilityOfElementLocated(By.id("sc_task.close_notes")));
                closeNotesField.sendKeys("Your tracking number is: " + task.trackingNumber + "\n" + task.workNotes);
            }

            //Save the ticket
            WebElement endSave = new WebDriverWait(driver, 30).until(
                    ExpectedConditions.elementToBeClickable(By.id("sysverb_update_and_stay")));
            endSave.click();

            //Switch to default frame
            driver.switchTo().defaultContent();
        }

        //Close Chrome Instance
        driver.close();

        onCloseTaskComplete();
    }
}
