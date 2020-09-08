package JosephSmith;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InputValidator {
    MainUIGridPaneController mainController;
    ServiceNowController serviceNowController;
    TechDirectLoginDialogController techDirectLoginDialogController;
    ServiceNowLoginDialogController serviceNowLoginDialogController;

    //Constructors with each controller
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
        String serviceTagRegex = "^[a-zA-Z0-9]{7}+$";
        Pattern serviceTagpattern = Pattern.compile(serviceTagRegex);
        Matcher matcher = serviceTagpattern.matcher(serviceTag);
        boolean serviceTagMatches = matcher.matches();


        String serialNumberRegex = "^[a-zA-Z0-9]{23}+$";
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
        }

        //Validate the input
        return isValid;
    }
    /*
    public boolean techDirectLoginValidation(){

    }

     */

    public boolean serviceNowLoginValidation() {

        //Retrieve values from form fields
        String userWWID = serviceNowLoginDialogController.serviceNowWwidField.getText();
        String userPass = serviceNowLoginDialogController.serviceNowPassField.getText();
        String techName = serviceNowLoginDialogController.techNameComboBox.getValue();

        //Error Strings
        String wwidError = "* WWID must be 5 character and include 2 letters followed by 3 digits";
        String passwordError = "* Please enter a password";
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

    /*
    public boolean serviceNowFormValidation(){

    }

     */


}
