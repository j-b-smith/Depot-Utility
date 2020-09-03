package JosephSmith;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainUIGridPaneController implements Initializable {
    public Button submitMachineButton;
    public Button initiateWarranty;
    @FXML
    public ComboBox<String> machineIssueComboBox;
    public TextField serviceTagTextField;
    public TextField serialNumberTextField;
    public Label serialNumberLabel;
    public ListView<WarrantyMachine> warrantyMachineListView;
    public Button removeListViewItem;
    public Label listViewCountLabel;
    public Label alertLabel;
    public TableColumn<Object, Object> serviceTag;
    public CheckBox multipleIssuesCheckbox;
    public Label multipleIssueLabel;
    public ListView<String> multipleIssueListView;
    public Button multipleIssueButton;



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        populateMachineIssueComboBox();
        populateMachineListView();
    }

    /*
    Run the warranty process on a separate thread to avoid UI stall
    Clear the list view
     */
    public void initiateWarrantyButton() throws IOException, InterruptedException {
        openLoginDialog();

    }

    public void openLoginDialog() throws IOException, InterruptedException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("loginDialog.fxml"));
        Parent root = (Parent) loader.load();
        Stage stage = new Stage();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Dell Tech Direct Login");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();

        //Get controller
        LoginDialogController loginController = loader.getController();
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
            boolean isValid = warrantyFormValidation();

            if (isValid) {
                alertLabel.setVisible(false);
                writeWarrantyMachineTable();
                serviceTagTextField.clear();
                serialNumberTextField.clear();
                serviceTagTextField.requestFocus();
                machineIssueComboBox.getSelectionModel().clearSelection();
                populateMachineListView();
                serialNumberTextField.setVisible(false);
                serialNumberLabel.setVisible(false);
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

        //Allow multiple selections by user with CTRL+Click
        warrantyMachineListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        //Populate object list from database
        ArrayList<WarrantyMachine> listViewMachineList = new ArrayList<>();
        for (int i = 0; i < database.getRowCount("WarrantyMachines"); i++) {
            WarrantyMachine machine = new WarrantyMachine(database.getCellValue("Service_Tag", "WarrantyMachines", i


            ),
                    database.getCellValue("Machine_Issue", "WarrantyMachines", i),
                    database.getCellValue("Troubleshooting_Steps", "WarrantyMachines", i),
                    database.getCellValue("Part_Needed", "WarrantyMachines", i),
                    database.getCellValue("Battery_Serial_Number", "WarrantyMachines", i));
            listViewMachineList.add(machine);
        }

        //Convert object list to Observable list
        ObservableList<WarrantyMachine> warrantyMachineListViewData = FXCollections.observableArrayList(listViewMachineList);

        if (warrantyMachineListViewData.size() != 0) {
            //Set machine quantity label
            listViewCountLabel.setText("Qty: " + warrantyMachineListViewData.size());
        } else {
            listViewCountLabel.setText("");
        }

        //Set List View data and populate
        warrantyMachineListView.setItems(warrantyMachineListViewData);
        warrantyMachineListView.setCellFactory(param -> new ListCell<WarrantyMachine>() {
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
        if (partNeeded.equals("Battery") || partNeeded.equals("Display, Monitor")) {

            warrantyMachine = new WarrantyMachine(serviceTagTextField.getText(), machineIssue,
                    troubleshootingSteps, partNeeded, serialNumberTextField.getText());

            database.addNewRowToWarrantyMachinesSerial(warrantyMachine);
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
    public void performWarranty(String techDirectEmail, String techDirectPass) throws InterruptedException {


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

        //Get the number of warranty machines in the database
        int rowCount = database.getRowCount("WarrantyMachines");

        //Iterate through warranty machines
        for (int i = 0; i < rowCount; i++) {

            String serviceTag = database.getCellValue("Service_Tag", "WarrantyMachines", i);
            String machineIssue = database.getCellValue("Machine_Issue", "WarrantyMachines", i);
            String troubleshootingSteps = database.getCellValue("Troubleshooting_Steps", "WarrantyMachines", i);
            String partNeeded = database.getCellValue("Part_Needed", "WarrantyMachines", i);
            String serialNumber = database.getCellValue("Battery_Serial_Number", "WarrantyMachines", i);

            //Create warranty machine object
            WarrantyMachine warrantyMachine;

            //Check if part needed is a battery
            if (partNeeded.equals("Battery") || partNeeded.equals("Display, Monitor")) {

                //Create warranty machine object with battery
                warrantyMachine = new WarrantyMachine(serviceTag, machineIssue,
                        troubleshootingSteps, partNeeded, serialNumber);
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

            //Switch to parts iFrame
            WebElement partsIFrame = new WebDriverWait(driver, 30).until(
                    ExpectedConditions.presenceOfElementLocated(By.id("iParts")));
            driver.switchTo().frame(partsIFrame);
            Thread.sleep(7000);

            //Enter part needed in search box
            WebElement partSearchField = new WebDriverWait(driver, 30).until(
                    ExpectedConditions.presenceOfElementLocated(By.name("search")));
            partSearchField.sendKeys(database.getPartDescription(machineModel, warrantyMachine.partNeeded));
            partSearchField.sendKeys(Keys.RETURN);

            //Wait for elements to load
            Thread.sleep(4000);

            //Get all input elements on page
            List<WebElement> radioButtonList = driver.findElements(By.tagName("input"));
            //Remove any elements that are not of type radio
            radioButtonList.removeIf(input -> !input.getAttribute("type").equals("radio"));

            //Get all checkbox elements on page
            List<WebElement> checkboxList = driver.findElements(By.name("options"));

            //Check if there are any radio buttons present on page
            if (radioButtonList.size() != 0){
                for (WebElement radioButton : radioButtonList) {
                        //Check if radio button contains strong tag
                        List<WebElement> strongTagList = driver.findElements(By.tagName("strong"));
                        for (WebElement strongTag : strongTagList) {
                            //Check if the text of the strong tag contains the part description
                            if (strongTag.getText().contains(database.getPartDescription(machineModel, warrantyMachine.partNeeded))) {
                                //Retrieve the id of the element that meets this criteria and select with the space key
                                driver.findElement(By.id(radioButton.getAttribute("id"))).sendKeys(Keys.SPACE);
                                //Check if the part needed is a battery
                                if (warrantyMachine.partNeeded.equals("Battery") || warrantyMachine.partNeeded.equals("Display, Monitor")) {
                                    //Get all input elements on the page
                                    List<WebElement> inputList = driver.findElements(By.tagName("input"));
                                    //Remove all elements that are not of type text and not of class form-control
                                    inputList.removeIf(input -> !input.getAttribute("type").equals("text"));
                                    inputList.removeIf(input -> !input.getAttribute("class").equals("form-control"));
                                    for (WebElement input : inputList) {
                                            //Input the serial number into the element that meets these criteria
                                            input.sendKeys(warrantyMachine.serialNumber);
                                    }
                                }
                            }

                        }
                    break;
                }
            //If no radio buttons are present
            } else {
                for (WebElement checkbox : checkboxList) {

                        //Check if checkbox contains strong tag
                        List<WebElement> strongTagList = driver.findElements(By.tagName("strong"));
                        for (WebElement strongTag : strongTagList) {
                            //Check if the text of the strong tag contains the part description
                            String test = strongTag.getText();
                            String description = database.getPartDescription(machineModel, warrantyMachine.partNeeded);
                            if (strongTag.getText().contains(database.getPartDescription(machineModel, warrantyMachine.partNeeded))) {
                                //Retrieve the id of the element that meets this criteria and select with the space key
                                driver.findElement(By.id(checkbox.getAttribute("id"))).sendKeys(Keys.SPACE);
                            }
                        }
                    break;
                }
            }

            //Switch from iFrame to default content
            driver.switchTo().defaultContent();

            //Click Next
            nextPageButton = new WebDriverWait(driver, 30).until(
                    ExpectedConditions.elementToBeClickable(By.id("_ctl0_BodyContent_ctrl02A65B24__ctl4")));
            nextPageButton.click();

            //Check for flea power motherboard alert/ePSA alert'
            Thread.sleep(2000);
            if (checkIfExistsByXpath(driver, "//*[@id=\"btn1\"]")) {
                driver.findElement(By.xpath("//*[@id=\"btn1\"]")).click();
            }

            //**FOR DEBUGGING PURPOSES**
            //cancelWarrantyRequest(driver);


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


            //Remove current warranty machine from table
            database.removeRowFromWarrantyMachines(warrantyMachine);
        }

        //Remove all entries from database table
        //database.clearTable("WarrantyMachines");

        //Close database connection
        database.closeConnection();

        //Close ChromeDriver
        driver.close();
    }

    public void writeToLogSheet(WarrantyMachine warrantyMachine, String machineModel, String requestNumberRaw) {
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

        //Write Warranty Information to Log Sheet
        database.addNewRowToLogSheet(requestNumber, warrantyMachine.serviceTag.toUpperCase(), machineModel, warrantyMachine.machineIssue, warrantyMachine.partNeeded);
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

    /*
    Cancel requests instead of submitting and logging **FOR DEBUGGING PURPOSES**
    */
    private void cancelWarrantyRequest(WebDriver driver) throws InterruptedException {

        Thread.sleep(2000);
        //Cancel Request
        WebElement cancelRequest = new WebDriverWait(driver, 30).until(
                ExpectedConditions.presenceOfElementLocated(By.id("_ctl0_BodyContent_ctrl02A65B24_hlbtnNextStatus_C6046059F28B469D9D3916425CFFDEF9")));
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


    public boolean warrantyFormValidation() {

        //retrieve values from form fields
        String serviceTag = serviceTagTextField.getText();
        String machineIssueSelection = machineIssueComboBox.getValue();
        String serialNumber = serialNumberTextField.getText();

        //Regex to limit service tag input to exactly 7 alphanumeric characters
        String serviceTagRegex = "^[a-zA-Z0-9]{7}+$";
        Pattern serviceTagpattern = Pattern.compile(serviceTagRegex);
        Matcher matcher = serviceTagpattern.matcher(serviceTag);
        boolean serviceTagMatches = matcher.matches();

        //Regex to limit serial number input to exactly 20 alphanumeric characters
        String serialNumberRegex = "^[a-zA-Z0-9]{20}+$";
        Pattern serialNumberPattern = Pattern.compile(serialNumberRegex);
        matcher = serialNumberPattern.matcher(serialNumber);
        boolean serialNumberMatches = matcher.matches();
        boolean serialNumberVisible = serialNumberTextField.isVisible();

        //Store the result of all checks
        boolean isValid = true;

        //Check if service tag and serial number are incorrect
        if (!serviceTagMatches && serialNumberVisible && !serialNumberMatches){
            alertLabel.setText("* Service Tag must be 7 characters (a-z, A-Z, 0-9)\n* Serial Number must be 20 characters (a-z, A-Z, 0-9)");
            serviceTagTextField.setStyle("-fx-border-color: red;");
            serialNumberTextField.setStyle("-fx-border-color: red;");
            machineIssueComboBox.setStyle(null);
            alertLabel.setVisible(true);
            isValid = false;

        //Check if service tag and machine issue selection are incorrect
        } else if (!serviceTagMatches && machineIssueSelection == null){
            alertLabel.setText("* Service Tag must be 7 characters (a-z, A-Z, 0-9)\n* Please select a machine issue");
            serviceTagTextField.setStyle("-fx-border-color: red;");
            serialNumberTextField.setStyle(null);
            machineIssueComboBox.setStyle("-fx-border-color: red;");
            alertLabel.setVisible(true);
            isValid = false;

        //Check if service tag is incorrect
        } else if (!serviceTagMatches){
            alertLabel.setText("* Service Tag must be 7 characters (a-z, A-Z, 0-9)");
            serviceTagTextField.setStyle("-fx-border-color: red;");
            serialNumberTextField.setStyle(null);
            machineIssueComboBox.setStyle(null);
            alertLabel.setVisible(true);
            isValid = false;

        //Check if machine issue selection is incorrect
        } else if (machineIssueSelection == null){
            alertLabel.setText("* Please select a machine issue");
            serviceTagTextField.setStyle(null);
            serialNumberTextField.setStyle(null);
            machineIssueComboBox.setStyle("-fx-border-color: red;");
            alertLabel.setVisible(true);
            isValid = false;

        //Check if serial number is visible and incorrect
        } else if (serialNumberVisible && !serialNumberMatches){
            alertLabel.setText("* Serial Number must be 20 characters (a-z, A-Z, 0-9)");
            serviceTagTextField.setStyle(null);
            serialNumberTextField.setStyle("-fx-border-color: red;");
            machineIssueComboBox.setStyle(null);
            alertLabel.setVisible(true);
            isValid = false;

        //Remove styles if all tests pass
        } else {
            serviceTagTextField.setStyle(null);
            serialNumberTextField.setStyle(null);
            machineIssueComboBox.setStyle(null);
            alertLabel.setVisible(false);
        }

        //Validate the input
        return isValid;
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




