package JosephSmith;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.net.URL;
import java.util.*;

public class MainUIGridPaneController implements Initializable {
    public Button submitMachineButton;
    public Button initiateWarranty;
    @FXML
    public ComboBox<String> machineIssueComboBox;
    public TextField serviceTagTextField;
    public TextField batterySerialNumberTextField;
    public Label batterySerialNumberLabel;
    public ListView<WarrantyMachine> warrantyMachineListView;
    public Button removeListViewItem;
    public Label listViewCountLabel;
    public Label alertLabel;
    public TableColumn<Object, Object> serviceTag;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        populateMachineIssueComboBox();
        DatabaseHelper database = new DatabaseHelper();
        database.connect();
        database.clearTable("WarrantyMachines");
    }

    /*
    Run the warranty process on a separate thread to avoid UI stall
    Clear the list view
     */
    public void initiateWarrantyButton() {
        listViewCountLabel.setText("");
        warrantyMachineListView.getItems().clear();
        Thread warrantyThread = new Thread(() -> {
            try {
                performWarranty();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        warrantyThread.start();
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

        //Check that all fields have been filled
        if (serviceTagTextField.getText() == null || machineIssueComboBox.getSelectionModel().getSelectedItem() == null) {
            alertLabel.setVisible(true);
        } else if (serviceTagTextField.getText().length() != 7) {
            alertLabel.setText("* The Service Tag must be 7 characters long");
        } else if (machineIssueComboBox.getSelectionModel().getSelectedItem().equals("Battery Swollen") ||
                machineIssueComboBox.getSelectionModel().getSelectedItem().equals("Battery Not Charging / Holding Charge")
                        && batterySerialNumberTextField.getText() == null) {
            alertLabel.setText("* Please complete all required fields");
        } else {
            alertLabel.setVisible(false);
            writeWarrantyMachineTable();
            serviceTagTextField.clear();
            batterySerialNumberTextField.clear();
            serviceTagTextField.requestFocus();
            machineIssueComboBox.getSelectionModel().clearSelection();
            populateListView();
        }
    }

    @FXML
    public void onServiceTagInputEnter() {
        machineIssueComboBox.requestFocus();
    }

    @FXML
    public void onComboKeyEnter(javafx.scene.input.KeyEvent keyEvent) {

        if (keyEvent.getCode().equals(KeyCode.ENTER)) {
            submitMachineButton.requestFocus();
        }

    }

    /*
    If the issue selected requires a battery:
    Set the battery serial number label and text field visible and enables
    Else:
    Set the battery serial number label and text field not visible and disabled
     */
    public void onComboBoxChange() {
        if (machineIssueComboBox.getValue() != null) {
            if (machineIssueComboBox.getValue().equals("Battery Swollen") || machineIssueComboBox.getValue().equals("Battery Not Charging / Holding Charge")) {
                batterySerialNumberLabel.setVisible(true);
                batterySerialNumberTextField.setVisible(true);
                batterySerialNumberTextField.setDisable(false);
            } else {
                batterySerialNumberLabel.setVisible(false);
                batterySerialNumberTextField.setVisible(false);
                batterySerialNumberTextField.setDisable(true);
            }
        }
    }

    /*
    Retrieve the Warranty machine table information from the database
    Use the information to populate the list view
     */
    public void populateListView() {

        //Get database connection
        DatabaseHelper database = new DatabaseHelper();
        database.connect();

        //Allow multiple selections by user with CTRL+Click
        warrantyMachineListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        //Populate object list from database
        ArrayList<WarrantyMachine> listViewMachineList = new ArrayList<>();
        for (int i = 0; i < database.getRowCount("WarrantyMachines"); i++) {
            WarrantyMachine machine = new WarrantyMachine(database.getCellValue("Service_Tag", "WarrantyMachines", i),
                    database.getCellValue("Machine_Issue", "WarrantyMachines", i),
                    database.getCellValue("Troubleshooting_Steps", "WarrantyMachines", i),
                    database.getCellValue("Part_Needed", "WarrantyMachines", i),
                    database.getCellValue("Battery_Serial_Number", "WarrantyMachines", i));
            listViewMachineList.add(machine);
        }

        //Convert object list to Observable list
        ObservableList<WarrantyMachine> warrantyMachineListViewData = FXCollections.observableArrayList(listViewMachineList);

        //Set machine quantity label
        listViewCountLabel.setText("Qty: " + warrantyMachineListViewData.size());

        //Set List View data and populate
        warrantyMachineListView.setItems(warrantyMachineListViewData);
        warrantyMachineListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(WarrantyMachine item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null || item.toString() == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                }
            }
        });

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
        selectedWarrantyMachines = warrantyMachineListView.getSelectionModel().getSelectedItems();
        currentMachinesData = warrantyMachineListView.getItems();

        //Remove from warranty machine table
        database.removeRowsFromWarrantyMachines(selectedWarrantyMachines);

        //Update list view
        currentMachinesData.removeAll(selectedWarrantyMachines);
        warrantyMachineListView.setItems(currentMachinesData);

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
        ArrayList<String> machineIssueList = database.createListFromColumn("Machine_Issue", "DescriptionSheet");


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
        String machineIssueSelection = machineIssueComboBox.getSelectionModel().getSelectedItem();

        //Create database helper and connect
        DatabaseHelper database = new DatabaseHelper();
        database.connect();


        //Get values from description sheet based on machine issue selection value
        String machineIssue = database.getCellValue("Machine_Issue", "DescriptionSheet", "Machine_Issue", machineIssueSelection);
        String troubleshootingSteps = database.getCellValue("Troubleshooting_Steps", "DescriptionSheet", "Machine_Issue", machineIssueSelection);
        String partNeeded = database.getCellValue("Part_Needed", "DescriptionSheet", "Machine_Issue", machineIssueSelection);

        //Check if part needed is a battery
        //Write machine object to database
        if (partNeeded.equals("Battery, Removable")) {

            warrantyMachine = new WarrantyMachine(serviceTagTextField.getText(), machineIssue,
                    troubleshootingSteps, partNeeded, batterySerialNumberTextField.getText());

            database.addNewRowToWarrantyMachinesBattery(warrantyMachine);
        } else {

            warrantyMachine = new WarrantyMachine(serviceTagTextField.getText(), machineIssue,
                    troubleshootingSteps, partNeeded);

            database.addNewRowToWarrantyMachines(warrantyMachine);
        }

        database.closeConnection();
    }

    /*
    Using selenium, perform the warranty process and log the information to the database
     */
    public void performWarranty() throws InterruptedException {
        //Create WebDriver object
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\jsmit\\chromedriver.exe");
        WebDriver driver = new ChromeDriver();


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

        //Navigate to Create Dispatch page
        WebElement createDispatch = new WebDriverWait(driver, 30).until(
                ExpectedConditions.elementToBeClickable(By.id("_ctl0_BodyContent_common_boxes_rptBoxes__ctl1_btnBox")));
        createDispatch.click();


        //Create database object and connect
        DatabaseHelper database = new DatabaseHelper();
        database.connect();

        //Iterate through warranty machines
        for (int i = 0; i < database.getRowCount("WarrantyMachines"); i++) {

            String serviceTag = database.getCellValue("Service_Tag", "WarrantyMachines", i);
            String machineIssue = database.getCellValue("Machine_Issue", "WarrantyMachines", i);
            String troubleshootingSteps = database.getCellValue("Troubleshooting_Steps", "WarrantyMachines", i);
            String partNeeded = database.getCellValue("Part_Needed", "WarrantyMachines", i);
            String batterySerialNumber = database.getCellValue("Battery_Serial_Number", "WarrantyMachines", i);

            //Create warranty machine object
            WarrantyMachine warrantyMachine;

            //Check if part needed is a battery
            if (partNeeded.equals("Battery, Removable")) {

                //Create warranty machine object with battery
                warrantyMachine = new WarrantyMachine(serviceTag, machineIssue,
                        troubleshootingSteps, partNeeded, batterySerialNumber);
            } else {

                //Create warranty machine object without battery
                warrantyMachine = new WarrantyMachine(serviceTag, machineIssue,
                        troubleshootingSteps, partNeeded);
            }

            //Enter Service Tag
            WebElement enterServiceTag = new WebDriverWait(driver, 30).until(
                    ExpectedConditions.elementToBeClickable(By.id("_ctl0_BodyContent_CreateDispL_prt_txtSerialFind_tbDataControl")));
            enterServiceTag.sendKeys(warrantyMachine.serviceTag);

            //Submit Service Tag
            WebElement submitServiceTag = new WebDriverWait(driver, 30).until(
                    ExpectedConditions.elementToBeClickable(By.id("_ctl0_BodyContent_CreateDispL_lblbntValidate")));
            submitServiceTag.click();

            //Check for alerts
            if (checkIfExistsByXpath(driver, "//*[@id=\"_ctl0_BodyContent_CreateDispL_caError_lblAlertText\"]")) {

                //Create variable to store alert message
                String alertMessage = driver.findElement(By.xpath("//*[@id=\"_ctl0_BodyContent_CreateDispL_caError_lblAlertText\"]")).getText();

                //Write the alert to the alert sheet table
                database.addNewRowToAlertSheet(warrantyMachine.serviceTag, alertMessage);

                //Skip the iteration of this machine
                continue;
            }

            //Click warranty in the past 30 days modal box if present
            if (checkIfExistsByXpath(driver, "//*[@id=\"modalDuplicateDispatch\"]/div/div/div[3]/button")) {
                if (driver.findElement(By.xpath("//*[@id=\"modalDuplicateDispatch\"]/div/div/div[3]/button")).isDisplayed()) {
                    driver.findElement(By.xpath("//*[@id=\"modalDuplicateDispatch\"]/div/div/div[3]/button")).click();
                }
            }

            // Get the model of the current machine and format it to have the first character capitalized
            String machineModel = driver.findElement(By.xpath("//*[@id=\"_ctl0_BodyContent_CreateDispL_rev_row_Model_divLabel\"]/span")).getText().toLowerCase();
            machineModel = machineModel.substring(0, 1).toUpperCase() + machineModel.substring(1);

            //Create work order for current machine
            WebElement createWorkOrder = new WebDriverWait(driver, 30).until(
                    ExpectedConditions.elementToBeClickable(By.id("_ctl0_BodyContent_CreateDispL_lblbtnCreateWO")));
            createWorkOrder.click();

            //Enter machine issue
            WebElement machineIssueInput = new WebDriverWait(driver, 30).until(
                    ExpectedConditions.elementToBeClickable(By.id("_ctl0_BodyContent_ctrl02A65B24_dtlSubmitDispatchServiceTagInfo_ptbDescription_tbDataControl")));
            machineIssueInput.sendKeys(warrantyMachine.machineIssue);

            //Enter troubleshooting steps
            WebElement issueDescriptionInput = new WebDriverWait(driver, 30).until(
                    ExpectedConditions.elementToBeClickable(By.id("_ctl0_BodyContent_ctrl02A65B24_dtlSubmitDispatchServiceTagInfo_ptbTroubleshooting_tbDataControl")));
            issueDescriptionInput.sendKeys(warrantyMachine.troubleshootingSteps);

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

            //Enter part needed in search box
            WebElement partSearchField = new WebDriverWait(driver, 30).until(
                    ExpectedConditions.elementToBeClickable(By.id("_ctl0_BodyContent_ctrl02A65B24_dtlSubmitDispatchParts_lvg9BB97555_txtSearch")));
            partSearchField.sendKeys(warrantyMachine.partNeeded);

            //Click search icon
            WebElement partSearchStart = new WebDriverWait(driver, 30).until(
                    ExpectedConditions.elementToBeClickable(By.id("_ctl0_BodyContent_ctrl02A65B24_dtlSubmitDispatchParts_lvg9BB97555_searchIcon")));
            partSearchStart.click();

            //Select Part
            //Check if part is Webcam, Select second checkbox if true
            if (warrantyMachine.partNeeded.equals("Camera/Webcam")) {
                Thread.sleep(3000);
                WebElement partListSecondCheckbox = new WebDriverWait(driver, 30).until(
                        ExpectedConditions.presenceOfElementLocated(By.id("_ctl0_BodyContent_ctrl02A65B24_dtlSubmitDispatchParts_lvg9BB97555_adg_dgiRow1__scSelect")));
                partListSecondCheckbox.click();

                // Else select first checkbox
            } else {
                Thread.sleep(3000);
                WebElement partListFirstCheckbox = new WebDriverWait(driver, 30).until(
                        ExpectedConditions.presenceOfElementLocated(By.id("_ctl0_BodyContent_ctrl02A65B24_dtlSubmitDispatchParts_lvg9BB97555_adg_dgiRow0__scSelect")));
                partListFirstCheckbox.click();
            }

            //Check if part is battery
            if (warrantyMachine.partNeeded.equals("Battery, Removable")) {
                //Check if swollen
                if (warrantyMachine.machineIssue.equals("Battery Swollen")) {
                    //Check yes
                    WebElement batterySwollenYes = new WebDriverWait(driver, 30).until(
                            ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"_ctl0_BodyContent_ctrl02A65B24_dtlSubmitDispatchParts_rbBatteryDamagedYes\"]")));
                    Thread.sleep(3000);
                    batterySwollenYes.click();
                    //******This ^ clickInterceptedException ******


                } else {

                    //Check no
                    WebElement batterySwollenNo = new WebDriverWait(driver, 30).until(
                            ExpectedConditions.elementToBeClickable(By.id("_ctl0_BodyContent_ctrl02A65B24_dtlSubmitDispatchParts_rbBatteryDamagedNo")));
                    Thread.sleep(3000);
                    batterySwollenNo.click();
                }

                //Click submit on battery dialog
                WebElement batterySwollenSubmit = new WebDriverWait(driver, 30).until(
                        ExpectedConditions.elementToBeClickable(By.id("_ctl0_BodyContent_ctrl02A65B24_dtlSubmitDispatchParts_btnBatteryDamagedSubmit")));
                batterySwollenSubmit.click();

                //Enter battery serial number if part is battery
                //Check if model is 5530, serial input has a different id
                if (machineModel.toLowerCase().equals("precision 5530")) {
                    WebElement batterySerialInput5530 = new WebDriverWait(driver, 30).until(
                            ExpectedConditions.elementToBeClickable(By.id("_ctl0_BodyContent_ctrl02A65B24_dtlSubmitDispatchParts_btnBatteryDamagedSubmit")));
                    batterySerialInput5530.sendKeys(warrantyMachine.batterySerialNumber);
                } else {
                    WebElement batterySerialInput5530 = new WebDriverWait(driver, 30).until(
                            ExpectedConditions.elementToBeClickable(By.id("_ctl0_BodyContent_ctrl02A65B24_dtlSubmitDispatchParts_lvg9BB97555_adg_dgiRow0_txtSerialNumber1CAFCD2642814D57898656F22FED4AB5_0")));
                    batterySerialInput5530.sendKeys(warrantyMachine.batterySerialNumber);
                }
            }

            //Click Next
            nextPageButton = new WebDriverWait(driver, 30).until(
                    ExpectedConditions.elementToBeClickable(By.id("_ctl0_BodyContent_ctrl02A65B24_btnNextElement")));
            nextPageButton.click();

            //Check for flea power motherboard alert/ePSA alert'
            Thread.sleep(2000);
            if (checkIfExistsByXpath(driver, "//*[@id=\"btn1\"]")) {
                driver.findElement(By.xpath("//*[@id=\"btn1\"]")).click();
            }

            //Submit Warranty
            WebElement warrantySubmit = new WebDriverWait(driver, 30).until(
                    ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"_ctl0_BodyContent_ctrl02A65B24_btnNextStatus_43B9DA4D39254CE19F028F1BF6942430\"]")));
            warrantySubmit.click();

            //Get value for Service Request Number
            WebElement requestNumberText = new WebDriverWait(driver, 30).until(
                    ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"_ctl0_BodyContent_ctrl02A65B24_dtlSubmitDispatchConfirm_ltCongratulations\"]")));
            String requestNumberRaw = requestNumberText.getText();

            writeToLogSheet(warrantyMachine, machineModel, requestNumberRaw);

            //Submit Warranty
            WebElement createNewDispatch = new WebDriverWait(driver, 30).until(
                    ExpectedConditions.elementToBeClickable(By.id("_ctl0_BodyContent_ctrl02A65B24_dtlSubmitDispatchConfirm_ltCreateAnother")));
            createNewDispatch.click();
        }

        //Delete table
        database.clearTable("WarrantyMachines");

        //Close database connection
        database.closeConnection();

        //Close ChromeDriver
        driver.close();
    }

    public void writeToLogSheet(WarrantyMachine warrantyMachine, String machineModel, String requestNumberRaw) {
        DatabaseHelper database = new DatabaseHelper();
        database.connect();

        String[] tempRequestNumberArray = requestNumberRaw.split(" ");

        //Create ArrayList and convert Array to ArrayList
        List<String> requestNumberList;
        requestNumberList = Arrays.asList(tempRequestNumberArray);

        //Get request number from last ArrayList index
        String requestNumber = requestNumberList.get(requestNumberList.size() - 1);

        //Remove period from end of Request Number
        requestNumber = requestNumber.substring(0, requestNumber.length() - 1);

        //Write Warranty Information to Log Sheet
        database.addNewRowToLogSheet(requestNumber, warrantyMachine.serviceTag, machineModel, warrantyMachine.machineIssue, warrantyMachine.partNeeded);
        database.closeConnection();
    }

    /*
    Check if a web element exists or not using it's xpath     
     */
    public boolean checkIfExistsByXpath(WebDriver driver, String xpath) {
        try {
            driver.findElement(By.xpath(xpath));
        } catch (org.openqa.selenium.NoSuchElementException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}


/*
Switch windows on button click
    @FXML
    private void displayLog() throws IOException {
        try {
            Stage stage = (Stage) viewLog.getScene().getWindow();
            GridPane root = FXMLLoader.load(getClass().getResource("logSheetUI.fxml"));
            Scene scene = new Scene(root, 1200, 700);
            stage.setScene(scene);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
 */


