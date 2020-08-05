package JosephSmith;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;

public class FedExShippingUI implements Initializable {

    public TextField taskNumber;
    public TextArea serviceNowNotesArea;
    public ListView<String> taskListView;
    public TextField firstName;
    public TextField lastName;
    public TextField addressLineOne;
    public TextField addressLineTwo;
    public TextField customerCity;
    public TextField customerState;
    public TextField customerZip;
    public Button submitFedexQueue;
    public WebDriver driver;
    public TableColumn<Object, Object> sctaskNumColumn;
    public TableColumn<Object, Object> customerNameColumn;
    public TableColumn<Object, Object> sctaskNotesColumn;
    public ListView<String> sctaskListView;
    public TableView<SCTask> scTaskTable;
    ArrayList<String> taskNumList = new ArrayList<>();
    ArrayList<SCTask> taskObjectList = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    public void addToQueue(){

        taskNumList.add(taskNumber.getText());
        ObservableList<String> taskNumData = FXCollections.observableArrayList(taskNumList);
        sctaskListView.setItems(taskNumData);

    }

    public void getTaskInfo() throws InterruptedException {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\jsmit\\chromedriver.exe");
        driver = new ChromeDriver();
        driver.get("https://cummins.service-now.com");


        driver.findElement(By.id("i0116")).sendKeys("rv355@cummins.com");

        Thread.sleep(1000);

        driver.findElement(By.id("idSIButton9")).click();

        WebElement passwordInput = new WebDriverWait(driver, 30).until(
                ExpectedConditions.visibilityOfElementLocated(By.id("passwordInput")));
        passwordInput.sendKeys("Kayla95!$");

        WebElement submitButton = new WebDriverWait(driver, 30).until(
                ExpectedConditions.visibilityOfElementLocated(By.id("submitButton")));
        submitButton.click();

        Thread.sleep(20000);

        for (String scTask : taskNumList) {

            try {
                //Input task number into search bar and press enter
                WebElement searchBar = new WebDriverWait(driver, 30).until(
                        ExpectedConditions.presenceOfElementLocated(By.id("sysparm_search")));
                searchBar.clear();
                searchBar.sendKeys("SCTASK" + scTask);
                searchBar.sendKeys(Keys.ENTER);
            } catch (NoSuchElementException e){
                e.printStackTrace();
                driver.findElement(By.id("sysparm_search")).clear();
                driver.findElement(By.id("sysparm_search")).sendKeys("SCTASK" + scTask);
                driver.findElement(By.id("sysparm_search")).sendKeys(Keys.ENTER);
            }

            Thread.sleep(2000);
            driver.switchTo().frame(driver.findElement(By.name("gsft_main")));
            //Get customer name from "Requested For" field
            WebElement customerNameField = driver.findElement(By.name("sys_display.sc_task.request_item.u_requested_for"));
            String customerName = customerNameField.getAttribute("value");

            //Get the contents of the notes field
            Thread.sleep(1000);
            WebElement ticketNotesField = driver.findElement(By.name("ni.VEc93f9a4edb56d4d0d7571d891396199a"));
            ArrayList<String> ticketNotesList = new ArrayList<>(Arrays.asList(ticketNotesField.getText().split("[*]+")));
            String ticketNotes = ticketNotesList.get(ticketNotesList.size() -1);

            taskObjectList.add(new SCTask(scTask, customerName, ticketNotes));

            driver.switchTo().defaultContent();
        }
        driver.close();

        ObservableList<SCTask> taskData = FXCollections.observableArrayList(taskObjectList);

        sctaskNumColumn.setCellValueFactory(
                new PropertyValueFactory<>("taskNumber"));
        customerNameColumn.setCellValueFactory(
                new PropertyValueFactory<>("customerName"));
        sctaskNotesColumn.setCellValueFactory(
                new PropertyValueFactory<>("taskNotes"));

        scTaskTable.setItems(taskData);
    }

    /*
    public void initiateFedexLabels() throws IOException, InterruptedException {

        Runtime runtime = Runtime.getRuntime();
        runtime.exec("C:\\Program Files (x86)\\Windows Application Driver\\WinAppDriver.exe");
        //runtime.exec("FedEx.Gsm.Cafe.ApplicationEngine.Gui.exe");

        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        desiredCapabilities.setCapability("app", "C:\\Program Files (x86)\\FedEx\\ShipManager\\BIN\\FedEx.Gsm.Cafe.ApplicationEngine.Gui.exe");
        WindowsDriver appDriver = new WindowsDriver(new URL("http://127.0.0.1:4723/"), desiredCapabilities);

        Thread.sleep(15000);

        appDriver.switchTo();
        appDriver.findElement(By.name("OK")).click();

        Thread.sleep(5000);
        runtime.exit(0);
    }

     */
}
