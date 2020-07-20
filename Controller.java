package JosephSmith;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class Controller implements Initializable {
    public Button submitMachineButton;
    public Button initiateWarranty;
    @FXML
    public Button viewLog;
    public ComboBox<String> machineIssueComboBox;
    public TextField serviceTagTextField;
    public Button newMachineIssueButton;
    public TextField batterySerialNumberTextField;
    public Label batterySerialNumberLabel;
    public ListView<WarrantyMachine> warrantyMachineListView;
    public Button removeListViewItem;
    public Label listViewCountLabel;
    public String loginEmail;
    public String loginPassword;
    public ArrayList<String> credentials;

    //Runs when UI is created
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        //Get login credentials from Main
        Main main = new Main();
        credentials = main.credentials;

        createWarrantyMachineTable();
        populateMachineIssueComboBox();
    }

    @FXML
    private void displayLog() throws IOException{
        try {
            Stage stage = (Stage) viewLog.getScene().getWindow();
            GridPane root = FXMLLoader.load(getClass().getResource("logSheetUI.fxml"));
            Scene scene = new Scene(root, 1200, 700);
            stage.setScene(scene);
        } catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    /*
    Run the warranty process on a separate thread to avoid UI stall and clear the list view
     */
    public void initiateWarrantyButton(){
        Thread warrantyThread = new Thread(() -> {
            try {
                performWarranty();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        warrantyThread.start();
        warrantyMachineListView.getItems().clear();
    }
    
    /*
    Write the value to the warranty machine table, clear the service tag field,
    clear the battery serial number field, set focus to the service tag field, 
    populate the list view
     */
    @FXML
    public void submitMachineButton(){
        writeWarrantyMachineTable();
        serviceTagTextField.clear();
        batterySerialNumberTextField.clear();
        serviceTagTextField.requestFocus();
        machineIssueComboBox.getSelectionModel().clearSelection();
        populateListView();

    }

    @FXML
    public void onServiceTagInputEnter(){
        machineIssueComboBox.requestFocus();
    }

    @FXML
    public void onComboKeyEnter(javafx.scene.input.KeyEvent keyEvent) {
        if (keyEvent.getCode().equals(KeyCode.ENTER)){
            submitMachineButton.requestFocus();
        }
    }
    
    /*
    Display and enable the battery serial number label and textfield only if the issue is for a battery
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
    Get the current warranty machine values from the database and populate the list view
     */
    public void populateListView(){
        
        //Get database connection
        DatabaseHelper database = new DatabaseHelper();
        database.connect();

        ArrayList<WarrantyMachine> listViewMachineList = new ArrayList<>();

        //Allow multiple selections by user with CTRL+Click
        warrantyMachineListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        //Populate object list from database
        for (int i = 0; i < database.getRowCount("WarrantyMachines"); i++){
            WarrantyMachine machine = new WarrantyMachine (database.getCellValue("Service_Tag", "WarrantyMachines", i),
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
    Remove selected items from list view and database
     */
    public void removeFromListView(){

        //Get database connection
        DatabaseHelper database = new DatabaseHelper();
        database.connect();
        /*
        Works, but if you try to delete multiple items next to each other the last item is not deleted, the same goes for 3 items with spaces between each
        Single item delete works fine, 2 item delete with a space in between works fine
         */
        //Remove selected items from WarrantyMachines Table and List View
        ObservableList<WarrantyMachine> selectedWarrantyMachines = warrantyMachineListView.getSelectionModel().getSelectedItems();
        System.out.println(selectedWarrantyMachines.size());
        ObservableList<WarrantyMachine> currentMachinesData = warrantyMachineListView.getItems();

        for (int i = 0; i < selectedWarrantyMachines.size(); i++) {
            database.removeRowFromWarrantyMachines(selectedWarrantyMachines.get(i));
            currentMachinesData.remove(selectedWarrantyMachines.get(i));
        }
        
        //Update List View data
        warrantyMachineListView.setItems(currentMachinesData);

        //Set machine quantity label
        if (currentMachinesData.size() > 0) {
            listViewCountLabel.setText("Qty: " + currentMachinesData.size());
        } else listViewCountLabel.setText("");
        database.closeConnection();
    }
    
    /*
    Get the possible machine issues from the database and populate the combo box
     */
    public void populateMachineIssueComboBox(){
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
    Create a table to store current warranty machines
     */
    public void createWarrantyMachineTable(){
        //Connect to database
        DatabaseHelper database = new DatabaseHelper();
        database.connect();

        //Delete table
        database.deleteTable("WarrantyMachines");

        //Create warranty machine table
        database.createNewTable("WarrantyMachines", "Service_Tag");
        database.addNewColumn("WarrantyMachines", "Machine_Issue");
        database.addNewColumn("WarrantyMachines", "Troubleshooting_Steps");
        database.addNewColumn("WarrantyMachines", "Part_Needed");
        database.addNewColumn("WarrantyMachines", "Battery_Serial_Number");

        //Close connection
        database.closeConnection();
    }
    
    /*
    Write warranty machines to the warranty machine table
     */
    public void writeWarrantyMachineTable(){

        //Create Warranty Machine object
        WarrantyMachine warrantyMachine;

        //Machine issue selection
        int machineIssueSelection = machineIssueComboBox.getSelectionModel().getSelectedIndex();

        //Create database helper and connect
        DatabaseHelper database = new DatabaseHelper();
        database.connect();

        //Get values from description sheet
        String machineIssue = database.getCellValue("Machine_Issue", "DescriptionSheet", machineIssueSelection);
        String troubleshootingSteps = database.getCellValue("Troubleshooting_Steps", "DescriptionSheet", machineIssueSelection);
        String partNeeded = database.getCellValue("Part_Needed", "DescriptionSheet", machineIssueSelection);

        //Check if part needed is a battery

        if (partNeeded.equals("Battery, Removable")) {
            warrantyMachine = new WarrantyMachine(serviceTagTextField.getText(), machineIssue,
                    troubleshootingSteps, partNeeded, batterySerialNumberTextField.getText());

            //Write new warranty machine to database
            database.addNewRowToWarrantyMachinesBattery(warrantyMachine.serviceTag, warrantyMachine.machineIssue,
                    warrantyMachine.troubleshootingSteps, warrantyMachine.partNeeded, warrantyMachine.batterySerialNumber);

        } else {
            warrantyMachine = new WarrantyMachine(serviceTagTextField.getText(), machineIssue,
                    troubleshootingSteps, partNeeded);

            database.addNewRowToWarrantyMachines(warrantyMachine.serviceTag, warrantyMachine.machineIssue,
                    warrantyMachine.troubleshootingSteps, warrantyMachine.partNeeded);
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
        driver.findElement(By.id("EmailAddress")).sendKeys(loginEmail);
        driver.findElement(By.id("Password")).sendKeys(loginPassword);

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
            if (checkIfExistsByXpath(driver ,"//*[@id=\"modalDuplicateDispatch\"]/div/div/div[3]/button")){
                if (driver.findElement(By.xpath("//*[@id=\"modalDuplicateDispatch\"]/div/div/div[3]/button")).isDisplayed()){
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
            if (warrantyMachine.partNeeded.equals("Battery, Removable")){
                //Check if swollen
                if (warrantyMachine.machineIssue.equals("Battery Swollen")){
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
                if (machineModel.toLowerCase().equals("precision 5530")){
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
            nextPageButton = new WebDriverWait(driver, 30). until(
                    ExpectedConditions.elementToBeClickable(By.id("_ctl0_BodyContent_ctrl02A65B24_btnNextElement")));
            nextPageButton.click();

            //Check for flea power motherboard alert/ePSA alert'
            Thread.sleep(2000);
            if (checkIfExistsByXpath(driver, "//*[@id=\"btn1\"]")){
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

            writeToLogSheet(warrantyMachine, machineModel,  requestNumberRaw);

            //Submit Warranty
            WebElement createNewDispatch = new WebDriverWait(driver, 30).until(
                    ExpectedConditions.elementToBeClickable(By.id("_ctl0_BodyContent_ctrl02A65B24_dtlSubmitDispatchConfirm_ltCreateAnother")));
            createNewDispatch.click();
            }

        //Delete table
        database.deleteTable("WarrantyMachines");

        //Close database connection
        database.closeConnection();

        //Close ChromeDriver
        driver.close();
        }

        public void writeToLogSheet(WarrantyMachine warrantyMachine, String machineModel, String requestNumberRaw){
            DatabaseHelper database = new DatabaseHelper();
            database.connect();

            String[] tempRequestNumberArray = requestNumberRaw.split(" ");

            //Create ArrayList and convert Array to ArrayList
            List<String> requestNumberList;
            requestNumberList = Arrays.asList(tempRequestNumberArray);

            //Get request number from last ArrayList index
            String requestNumber = requestNumberList.get(requestNumberList.size() - 1);

            //Remove period from end of Request Number
            requestNumber = requestNumber.substring(0, requestNumber.length() -1);

            //Write Warranty Information to Log Sheet
            database.addNewRowToLogSheet(requestNumber, warrantyMachine.serviceTag, machineModel, warrantyMachine.machineIssue, warrantyMachine.partNeeded);
            database.closeConnection();
        }
    /*
    Check if a web element exists or not using it's xpath     
     */
    public boolean checkIfExistsByXpath(WebDriver driver, String xpath){
        try {
            driver.findElement(By.xpath(xpath));
        } catch (org.openqa.selenium.NoSuchElementException e){
            e.printStackTrace();
            return false;
        } return true;
    }

    //Display the dialog when "Add New Machine Issue" is clicked in mainUI
    public void showNewIssueDialog() throws IOException {

        //Create dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add New Machine Issue");

        //Get resource for loader and set dialog
        FXMLLoader newIssueDialogLoader = new FXMLLoader(getClass().getResource("newIssueDialog.fxml"));
        dialog.setDialogPane(newIssueDialogLoader.load());

        //Get controller for dialog, add buttons and display dialog
        NewIssueDialogController controller = newIssueDialogLoader.getController();
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Optional<ButtonType> dialogResult = dialog.showAndWait();

        /*
        Prevent the user from entering null values **Causes NullPointerException but doesn't crash program**
        Does not work if IF statement checking for null values below is removed
         */
        Button dialogOK = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        dialogOK.addEventFilter(ActionEvent.ACTION, actionEvent -> {
            if(controller.newMachineIssue.getText().isEmpty() ||
                    controller.newTroubleshootingSteps.getText().isEmpty() ||
                    controller.newPartNeededComboBox.getValue().isEmpty()){
                actionEvent.consume();
                controller.alertLabel.setText("Please fill in all required fields");
            }
        });

        //Check for null values
        if (controller.newMachineIssue.getText() != null &&
                controller.newTroubleshootingSteps.getText() != null &&
                controller.newPartNeededComboBox.getSelectionModel().getSelectedItem() != null) {

            //If OK button is clicked, write to database
            if (dialogResult.isPresent() && dialogResult.get() == ButtonType.OK) {
                DatabaseHelper database = new DatabaseHelper();
                database.connect();
                database.addNewRowToDescriptionSheet(controller.newMachineIssue.getText(),
                        controller.newTroubleshootingSteps.getText(), controller.newPartNeededComboBox.getValue());
                database.closeConnection();
            }
        }
    }


}

