package JosephSmith.model;

import JosephSmith.API.DispatchAPI;
import JosephSmith.view.*;
import jakarta.xml.soap.SOAPException;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InputValidator {

    //Error Strings
    private static final String wwidError = "* WWID must be 5 character and include 2 letters followed by 3 digits";
    private static final String passwordError = "* Please enter a password";

    /**
     * Check if the warranty form is valid
     * @param controller the controller containing the form
     * @return the boolean result of the check
     */
    public static boolean warrantyFormIsValid(HomePageController controller){

        String serviceTagRegex = "^[a-zA-Z0-9]{7}+$";
        String serialNumberRegex = "^[a-zA-Z0-9]{23}+$";

        //retrieve values from form fields
        String serviceTag = controller.serviceTagTextField.getText();
        String machineIssueSelection = controller.machineIssueComboBox.getValue();
        String serialNumber = controller.serialNumberTextField.getText();

        //Error Strings
        String serviceTagError = "* Service Tag must be 7 characters (a-z, A-Z, 0-9)";
        String serialNumberError = "* Serial Number must be 23 characters (a-z, A-Z, 0-9)";
        String machineIssueError = "* Please select a machine issue";

        //Regex to limit service tag input to exactly 7 alphanumeric characters
        Pattern serviceTagpattern = Pattern.compile(serviceTagRegex);
        Matcher matcher = serviceTagpattern.matcher(serviceTag);
        boolean serviceTagMatches = matcher.matches();

        //Regex to limit serial number to exactly 23 alphanumeric characters
        Pattern serialNumberPattern = Pattern.compile(serialNumberRegex);
        matcher = serialNumberPattern.matcher(serialNumber);
        boolean serialNumberMatches = matcher.matches();
        boolean serialNumberDisabled = controller.serialNumberTextField.isDisabled();

        //Store the result of all checks
        boolean isValid = true;

        //Check if service tag and serial number are incorrect
        if (!serviceTagMatches && !serialNumberDisabled && !serialNumberMatches){
            controller.alertLabel.setText(serviceTagError + "\n" + serialNumberError);
            controller.serviceTagTextField.setStyle("-fx-border-color: red;");
            controller.serialNumberTextField.setStyle("-fx-border-color: red;");
            controller.machineIssueComboBox.setStyle(null);
            controller.alertLabel.setVisible(true);
            isValid = false;

            //Check if service tag and machine issue selection are incorrect
        } else if (!serviceTagMatches && machineIssueSelection == null){
            controller.alertLabel.setText(serviceTagError + "\n" + machineIssueError);
            controller.serviceTagTextField.setStyle("-fx-border-color: red;");
            controller.serialNumberTextField.setStyle(null);
            controller.machineIssueComboBox.setStyle("-fx-border-color: red;");
            controller.alertLabel.setVisible(true);
            isValid = false;

            //Check if service tag is incorrect
        } else if (!serviceTagMatches){
            controller.alertLabel.setText(serviceTagError);
            controller.serviceTagTextField.setStyle("-fx-border-color: red;");
            controller.serialNumberTextField.setStyle(null);
            controller.machineIssueComboBox.setStyle(null);
            controller.alertLabel.setVisible(true);
            isValid = false;

            //Check if machine issue selection is incorrect
        } else if (machineIssueSelection == null){
            controller.alertLabel.setText(machineIssueError);
            controller.serviceTagTextField.setStyle(null);
            controller.serialNumberTextField.setStyle(null);
            controller.machineIssueComboBox.setStyle("-fx-border-color: red;");
            controller.alertLabel.setVisible(true);
            isValid = false;

            //Check if serial number is visible and incorrect
        } else if (!serialNumberDisabled && !serialNumberMatches){
            controller.alertLabel.setText(serialNumberError);
            controller.serviceTagTextField.setStyle(null);
            controller.serialNumberTextField.setStyle("-fx-border-color: red;");
            controller.machineIssueComboBox.setStyle(null);
            controller.alertLabel.setVisible(true);
            isValid = false;

            //Remove styles if all tests pass
        } else {
            controller.serviceTagTextField.setStyle(null);
            controller.serialNumberTextField.setStyle(null);
            controller.machineIssueComboBox.setStyle(null);
            controller.alertLabel.setVisible(false);

            controller.serviceTagTextField.requestFocus();
            //mainController.serialNumberTextField.setVisible(false);
            //mainController.serialNumberLabel.setVisible(false);
        }

        //Validate the input
        return isValid;
    }

    /**
     * Check if the tech direct login form is valid
     * @param controller the controller containing the form
     * @return the boolean result of the check
     * @throws IOException
     * @throws SOAPException
     */
    public static boolean techDirectLoginIsValid(TechDirectLoginDialogController controller) throws IOException, SOAPException {

        String wwidRegex = "^[a-zA-Z]{2}\\d{3}";
        
        //Retrieve values from form fields
        String userWWID = controller.techDirectWwidField.getText();
        String userPass = controller.techDirectPassField.getText();


        //Regex to limit WWID to alphanumeric characters with 2 letters followed by 3 digits
        Pattern wwidPattern = Pattern.compile(wwidRegex);
        Matcher matcher = wwidPattern.matcher(userWWID);
        boolean wwidMatches = matcher.matches();

        //Store the result of all checks
        boolean isValid = true;



        //Check if wwid is incorrect and password is blank
        if (!wwidMatches && userPass.equals("")) {
            controller.alertLabel.setText(wwidError + "\n" + passwordError);
            controller.techDirectWwidField.setStyle("-fx-border-color: red;");
            controller.techDirectPassField.setStyle("-fx-border-color: red;");
            controller.alertLabel.setVisible(true);
            isValid = false;

            //Check if wwid is incorrect
        } else if (!wwidMatches) {
            controller.alertLabel.setText(wwidError);
            controller.techDirectWwidField.setStyle("-fx-border-color: red;");
            controller.techDirectPassField.setStyle(null);
            controller.alertLabel.setVisible(true);
            isValid = false;

            //Check if password is blank
        } else if (userPass.equals("")) {
            controller.alertLabel.setText(passwordError);
            controller.techDirectWwidField.setStyle(null);
            controller.techDirectPassField.setStyle("-fx-border-color: red;");
            controller.alertLabel.setVisible(true);
            isValid = false;

            //Set styles to default and hide label
        } else if (!DispatchAPI.checkLogin(userWWID + "@cummins.com", userPass)){


        } else {
            controller.techDirectWwidField.setStyle(null);
            controller.techDirectPassField.setStyle(null);
            controller.alertLabel.setVisible(false);
        }
        return isValid;
    }

    /**
     * Check if the new issue form is valid
     * @param controller the controller containing the form
     * @return the boolean reuslt of the check
     */
    public static boolean newIssueFormIsValid(NewIssueController controller) {

        //Retrieve values from form fields
        String machineIssue = controller.newMachineIssue.getText();
        String troubleshootingSteps = controller.newTroubleshootingSteps.getText();
        String partNeeded = controller.newPartNeededComboBox.getValue();

        //Error String
        String machineIssueError = "* Please enter a machine issue";
        String troubleshootingStepsError = "* Please enter troubleshooting steps";
        String partNeededError = "* Please make a part selection";

        //Store the result of all checks
        boolean isValid = true;

        //Check if wwid is incorrect and password and techname are blank/null
        if (machineIssue.equals("") && troubleshootingSteps.equals("") && partNeeded == null) {
            controller.alertLabel.setText(machineIssueError + "\n" + troubleshootingStepsError + "\n" + partNeededError);
            controller.newMachineIssue.setStyle("-fx-border-color: red;");
            controller.newTroubleshootingSteps.setStyle("-fx-border-color: red;");
            controller.newPartNeededComboBox.setStyle("-fx-border-color: red;");
            controller.alertLabel.setVisible(true);
            isValid = false;

            //Check if wwid is incorrect and password is blank
        } else if (machineIssue.equals("") && troubleshootingSteps.equals("")) {
            controller.alertLabel.setText(machineIssueError + "\n" + troubleshootingStepsError);
            controller.newMachineIssue.setStyle("-fx-border-color: red;");
            controller.newTroubleshootingSteps.setStyle("-fx-border-color: red;");
            controller.newPartNeededComboBox.setStyle(null);
            controller.alertLabel.setVisible(true);
            isValid = false;

            //Check if wwid is incorrect and tech name is null
        } else if (machineIssue.equals("") && partNeeded == null) {
            controller.alertLabel.setText(machineIssueError + "\n" + partNeededError);
            controller.newMachineIssue.setStyle("-fx-border-color: red;");
            controller.newTroubleshootingSteps.setStyle(null);
            controller.newPartNeededComboBox.setStyle("-fx-border-color: red;");
            controller.alertLabel.setVisible(true);
            isValid = false;

            //Check if password is blank and tech name is null
        } else if (troubleshootingSteps.equals("") && partNeeded == null) {
            controller.alertLabel.setText(troubleshootingStepsError + "\n" + partNeededError);
            controller.newMachineIssue.setStyle(null);
            controller.newTroubleshootingSteps.setStyle("-fx-border-color: red;");
            controller.newPartNeededComboBox.setStyle("-fx-border-color: red;");
            controller.alertLabel.setVisible(true);
            isValid = false;

            //Check if wwid is incorrect
        } else if (machineIssue.equals("")) {
            controller.alertLabel.setText(machineIssueError);
            controller.newMachineIssue.setStyle("-fx-border-color: red;");
            controller.newTroubleshootingSteps.setStyle(null);
            controller.newPartNeededComboBox.setStyle(null);
            controller.alertLabel.setVisible(true);
            isValid = false;

            //Check if password is blank
        } else if (troubleshootingSteps.equals("")) {
            controller.alertLabel.setText(troubleshootingStepsError);
            controller.newMachineIssue.setStyle(null);
            controller.newTroubleshootingSteps.setStyle("-fx-border-color: red;");
            controller.newPartNeededComboBox.setStyle(null);
            controller.alertLabel.setVisible(true);
            isValid = false;

            //Check if tech name is null
        } else if (partNeeded == null) {
            controller.alertLabel.setText(partNeededError);
            controller.newMachineIssue.setStyle(null);
            controller.newTroubleshootingSteps.setStyle(null);
            controller.newPartNeededComboBox.setStyle("-fx-border-color: red;");
            controller.alertLabel.setVisible(true);
            isValid = false;

            //Set styles to default and hide label
        } else {
            controller.newMachineIssue.setStyle(null);
            controller.newTroubleshootingSteps.setStyle(null);
            controller.newPartNeededComboBox.setStyle(null);
            controller.alertLabel.setVisible(false);
        }

        return isValid;
    }

}
