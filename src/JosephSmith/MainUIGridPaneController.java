package JosephSmith;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDrawer;
import com.jfoenix.controls.JFXHamburger;
import com.jfoenix.transitions.hamburger.HamburgerBackArrowBasicTransition;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class MainUIGridPaneController implements Initializable {
    public Button submitMachineButton;
    public Button initiateWarranty;
    @FXML
    public ComboBox<String> machineIssueComboBox;
    public TextField serviceTagTextField;
    public TextField serialNumberTextField;
    public Label serialNumberLabel;
    public Button removeListViewItem;
    public Label listViewCountLabel;
    public Label alertLabel;
    public TableView<WarrantyMachine> warrantyMachineTableView;
    public TableColumn<Object, Object> serviceTagColumn;
    public TableColumn<Object, Object> machineIssueColumn;
    public TableColumn<WarrantyMachine, String> troubleshootingStepsColumn;
    public TableColumn<Object, Object> partNeededColumn;
    public TableColumn<Object, Object> serialNumberColumn;
    public JFXDrawer menuDrawer;
    public BorderPane mainUIBorderPane;
    public JFXHamburger hamburger;
    public JFXButton serviceNowButton;
    public JFXButton warrantyHistory;
    public GridPane warrantyFormGridPane;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        setMenuDrawer();
        populateMachineIssueComboBox();
        populateMachineListView();
        warrantyMachineTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    //Set the transition and content for the menu drawer
    public void setMenuDrawer(){
        mainUIBorderPane.setLeft(null);
        HamburgerBackArrowBasicTransition burgerTransition = new HamburgerBackArrowBasicTransition(hamburger);
        burgerTransition.setRate(-1);
        hamburger.addEventHandler(MouseEvent.MOUSE_CLICKED, (event -> {
            burgerTransition.setRate(burgerTransition.getRate() * -1);
            burgerTransition.play();


            if (menuDrawer.isOpened()){
                menuDrawer.close();
                mainUIBorderPane.setLeft(null);
            } else {
                menuDrawer.open();
                mainUIBorderPane.setLeft(menuDrawer);
            }
        }));
    }

    //Event functions for menu buttons
    public void switchToServiceNow() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ServiceNowUI.fxml"));
        Parent root = loader.load();
        mainUIBorderPane.setCenter(root);
    }

    public void switchToWarrantyForm() {
        mainUIBorderPane.setCenter(warrantyFormGridPane);
    }

    public void switchToMachineLog() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("logSheetUI.fxml"));
        Parent root = loader.load();
        mainUIBorderPane.setCenter(root);
    }

    public void switchToVisualLog() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("logPieChart.fxml"));
        Parent root = loader.load();
        mainUIBorderPane.setCenter(root);
    }

    public void switchToAddNewIssueForm() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("newIssueDialog.fxml"));
        Parent root = loader.load();
        mainUIBorderPane.setCenter(root);
    }


    /*
    Run the warranty process on a separate thread to avoid UI stall
    Clear the list view
     */
    public void initiateWarrantyButton() throws IOException, InterruptedException {
        openLoginDialog();
    }

    /*
    Create and display login dialog
     */
    public void openLoginDialog() throws IOException, InterruptedException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("TechDirectloginDialogUI.fxml"));
        Parent root = loader.load();
        Stage stage = new Stage();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Dell Tech Direct Login");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();

        //Get controller
        TechDirectLoginDialogController loginController = loader.getController();
        loginController.warrantyThread.join();
        populateMachineListView();
    }

    /*
    Write the value to the warranty machine table
    Clear the service tag field
    Clear the battery serial number field
    Set focus to the service tag field
    Populate the list view
     */
    @FXML
    public void submitMachineButton() {

            //Validate warranty form information
            InputValidator validator = new InputValidator(this);
            boolean isValid = validator.warrantyFormValidation();

            if (isValid) {
                writeWarrantyMachineTable();
                machineIssueComboBox.getSelectionModel().clearSelection();
                serviceTagTextField.clear();
                serialNumberTextField.clear();
                populateMachineListView();
            }
    }



    //Set focus to combo box when enter is pressed on service tag field
    @FXML
    public void onServiceTagInputEnter() {
        machineIssueComboBox.requestFocus();
    }

    //Set focus to submit machine button when enter is pressed on combo box
    @FXML
    public void onComboKeyEnter(javafx.scene.input.KeyEvent keyEvent) {
        if (keyEvent.getCode().equals(KeyCode.ENTER)) {
            submitMachineButton.requestFocus();
        }
    }

    //Event function for serial number label
    public void onComboBoxChange() {
        if (machineIssueComboBox.getValue() != null) {
            if (machineIssueComboBox.getValue().equals("Battery Swollen") || machineIssueComboBox.getValue().equals("Battery Not Charging / Holding Charge") || machineIssueComboBox.getValue().contains("Monitor")) {
                serialNumberLabel.setVisible(true);
                serialNumberTextField.setVisible(true);
                serialNumberTextField.setDisable(false);
            } else {
                serialNumberLabel.setVisible(false);
                serialNumberTextField.setVisible(false);
                serialNumberTextField.setDisable(true);
            }
        }
    }

    /*
    Retrieve the Warranty machine table information from the database
    Use the information to populate the list view
     */
    public void populateMachineListView() {

        //Get database connection
        DatabaseHelper database = new DatabaseHelper();
        database.connect();

        //reindex warranty machine table
        database.reIndexTable("WarrantyMachines");

        //Get warranty machines from database
        ArrayList<WarrantyMachine> listViewMachineList = database.getWarrantyMachines();

        //Convert object list to Observable list

        ObservableList<WarrantyMachine> warrantyMachineListViewData = FXCollections.observableArrayList(listViewMachineList);

        if (warrantyMachineListViewData.size() > 0) {
            //Set machine quantity label
            listViewCountLabel.setText("Qty: " + warrantyMachineListViewData.size());
        } else {
            listViewCountLabel.setText("");
        }

        //Set cell factories for table
        serviceTagColumn.setCellValueFactory(
                new PropertyValueFactory<>("serviceTag"));
        machineIssueColumn.setCellValueFactory(
                new PropertyValueFactory<>("machineIssue"));
        troubleshootingStepsColumn.setCellValueFactory(
                new PropertyValueFactory<>("troubleshootingSteps"));
        partNeededColumn.setCellValueFactory(
                new PropertyValueFactory<>("partNeeded"));
        serialNumberColumn.setCellValueFactory(
                new PropertyValueFactory<>("serialNumber"));

        //Set List View data and populate
        warrantyMachineTableView.setItems(warrantyMachineListViewData);

        database.closeConnection();
    }

    /*
    Remove selected items from list view
    Remove selected items form the warranty machine table in database
     */
    public void removeFromListView() {

        //Get database connection
        DatabaseHelper database = new DatabaseHelper();
        database.connect();

        //Remove selected items from WarrantyMachines Table and List View
        ObservableList<WarrantyMachine> selectedWarrantyMachines, currentMachinesData;
        selectedWarrantyMachines = warrantyMachineTableView.getSelectionModel().getSelectedItems();
        currentMachinesData = warrantyMachineTableView.getItems();

        //Remove from warranty machine table
        database.removeRowsFromWarrantyMachines(selectedWarrantyMachines);

        //Update list view
        currentMachinesData.removeAll(selectedWarrantyMachines);
        warrantyMachineTableView.setItems(currentMachinesData);

        //Set machine quantity label
        if (currentMachinesData.size() > 0) {
            listViewCountLabel.setText("Qty: " + currentMachinesData.size());
        } else listViewCountLabel.setText("");
        database.closeConnection();
    }

    /*
    Get the machine issues from Description table
    Populate machine issue combo box
     */
    public void populateMachineIssueComboBox() {
        //Get database connection
        DatabaseHelper database = new DatabaseHelper();
        database.connect();

        //Create lists from description sheet
        ArrayList<String> machineIssueList = database.createListFromColumn("Machine_Issue", "IssueDescriptions");

        //Populate Machine Issue Combo Box
        ObservableList<String> machineIssueComboList = FXCollections.observableList(machineIssueList);
        machineIssueComboBox.setItems(machineIssueComboList);

        //Close database connection
        database.closeConnection();
    }

    /*
    Write warranty machines to the warranty machine table
     */
    public void writeWarrantyMachineTable() {

        //Create Warranty Machine object
        WarrantyMachine warrantyMachine;

        //Machine issue selection
        String machineIssueSelection = machineIssueComboBox.getValue();

        //Create database helper and connect
        DatabaseHelper database = new DatabaseHelper();
        database.connect();

        //Get values from description sheet based on machine issue selection value
        String machineIssue = database.getCellValue("Machine_Issue", "IssueDescriptions", "Machine_Issue", machineIssueSelection);
        String troubleshootingSteps = database.getCellValue("Troubleshooting_Steps", "IssueDescriptions", "Machine_Issue", machineIssueSelection);
        String partNeeded = database.getCellValue("Part_Needed", "IssueDescriptions", "Machine_Issue", machineIssueSelection);
        String serviceTag = serviceTagTextField.getText();

        //Check if part needed is a battery
        //Write machine object to database
        if (partNeeded.equals("Battery") || partNeeded.equals("Display, Monitor")) {

            warrantyMachine = new WarrantyMachine(serviceTag, machineIssue,
                    troubleshootingSteps, partNeeded, serialNumberTextField.getText());

            database.addNewRowToWarrantyMachinesSerial(warrantyMachine);
        } else {

            warrantyMachine = new WarrantyMachine(serviceTag, machineIssue,
                    troubleshootingSteps, partNeeded);

            database.addNewRowToWarrantyMachines(warrantyMachine);
        }

        database.closeConnection();
    }

    /*
    Using selenium, perform the warranty process and log the information to the database
     */
    public void performWarranty(String techDirectEmail, String techDirectPass) {

        //Create WebDriver object
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\jsmit\\chromedriver.exe");
        WebDriver driver = new ChromeDriver();

        // Navigate to Dell Tech Direct Website
        driver.get("https://www.dell.com/Identity/global/" +
                "Login/7b0aec96-623a-4393-b6d0-a595d7d897ef?" +
                "Ctx=bXAR5%2FJWRYQAwJHNtR9DwN92sZVPfDGk%2BXHHHA" +
                "dnENeURzRs12i%2FKkH46JTWGwRs&feir=1");

        //Login to website
        websiteLogin(driver, techDirectEmail, techDirectPass);

        //Navigate to Self-Dispatch page
        WebElement selfDispatch = new WebDriverWait(driver, 30).until(
                ExpectedConditions.elementToBeClickable(By.id("_ctl0_BodyContent__ctl1__ctl0_btnVisit")));
        selfDispatch.click();

        //Navigate to Create Dispatch page
        WebElement createDispatch = new WebDriverWait(driver, 30).until(
                ExpectedConditions.elementToBeClickable(By.id("_ctl0_BodyContent_common_boxes_rptBoxes__ctl1_btnBox")));
        createDispatch.click();

        //Create database object and connect
        DatabaseHelper database = new DatabaseHelper();
        database.connect();

        //Create list of distinct service tags
        ArrayList<String> distinctServiceTags = database.createUniqueValueList("Service_Tag", "WarrantyMachines");

        //Perform warranty process for each warranty machine
        for (String serviceTag : distinctServiceTags) {

            //Get list of warranty machines with the same service tag
            ArrayList<WarrantyMachine> multiplePartMachineList = database.getDuplicateMachines(serviceTag);

            //Enter Service Tag
            WebElement enterServiceTag = new WebDriverWait(driver, 30).until(
                    ExpectedConditions.elementToBeClickable(By.id("_ctl0_BodyContent_CreateDispL_prt_txtSerialFind_tbDataControl")));
            enterServiceTag.sendKeys(serviceTag);

            //Submit Service Tag
            WebElement submitServiceTag = new WebDriverWait(driver, 30).until(
                    ExpectedConditions.elementToBeClickable(By.id("_ctl0_BodyContent_CreateDispL_lblbntValidate")));
            submitServiceTag.click();

            //Check for alert message (Out of Country machine, expired warranty status etc.)
            try {
                //Create variable to store alert message
                String alertMessage = driver.findElement(By.xpath("//*[@id=\"_ctl0_BodyContent_CreateDispL_caError_lblAlertText\"]")).getText();

                //Write the alert to the alert sheet table
                database.addNewRowToAlertLog(serviceTag, alertMessage);

                //Skip the iteration of this machine
                continue;
            } catch (NoSuchElementException e) {
                e.printStackTrace();
            }

            //Handle duplicate dispatch dialog if present
            try {
                WebElement duplicateDispatchButton = new WebDriverWait(driver, 5).until(
                        ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"modalDuplicateDispatch\"]/div/div/div[3]/button")));
                duplicateDispatchButton.click();
            } catch (NoSuchElementException | TimeoutException | ElementNotInteractableException e) {
                e.printStackTrace();
            }

            // Get the model of the current machine and format it to have the first character capitalized
            String machineModel = driver.findElement(By.xpath("//*[@id=\"_ctl0_BodyContent_CreateDispL_rev_row_Model_divLabel\"]/span")).getText().toLowerCase();
            machineModel = machineModel.substring(0, 1).toUpperCase() + machineModel.substring(1);

            //Create work order for current machine
            WebElement createWorkOrder = new WebDriverWait(driver, 30).until(
                    ExpectedConditions.elementToBeClickable(By.id("_ctl0_BodyContent_CreateDispL_lblbtnCreateWO")));
            createWorkOrder.click();


            //Get machine issue field
            WebElement machineIssueInput = new WebDriverWait(driver, 30).until(
                    ExpectedConditions.elementToBeClickable(By.id("_ctl0_BodyContent_ctrl02A65B24_dtlSubmitDispatchServiceTagInfo_ptbDescription_tbDataControl")));

            //Get troubleshooting steps field
            WebElement troubleshootingStepsInput = new WebDriverWait(driver, 30).until(
                    ExpectedConditions.elementToBeClickable(By.id("_ctl0_BodyContent_ctrl02A65B24_dtlSubmitDispatchServiceTagInfo_ptbTroubleshooting_tbDataControl")));

            //Check if the serial number has multiple issues
            if (multiplePartMachineList.size() > 1) {

                //Indicate multiple machines
                machineIssueInput.sendKeys("Multiple Issues");

                //Create stringbuilder to store all issues and troubleshooting steps
                StringBuilder multipleTroubleshootingSteps = new StringBuilder();

                //Build string of multiple machine issues and troubleshooting steps
                for (WarrantyMachine machine : multiplePartMachineList) {
                    multipleTroubleshootingSteps.append(machine.machineIssue);
                    multipleTroubleshootingSteps.append("\n");
                    multipleTroubleshootingSteps.append(machine.troubleshootingSteps);
                    multipleTroubleshootingSteps.append("\n");
                }

                //Enter multiple machine issues and troubleshooting steps
                troubleshootingStepsInput.sendKeys(multipleTroubleshootingSteps);

            } else {
                for (WarrantyMachine machine : multiplePartMachineList) {
                    //Enter machine issue
                    machineIssueInput.sendKeys(machine.machineIssue);

                    //Enter troubleshooting steps
                    troubleshootingStepsInput.sendKeys(machine.troubleshootingSteps);
                }
            }

            //Click Next
            WebElement nextPageButton = new WebDriverWait(driver, 30).until(
                    ExpectedConditions.elementToBeClickable(By.id("_ctl0_BodyContent_ctrl02A65B24_btnNextElement")));
            nextPageButton.click();

            //Enter phone number
            WebElement phoneNumberInput = new WebDriverWait(driver, 30).until(
                    ExpectedConditions.elementToBeClickable(By.id("_ctl0_BodyContent_ctrl02A65B24_dtlSubmitDispatchContact_ptbPhone_tbDataControl")));
            phoneNumberInput.sendKeys("8123771406");

            //Select address from ComboBox
            Select selectAddress = new Select(driver.findElement(By.id("_ctl0_BodyContent_ctrl02A65B24_dtlSubmitDispatchShipToAddress_pddAddress_ddlDataControl")));
            selectAddress.selectByVisibleText("Cummins Office Building");

            //Click next
            WebElement nextPageButton2 = new WebDriverWait(driver, 30).until(
                    ExpectedConditions.elementToBeClickable(By.id("_ctl0_BodyContent_ctrl02A65B24_btnNextElement")));
            nextPageButton2.click();

            //Select part needed
            selectMachinePart(driver, database, machineModel, multiplePartMachineList);

            //Switch from iFrame to default content
            driver.switchTo().defaultContent();

            //Click Next
            nextPageButton = new WebDriverWait(driver, 30).until(
                    ExpectedConditions.elementToBeClickable(By.id("_ctl0_BodyContent_ctrl02A65B24__ctl4")));
            nextPageButton.click();

            //Check for flea power dialog for motherboards
            try {
                WebElement motherboardConfirmButton = new WebDriverWait(driver, 5).until(
                        ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"btn1\"]")));
                motherboardConfirmButton.click();
            } catch (NoSuchElementException | TimeoutException e) {
                e.printStackTrace();
            }

            //**FOR DEBUGGING PURPOSES**
            //cancelWarrantyRequest(driver);

            //Submit request
            submitWarrantyRequest(driver, multiplePartMachineList, machineModel);

            //Machines to be removed form warranty table
            ObservableList<WarrantyMachine> machinesToBeRemoved = FXCollections.observableArrayList(multiplePartMachineList);

            //Remove current warranty machines from table
            database.removeRowsFromWarrantyMachines(machinesToBeRemoved);

        }

        //Close database connection
        database.closeConnection();

        //Close ChromeDriver
        driver.close();
    }

    public void writeToLogSheet(ArrayList<WarrantyMachine> multiplePartMachineList, String machineModel, String requestNumberRaw) {
        DatabaseHelper database = new DatabaseHelper();
        database.connect();

        //Get raw Service Request string
        String[] tempRequestNumberArray = requestNumberRaw.split(" ");

        //Create ArrayList and convert Array to ArrayList
        List<String> requestNumberList;
        requestNumberList = Arrays.asList(tempRequestNumberArray);

        //Get request number from last ArrayList index
        String requestNumber = requestNumberList.get(requestNumberList.size() - 1);

        //Remove period from end of Request Number
        requestNumber = requestNumber.substring(0, requestNumber.length() - 1);

        for (WarrantyMachine warrantyMachine : multiplePartMachineList) {
            //Write Warranty Information to Log Sheet
            database.addNewRowToMachineLog(requestNumber, warrantyMachine.serviceTag.toUpperCase(), machineModel, warrantyMachine.machineIssue, warrantyMachine.partNeeded);
        }

        //Close database connection
        database.closeConnection();
    }

    /*
    Login to website
     */
    private void websiteLogin(WebDriver driver, String techDirectEmail, String techDirectPass){
        //Input Email Address and Password
        driver.findElement(By.id("EmailAddress")).sendKeys(techDirectEmail);
        driver.findElement(By.id("Password")).sendKeys(techDirectPass);

        //Sign in
        WebElement signIn = new WebDriverWait(driver, 30).until(
                ExpectedConditions.elementToBeClickable(By.id("sign-in-button")));
        signIn.click();
    }

    //Select the parts or parts needed for repair
    private void selectMachinePart(WebDriver driver, DatabaseHelper database, String machineModel, ArrayList<WarrantyMachine> multiplePartMachineList){

        //Switch to parts iFrame
        WebElement partsIFrame = new WebDriverWait(driver, 30).until(
                ExpectedConditions.presenceOfElementLocated(By.id("iParts")));
        driver.switchTo().frame(partsIFrame);

        //Wait for all parts to load
        new WebDriverWait(driver, 60).until(
                ExpectedConditions.presenceOfAllElementsLocatedBy(By.tagName("clr-checkbox-container")));

        for (WarrantyMachine machine : multiplePartMachineList) {

            //Get part search criteria from database
            String partDescription = database.getPartDescription(machineModel, machine.partNeeded);

            //Enter part needed in search box
            WebElement partSearchField = new WebDriverWait(driver, 30).until(
                    ExpectedConditions.presenceOfElementLocated(By.name("search")));
            partSearchField.clear();
            partSearchField.sendKeys(partDescription);
            partSearchField.sendKeys(Keys.RETURN);


            //Check for checkbox element
            try {
                WebElement checkbox = driver.findElement(By.xpath("//strong[contains(text(),'" + partDescription + "')]/ancestor::clr-checkbox-container/descendant::input[@type='checkbox']"));
                checkbox.sendKeys(Keys.SPACE);
            } catch (NoSuchElementException e) {
                e.printStackTrace();
            }

            //Check for radio button element
            try {
                WebElement radioButton = driver.findElement(By.xpath("//strong[contains(text(),'" + partDescription + "')]/ancestor::clr-dg-row/descendant::input[@type='radio']"));
                radioButton.sendKeys(Keys.SPACE);

                //Check for serial number
                if (machine.serialNumber != null) {
                    WebElement serialNumberField = driver.findElement(By.xpath("//strong[contains(text(),'" + partDescription + "')]/ancestor::clr-dg-row/descendant::input[@type='text']"));
                    serialNumberField.sendKeys(machine.serialNumber);
                }
            } catch (NoSuchElementException e) {
                e.printStackTrace();

                //For 5530 model, if "80 Keys" search doesn't match, try "Palmrest" search
                if (machineModel.equals("Precision 5530") && machine.partNeeded.equals("Palm Rest (incl Touch Pad)")) {
                    try {
                        partSearchField.clear();
                        partSearchField.sendKeys("Palmrest");
                        partSearchField.sendKeys(Keys.RETURN);
                        WebElement checkbox = driver.findElement(By.xpath("//strong[contains(text(),'Palmrest')]/ancestor::clr-checkbox-container/descendant::input[@type='checkbox']"));
                        checkbox.sendKeys(Keys.SPACE);
                    } catch (NoSuchElementException e1) {
                        e1.printStackTrace();
                    }
                }

            }
        }

    }

    public void submitWarrantyRequest(WebDriver driver, ArrayList<WarrantyMachine> multipleIssueMachineList, String machineModel){

        //Submit Warranty
        WebElement warrantySubmit = new WebDriverWait(driver, 30).until(
                ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"_ctl0_BodyContent_ctrl02A65B24_btnNextStatus_43B9DA4D39254CE19F028F1BF6942430\"]")));
        warrantySubmit.click();

        //Get value for Service Request Number
        WebElement requestNumberText = new WebDriverWait(driver, 30).until(
                ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"_ctl0_BodyContent_ctrl02A65B24_dtlSubmitDispatchConfirm_ltCongratulations\"]")));
        String requestNumberRaw = requestNumberText.getText();

        writeToLogSheet(multipleIssueMachineList, machineModel, requestNumberRaw);

        //Submit Warranty
        WebElement createNewDispatch = new WebDriverWait(driver, 30).until(
                ExpectedConditions.elementToBeClickable(By.id("_ctl0_BodyContent_ctrl02A65B24_dtlSubmitDispatchConfirm_ltCreateAnother")));
        createNewDispatch.click();
    }

    /*
    Cancel requests instead of submitting and logging **FOR DEBUGGING PURPOSES**
    */
    private void cancelWarrantyRequest(WebDriver driver) {

        //Cancel Request
        WebElement cancelRequest = new WebDriverWait(driver, 30).until(
                ExpectedConditions.elementToBeClickable(By.id("_ctl0_BodyContent_ctrl02A65B24_hlbtnNextStatus_C6046059F28B469D9D3916425CFFDEF9")));
        cancelRequest.click();

        //Switch to modal window
        driver.switchTo().activeElement();

        //Click yes button in modal
        WebElement cancelRequestYes = new WebDriverWait(driver, 30).until(
                ExpectedConditions.presenceOfElementLocated(By.id("btn6")));
        cancelRequestYes.click();

        //Switch to confirmation modal
        driver.switchTo().activeElement();

        //Click confirmation yes
        WebElement confirmationYes = new WebDriverWait(driver, 30).until(
                ExpectedConditions.presenceOfElementLocated(By.id("btn1")));
        confirmationYes.click();

        WebElement selfDispatchLink = new WebDriverWait(driver, 30).until(
                ExpectedConditions.presenceOfElementLocated(By.linkText("Self-Dispatch")));
        selfDispatchLink.click();

        //Navigate to Create Dispatch page
        WebElement createDispatch2 = new WebDriverWait(driver, 30).until(
                ExpectedConditions.elementToBeClickable(By.id("_ctl0_BodyContent_common_boxes_rptBoxes__ctl1_btnBox")));
        createDispatch2.click();
    }

}




