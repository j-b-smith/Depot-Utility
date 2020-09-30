package JosephSmith;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InputValidator {

    //Controllers
    MainUIGridPaneController mainController;
    ServiceNowController serviceNowController;
    TechDirectLoginDialogController techDirectLoginDialogController;
    ServiceNowLoginDialogController serviceNowLoginDialogController;
    NewIssueDialogController newIssueDialogController;

    //Error Strings
    String wwidError = "* WWID must be 5 character and include 2 letters followed by 3 digits";
    String passwordError = "* Please enter a password";

    //Regex
    String wwidRegex = "^[a-zA-Z]{2}\\d{3}";
    String serviceTagRegex = "^[a-zA-Z0-9]{7}+$";
    String serialNumberRegex = "^[a-zA-Z0-9]{23}+$";

    //Constructors for each controller
    public InputValidator(MainUIGridPaneController mainUIGridPaneController) {
        this.mainController = mainUIGridPaneController;
    }

    public InputValidator(ServiceNowController serviceNowController) {
        this.serviceNowController = serviceNowController;
    }

    public InputValidator(TechDirectLoginDialogController techDirectLoginDialogController) {
        this.techDirectLoginDialogController = techDirectLoginDialogController;
    }

    public InputValidator(ServiceNowLoginDialogController serviceNowLoginDialogController) {
        this.serviceNowLoginDialogController = serviceNowLoginDialogController;
    }

    public InputValidator(NewIssueDialogController newIssueDialogController) {
        this.newIssueDialogController = newIssueDialogController;
    }

    public boolean warrantyFormValidation(){

        //retrieve values from form fields
        String serviceTag = mainController.serviceTagTextField.getText();
        String machineIssueSelection = mainController.machineIssueComboBox.getValue();
        String serialNumber = mainController.serialNumberTextField.getText();

        //Error Strings
        String serviceTagError = "* Service Tag must be 7 characters (a-z, A-Z, 0-9)";
        String serialNumberError = "* Serial Number must be 20 characters (a-z, A-Z, 0-9)";
        String machineIssueError = "* Please select a machine issue";

        //Regex to limit service tag input to exactly 7 alphanumeric characters
        Pattern serviceTagpattern = Pattern.compile(serviceTagRegex);
        Matcher matcher = serviceTagpattern.matcher(serviceTag);
        boolean serviceTagMatches = matcher.matches();

        //Regex to limit serial number to exactly 23 alphanumeric characters
        Pattern serialNumberPattern = Pattern.compile(serialNumberRegex);
        matcher = serialNumberPattern.matcher(serialNumber);
        boolean serialNumberMatches = matcher.matches();
        boolean serialNumberVisible = mainController.serialNumberTextField.isVisible();

        //Store the result of all checks
        boolean isValid = true;

        //Check if service tag and serial number are incorrect
        if (!serviceTagMatches && serialNumberVisible && !serialNumberMatches){
            mainController.alertLabel.setText(serviceTagError + "\n" + serialNumberError);
            mainController.serviceTagTextField.setStyle("-fx-border-color: red;");
            mainController.serialNumberTextField.setStyle("-fx-border-color: red;");
            mainController.machineIssueComboBox.setStyle(null);
            mainController.alertLabel.setVisible(true);
            isValid = false;

            //Check if service tag and machine issue selection are incorrect
        } else if (!serviceTagMatches && machineIssueSelection == null){
            mainController.alertLabel.setText(serviceTagError + "\n" + machineIssueError);
            mainController.serviceTagTextField.setStyle("-fx-border-color: red;");
            mainController.serialNumberTextField.setStyle(null);
            mainController.machineIssueComboBox.setStyle("-fx-border-color: red;");
            mainController.alertLabel.setVisible(true);
            isValid = false;

            //Check if service tag is incorrect
        } else if (!serviceTagMatches){
            mainController.alertLabel.setText(serviceTagError);
            mainController.serviceTagTextField.setStyle("-fx-border-color: red;");
            mainController.serialNumberTextField.setStyle(null);
            mainController.machineIssueComboBox.setStyle(null);
            mainController.alertLabel.setVisible(true);
            isValid = false;

            //Check if machine issue selection is incorrect
        } else if (machineIssueSelection == null){
            mainController.alertLabel.setText(machineIssueError);
            mainController.serviceTagTextField.setStyle(null);
            mainController.serialNumberTextField.setStyle(null);
            mainController.machineIssueComboBox.setStyle("-fx-border-color: red;");
            mainController.alertLabel.setVisible(true);
            isValid = false;

            //Check if serial number is visible and incorrect
        } else if (serialNumberVisible && !serialNumberMatches){
            mainController.alertLabel.setText(serialNumberError);
            mainController.serviceTagTextField.setStyle(null);
            mainController.serialNumberTextField.setStyle("-fx-border-color: red;");
            mainController.machineIssueComboBox.setStyle(null);
            mainController.alertLabel.setVisible(true);
            isValid = false;

            //Remove styles if all tests pass
        } else {
            mainController.serviceTagTextField.setStyle(null);
            mainController.serialNumberTextField.setStyle(null);
            mainController.machineIssueComboBox.setStyle(null);
            mainController.alertLabel.setVisible(false);

            mainController.serviceTagTextField.requestFocus();
            mainController.serialNumberTextField.setVisible(false);
            mainController.serialNumberLabel.setVisible(false);
        }

        //Validate the input
        return isValid;
    }
    
    public boolean techDirectLoginValidation(){
        
        //Retrieve values from form fields
        String userWWID = techDirectLoginDialogController.techDirectWwidField.getText();
        String userPass = techDirectLoginDialogController.techDirectPassField.getText();

        //Regex to limit WWID to alphanumeric characters with 2 letters followed by 3 digits
        Pattern wwidPattern = Pattern.compile(wwidRegex);
        Matcher matcher = wwidPattern.matcher(userWWID);
        boolean wwidMatches = matcher.matches();

        //Store the result of all checks
        boolean isValid = true;

        //Check if wwid is incorrect and password is blank
        if (!wwidMatches && userPass.equals("")) {
            techDirectLoginDialogController.alertLabel.setText(wwidError + "\n" + passwordError);
            techDirectLoginDialogController.techDirectWwidField.setStyle("-fx-border-color: red;");
            techDirectLoginDialogController.techDirectPassField.setStyle("-fx-border-color: red;");
            techDirectLoginDialogController.alertLabel.setVisible(true);
            isValid = false;

            //Check if wwid is incorrect
        } else if (!wwidMatches) {
            techDirectLoginDialogController.alertLabel.setText(wwidError);
            techDirectLoginDialogController.techDirectWwidField.setStyle("-fx-border-color: red;");
            techDirectLoginDialogController.techDirectPassField.setStyle(null);
            techDirectLoginDialogController.alertLabel.setVisible(true);
            isValid = false;

            //Check if password is blank
        } else if (userPass.equals("")) {
            techDirectLoginDialogController.alertLabel.setText(passwordError);
            techDirectLoginDialogController.techDirectWwidField.setStyle(null);
            techDirectLoginDialogController.techDirectPassField.setStyle("-fx-border-color: red;");
            techDirectLoginDialogController.alertLabel.setVisible(true);
            isValid = false;

            //Set styles to default and hide label
        } else {
            techDirectLoginDialogController.techDirectWwidField.setStyle(null);
            techDirectLoginDialogController.techDirectPassField.setStyle(null);
            techDirectLoginDialogController.alertLabel.setVisible(false);
        }
        return isValid;
    }

    public boolean serviceNowFormValidation(){
        //Retrieve values from form fields
        String sctaskNumber = serviceNowController.taskNumber.getText();
        String trackingNumber = serviceNowController.trackingNumber.getText();

        //Error Strings
        String sctaskError = "* Please enter an SCTASK number in the format \"SCTASK1234567\" or \"1234567\"";
        String trackingError = "* Please enter a tracking number that is 12 digits long (0-9)";

        //Regex to limit SCTASK number to the exact phrase "SCTASK" followed by exactly 7 digits
        String fullSCTaskRegex = "^\\bSCTASK\\d{7}$";
        Pattern fullSCTaskRegexPattern = Pattern.compile(fullSCTaskRegex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = fullSCTaskRegexPattern.matcher(sctaskNumber);
        boolean fullSCTaskMatches = matcher.matches();

        //Regex to limit SCTASK number to exactly 7 digits (SCTASK will be appended)
        String shortSCTaskRegex = "^[0-9]{7}$";
        Pattern shortSCTaskRegexPattern = Pattern.compile(shortSCTaskRegex);
        matcher = shortSCTaskRegexPattern.matcher(sctaskNumber);
        boolean shortSCTaskMatches = matcher.matches();

        //Regex to limit tracking number to exactly 12 digits
        String trackingNumberRegex = "^[0-9]{12}$";
        Pattern trackingNumberRegexPattern = Pattern.compile(trackingNumberRegex);
        matcher = trackingNumberRegexPattern.matcher(trackingNumber);
        boolean trackingNumberMatches = matcher.matches();

        //Store the result of all checks
        boolean isValid = true;

        //Check if wwid is incorrect and password and techname are blank/null
        if (!fullSCTaskMatches && !trackingNumberMatches) {
            serviceNowController.alertLabel.setText(sctaskError + "\n" + trackingError);
            serviceNowController.taskNumber.setStyle("-fx-border-color: red;");
            serviceNowController.trackingNumber.setStyle("-fx-border-color: red;");
            serviceNowController.alertLabel.setVisible(true);
            isValid = false;

            //Check if wwid is incorrect and password is blank
        } else if (!shortSCTaskMatches && !trackingNumberMatches) {
            serviceNowController.alertLabel.setText(sctaskError + "\n" + trackingError);
            serviceNowController.taskNumber.setStyle("-fx-border-color: red;");
            serviceNowController.trackingNumber.setStyle("-fx-border-color: red;");
            serviceNowController.alertLabel.setVisible(true);
            isValid = false;

            //Check if wwid is incorrect and tech name is null
        } else if (!trackingNumberMatches) {
            serviceNowController.alertLabel.setText(trackingError);
            serviceNowController.taskNumber.setStyle(null);
            serviceNowController.trackingNumber.setStyle("-fx-border-color: red;");
            serviceNowController.alertLabel.setVisible(true);
            isValid = false;
        } else if (!shortSCTaskMatches && !fullSCTaskMatches) {
            serviceNowController.alertLabel.setText(sctaskError);
            serviceNowController.taskNumber.setStyle("-fx-border-color: red;");
            serviceNowController.trackingNumber.setStyle(null);
            serviceNowController.alertLabel.setVisible(true);
            isValid = false;
            //Set styles to default and hide label
        } else {
            serviceNowController.taskNumber.setStyle(null);
            serviceNowController.trackingNumber.setStyle(null);
            serviceNowController.alertLabel.setVisible(false);
        }
        return isValid;
    }

    public boolean serviceNowLoginValidation() {

        //Retrieve values from form fields
        String userWWID = serviceNowLoginDialogController.serviceNowWwidField.getText();
        String userPass = serviceNowLoginDialogController.serviceNowPassField.getText();
        String techName = serviceNowLoginDialogController.techNameComboBox.getValue();

        //Error String
        String techNameError = "* Please select a tech name";

        //Regex to limit WWID to alphanumeric characters with 2 letters followed by 3 digits
        String wwidRegex = "^[a-zA-Z]{2}\\d{3}";
        Pattern wwidPattern = Pattern.compile(wwidRegex);
        Matcher matcher = wwidPattern.matcher(userWWID);
        boolean wwidMatches = matcher.matches();

        //Store the result of all checks
        boolean isValid = true;

        //Check if wwid is incorrect and password and techname are blank/null
        if (!wwidMatches && userPass.equals("") && techName == null) {
            serviceNowLoginDialogController.alertLabel.setText(wwidError + "\n" + passwordError + "\n" + techNameError);
            serviceNowLoginDialogController.serviceNowWwidField.setStyle("-fx-border-color: red;");
            serviceNowLoginDialogController.serviceNowPassField.setStyle("-fx-border-color: red;");
            serviceNowLoginDialogController.techNameComboBox.setStyle("-fx-border-color: red;");
            serviceNowLoginDialogController.alertLabel.setVisible(true);
            isValid = false;

            //Check if wwid is incorrect and password is blank
        } else if (!wwidMatches && userPass.equals("")) {
            serviceNowLoginDialogController.alertLabel.setText(wwidError + "\n" + passwordError);
            serviceNowLoginDialogController.serviceNowWwidField.setStyle("-fx-border-color: red;");
            serviceNowLoginDialogController.serviceNowPassField.setStyle("-fx-border-color: red;");
            serviceNowLoginDialogController.techNameComboBox.setStyle(null);
            serviceNowLoginDialogController.alertLabel.setVisible(true);
            isValid = false;

            //Check if wwid is incorrect and tech name is null
        } else if (!wwidMatches && techName == null) {
            serviceNowLoginDialogController.alertLabel.setText(wwidError + "\n" + techNameError);
            serviceNowLoginDialogController.serviceNowWwidField.setStyle("-fx-border-color: red;");
            serviceNowLoginDialogController.serviceNowPassField.setStyle(null);
            serviceNowLoginDialogController.techNameComboBox.setStyle("-fx-border-color: red;");
            serviceNowLoginDialogController.alertLabel.setVisible(true);
            isValid = false;

            //Check if password is blank and tech name is null
        } else if (userPass.equals("") && techName == null) {
            serviceNowLoginDialogController.alertLabel.setText(passwordError + "\n" + techNameError);
            serviceNowLoginDialogController.serviceNowWwidField.setStyle(null);
            serviceNowLoginDialogController.serviceNowPassField.setStyle("-fx-border-color: red;");
            serviceNowLoginDialogController.techNameComboBox.setStyle("-fx-border-color: red;");
            serviceNowLoginDialogController.alertLabel.setVisible(true);
            isValid = false;

            //Check if wwid is incorrect
        } else if (!wwidMatches) {
            serviceNowLoginDialogController.alertLabel.setText(wwidError);
            serviceNowLoginDialogController.serviceNowWwidField.setStyle("-fx-border-color: red;");
            serviceNowLoginDialogController.serviceNowPassField.setStyle(null);
            serviceNowLoginDialogController.techNameComboBox.setStyle(null);
            serviceNowLoginDialogController.alertLabel.setVisible(true);
            isValid = false;

            //Check if password is blank
        } else if (userPass.equals("")) {
            serviceNowLoginDialogController.alertLabel.setText(passwordError);
            serviceNowLoginDialogController.serviceNowWwidField.setStyle(null);
            serviceNowLoginDialogController.serviceNowPassField.setStyle("-fx-border-color: red;");
            serviceNowLoginDialogController.techNameComboBox.setStyle(null);
            serviceNowLoginDialogController.alertLabel.setVisible(true);
            isValid = false;

            //Check if tech name is null
        } else if (techName == null) {
            serviceNowLoginDialogController.alertLabel.setText(techNameError);
            serviceNowLoginDialogController.serviceNowWwidField.setStyle(null);
            serviceNowLoginDialogController.serviceNowPassField.setStyle(null);
            serviceNowLoginDialogController.techNameComboBox.setStyle("-fx-border-color: red;");
            serviceNowLoginDialogController.alertLabel.setVisible(true);
            isValid = false;

            //Set styles to default and hide label
        } else {
            serviceNowLoginDialogController.serviceNowWwidField.setStyle(null);
            serviceNowLoginDialogController.serviceNowPassField.setStyle(null);
            serviceNowLoginDialogController.techNameComboBox.setStyle(null);
            serviceNowLoginDialogController.alertLabel.setVisible(false);
        }

        return isValid;
    }

    public boolean addNewIssueValidation() {

        //Retrieve values from form fields
        String machineIssue = newIssueDialogController.newMachineIssue.getText();
        String troubleshootingSteps = newIssueDialogController.newTroubleshootingSteps.getText();
        String partNeeded = newIssueDialogController.newPartNeededComboBox.getValue();

        //Error String
        String machineIssueError = "* Please enter a machine issue";
        String troubleshootingStepsError = "* Please enter troubleshooting steps";
        String partNeededError = "* Please make a part selection";

        //Store the result of all checks
        boolean isValid = true;

        //Check if wwid is incorrect and password and techname are blank/null
        if (machineIssue.equals("") && troubleshootingSteps.equals("") && partNeeded == null) {
            newIssueDialogController.alertLabel.setText(machineIssueError + "\n" + troubleshootingStepsError + "\n" + partNeededError);
            newIssueDialogController.newMachineIssue.setStyle("-fx-border-color: red;");
            newIssueDialogController.newTroubleshootingSteps.setStyle("-fx-border-color: red;");
            newIssueDialogController.newPartNeededComboBox.setStyle("-fx-border-color: red;");
            newIssueDialogController.alertLabel.setVisible(true);
            isValid = false;

            //Check if wwid is incorrect and password is blank
        } else if (machineIssue.equals("") && troubleshootingSteps.equals("")) {
            newIssueDialogController.alertLabel.setText(machineIssueError + "\n" + troubleshootingStepsError);
            newIssueDialogController.newMachineIssue.setStyle("-fx-border-color: red;");
            newIssueDialogController.newTroubleshootingSteps.setStyle("-fx-border-color: red;");
            newIssueDialogController.newPartNeededComboBox.setStyle(null);
            newIssueDialogController.alertLabel.setVisible(true);
            isValid = false;

            //Check if wwid is incorrect and tech name is null
        } else if (machineIssue.equals("") && partNeeded == null) {
            newIssueDialogController.alertLabel.setText(machineIssueError + "\n" + partNeededError);
            newIssueDialogController.newMachineIssue.setStyle("-fx-border-color: red;");
            newIssueDialogController.newTroubleshootingSteps.setStyle(null);
            newIssueDialogController.newPartNeededComboBox.setStyle("-fx-border-color: red;");
            newIssueDialogController.alertLabel.setVisible(true);
            isValid = false;

            //Check if password is blank and tech name is null
        } else if (troubleshootingSteps.equals("") && partNeeded == null) {
            newIssueDialogController.alertLabel.setText(troubleshootingStepsError + "\n" + partNeededError);
            newIssueDialogController.newMachineIssue.setStyle(null);
            newIssueDialogController.newTroubleshootingSteps.setStyle("-fx-border-color: red;");
            newIssueDialogController.newPartNeededComboBox.setStyle("-fx-border-color: red;");
            newIssueDialogController.alertLabel.setVisible(true);
            isValid = false;

            //Check if wwid is incorrect
        } else if (machineIssue.equals("")) {
            newIssueDialogController.alertLabel.setText(machineIssueError);
            newIssueDialogController.newMachineIssue.setStyle("-fx-border-color: red;");
            newIssueDialogController.newTroubleshootingSteps.setStyle(null);
            newIssueDialogController.newPartNeededComboBox.setStyle(null);
            newIssueDialogController.alertLabel.setVisible(true);
            isValid = false;

            //Check if password is blank
        } else if (troubleshootingSteps.equals("")) {
            newIssueDialogController.alertLabel.setText(troubleshootingStepsError);
            newIssueDialogController.newMachineIssue.setStyle(null);
            newIssueDialogController.newTroubleshootingSteps.setStyle("-fx-border-color: red;");
            newIssueDialogController.newPartNeededComboBox.setStyle(null);
            newIssueDialogController.alertLabel.setVisible(true);
            isValid = false;

            //Check if tech name is null
        } else if (partNeeded == null) {
            newIssueDialogController.alertLabel.setText(partNeededError);
            newIssueDialogController.newMachineIssue.setStyle(null);
            newIssueDialogController.newTroubleshootingSteps.setStyle(null);
            newIssueDialogController.newPartNeededComboBox.setStyle("-fx-border-color: red;");
            newIssueDialogController.alertLabel.setVisible(true);
            isValid = false;

            //Set styles to default and hide label
        } else {
            newIssueDialogController.newMachineIssue.setStyle(null);
            newIssueDialogController.newTroubleshootingSteps.setStyle(null);
            newIssueDialogController.newPartNeededComboBox.setStyle(null);
            newIssueDialogController.alertLabel.setVisible(false);
        }

        return isValid;
    }

}
