package JosephSmith.view;

import JosephSmith.Database.Database;
import JosephSmith.API.DispatchAPI;
import JosephSmith.Main;
import JosephSmith.model.*;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXDrawer;
import com.jfoenix.controls.JFXHamburger;
import com.jfoenix.transitions.hamburger.HamburgerBackArrowBasicTransition;
import jakarta.xml.soap.SOAPException;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.IntegerBinding;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class HomePageController implements Initializable {
    @FXML
    public Button submitMachineButton;
    @FXML
    public Button initiateWarrantyButton;
    @FXML
    public ComboBox<String> machineIssueComboBox;
    @FXML
    public TextField serviceTagTextField;
    @FXML
    public TextField serialNumberTextField;
    @FXML
    public Label serialNumberLabel;
    @FXML
    public Label queueCountLabel;
    @FXML
    public Label alertLabel;
    @FXML
    public TableView<DispatchMachine> dispatchMachineTableView;
    @FXML
    public TableColumn<DispatchMachine, String> serviceTagColumn;
    @FXML
    public TableColumn<DispatchMachine, String> machineIssueColumn;
    @FXML
    public TableColumn<DispatchMachine, String> troubleshootingStepsColumn;
    @FXML
    public TableColumn<DispatchMachine, String> partNeededColumn;
    @FXML
    public TableColumn<DispatchMachine, String> serialNumberColumn;
    @FXML
    public JFXDrawer menuDrawer;
    @FXML
    public BorderPane homePageBorderPane;
    @FXML
    public JFXHamburger hamburger;
    @FXML
    public GridPane warrantyFormGridPane;
    @FXML
    public JFXButton warrantyMenuButton;
    @FXML
    public JFXButton logMenuButton;
    @FXML
    public JFXButton visualMenuButton;
    @FXML
    public JFXButton newIssueMenuButton;
    @FXML
    public JFXButton removeDispatchMachine;
    @FXML
    public VBox menuVbox;
    @FXML
    public Label machineIssueLabel;
    @FXML
    public JFXComboBox<String> partNeededComboBox;
    @FXML
    public Label partNeededLabel;
    @FXML
    public VBox warrantyFormVBox;
    @FXML
    public JFXButton dispatchFormButton;
    @FXML
    public VBox techDirectSubMenuVBox;
    @FXML
    public JFXButton assetInfoMenuButton;
    @FXML
    public Label quantityLabel;

    //Store dispatch machines
    ObservableList<DispatchMachine> dispatchMachines = FXCollections.observableArrayList();

    //Create bindings for queue label
    private final IntegerBinding dispatchListSize = Bindings.size(dispatchMachines);
    private final BooleanBinding dispatchListPopulated = dispatchListSize.greaterThan(0);

    public static Alert databaseAlert = new Alert(Alert.AlertType.ERROR);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setMenuDrawer();
        setBindings();
        setEventListeners();
        setCellFactories();

        //Populate machine issue combo box
        partNeededComboBox.setItems(Database.createUniqueValueList("Part_Needed", "IssueDescriptions"));

        //Remove part needed and serial number fields
        warrantyFormVBox.getChildren().removeAll(serialNumberLabel, serialNumberTextField, machineIssueLabel, machineIssueComboBox);
        serialNumberTextField.setDisable(true);


    }

    /** Set the transition and content for the side menu */
    public void setMenuDrawer(){

        //Set to null to allow grid pane to fill screen when drawer closes
        homePageBorderPane.setLeft(null);
        HamburgerBackArrowBasicTransition burgerTransition = new HamburgerBackArrowBasicTransition(hamburger);
        burgerTransition.setRate(-1);
        hamburger.addEventHandler(MouseEvent.MOUSE_CLICKED, (event -> {
            burgerTransition.setRate(burgerTransition.getRate() * -1);
            burgerTransition.play();

            //Set left border pane to null once drawer closes
            if (menuDrawer.isOpened()){
                menuDrawer.close();
                menuDrawer.onDrawerClosedProperty().set(event1 -> homePageBorderPane.setLeft(null));
            } else {
                menuDrawer.open();
                homePageBorderPane.setLeft(menuDrawer);
            }
        }));

        //Remove tech direct submenu buttons
        menuVbox.getChildren().removeAll(techDirectSubMenuVBox);

        //Set submenu button sizes
        logMenuButton.prefHeightProperty().bind(warrantyMenuButton.heightProperty().multiply(0.6));
        newIssueMenuButton.prefHeightProperty().bind(warrantyMenuButton.heightProperty().multiply(0.6));
        visualMenuButton.prefHeightProperty().bind(warrantyMenuButton.heightProperty().multiply(0.6));
        dispatchFormButton.prefHeightProperty().bind(warrantyMenuButton.heightProperty().multiply(0.6));
        assetInfoMenuButton.prefHeightProperty().bind(warrantyMenuButton.heightProperty().multiply(0.6));
    }

    /** Set the event listeners */
    public void setEventListeners() {

        //Update dispatch machine table to contents of dispatch machines
        dispatchMachines.addListener((ListChangeListener<DispatchMachine>) change -> dispatchMachineTableView.setItems(dispatchMachines));

        //Open and Close Tech Direct Submenu
        warrantyMenuButton.setOnAction(e -> {
            if (menuVbox.getChildren().contains(techDirectSubMenuVBox)){
                menuVbox.getChildren().removeAll(techDirectSubMenuVBox);
            } else {
                menuVbox.getChildren().add(2, techDirectSubMenuVBox);
            }
        });

        //Open Dispatch Form
        dispatchFormButton.setOnAction( e -> homePageBorderPane.setCenter(warrantyFormGridPane));

        //Open Dispatch Log
        logMenuButton.setOnAction(e ->{
            try {
                homePageBorderPane.setCenter(FXMLLoader.load(getClass().getResource("MachineLog.fxml")));
            } catch (IOException ex) {
                ex.printStackTrace();
            }

        });

        //Open Visual Log
        visualMenuButton.setOnAction(e ->{
            try {
                homePageBorderPane.setCenter(FXMLLoader.load(getClass().getResource("VisualLog.fxml")));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        //Open add new issue form
        newIssueMenuButton.setOnAction(e ->{
            try {
                homePageBorderPane.setCenter(FXMLLoader.load(getClass().getResource("NewIssue.fxml")));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        //Open Asset Information
        assetInfoMenuButton.setOnAction(e ->{
            try {
                homePageBorderPane.setCenter(FXMLLoader.load(getClass().getResource("AssetInformation.fxml")));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        //Display login dialog on initiate warranty
       initiateWarrantyButton.setOnAction(e -> displayTechDirectLogin());

        //Validate input on submit machine
        submitMachineButton.setOnAction(e -> addDispatchMachine());

        //Set event for part needed combo box change
        partNeededComboBox.setOnAction(e -> handlePartComboBoxChange());

        //Set event for machine issue combo box
        machineIssueComboBox.setOnAction(e ->{
            if (partNeededComboBox.getValue() != null && partNeededComboBox.getValue().equals("Battery")){
                serialNumberTextField.setDisable(false);
            }
        });

        //Remove warranty machines from the queue
        removeDispatchMachine.setOnAction(e -> removeDispatchMachine());
    }

    /** Set the cell factories for the dispatch machine table*/
    public void setCellFactories() {

        //Set table selection to multiple
        dispatchMachineTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        //Set cell factories for table
        serviceTagColumn.setCellValueFactory(
                new PropertyValueFactory<>("serviceTag"));
        machineIssueColumn.setCellValueFactory(
                new PropertyValueFactory<>("machineIssue"));

        //Set the troubleshooting steps column to wrap text
        machineIssueColumn.setCellFactory(param -> {
            TableCell<DispatchMachine, String> cell = new TableCell<>();
            Text text = new Text();
            text.setFill(Color.WHITE);
            cell.setGraphic(text);
            cell.setPadding(new Insets(10, 10, 10, 10));
            cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
            text.wrappingWidthProperty().bind(machineIssueColumn.widthProperty().divide(1.1));
            text.textProperty().bind(cell.itemProperty());
            return cell;
        });

        troubleshootingStepsColumn.setCellValueFactory(
                new PropertyValueFactory<>("troubleshootingSteps"));

        //Set the troubleshooting steps column to wrap text
        troubleshootingStepsColumn.setCellFactory(param -> {
            TableCell<DispatchMachine, String> cell = new TableCell<>();
            Text text = new Text();
            text.setFill(Color.WHITE);
            cell.setGraphic(text);
            cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
            text.wrappingWidthProperty().bind(troubleshootingStepsColumn.widthProperty().divide(1.1));
            text.textProperty().bind(cell.itemProperty());
            return cell;
        });

        partNeededColumn.setCellValueFactory(
                new PropertyValueFactory<>("partNeeded"));
        serialNumberColumn.setCellValueFactory(
                new PropertyValueFactory<>("serialNumber"));

        serialNumberColumn.setCellFactory( tc -> new TableCell<DispatchMachine, String>(){
            @Override
            protected void updateItem(String serialNumber, boolean empty) {
                super.updateItem(serialNumber, empty);

                if (empty){
                    setText(null);
                } else if (serialNumber.equals("")){
                    setText("Serial Not Required");
                } else {
                    setText(serialNumber);
                }
            }
        });
        //Set the troubleshooting steps column to wrap text
        serialNumberColumn.setCellFactory(param -> {
            TableCell<DispatchMachine, String> cell = new TableCell<>();
            Text text = new Text();
            text.setFill(Color.WHITE);
            cell.setGraphic(text);
            cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
            text.wrappingWidthProperty().bind(serialNumberColumn.widthProperty().divide(1.1));
            text.textProperty().bind(cell.itemProperty());
            return cell;
        });

    }

    /** Handle part combo box change events */
    private void handlePartComboBoxChange(){

        //Check if value present
        if (partNeededComboBox.getValue() != null) {

            //Check if part is serializable
            if (partNeededComboBox.getValue().equals("Battery") || partNeededComboBox.getValue().equals("Display, Monitor")) {
                warrantyFormVBox.getChildren().removeAll(serialNumberTextField, serialNumberLabel, machineIssueLabel, machineIssueComboBox);
                warrantyFormVBox.getChildren().add(4, machineIssueLabel);
                warrantyFormVBox.getChildren().add(5, machineIssueComboBox);
                warrantyFormVBox.getChildren().add(6, serialNumberLabel);
                warrantyFormVBox.getChildren().add(7, serialNumberTextField);
                serialNumberTextField.setDisable(false);
                machineIssueComboBox.setItems(Database.getMachineIssues(partNeededComboBox.getValue()));

                //Select item if only one option is present
                if (machineIssueComboBox.getItems().size() == 1){
                    machineIssueComboBox.getSelectionModel().select(0);
                }
            } else {
                warrantyFormVBox.getChildren().removeAll(serialNumberTextField, serialNumberLabel, machineIssueLabel, machineIssueComboBox);
                warrantyFormVBox.getChildren().add(4, machineIssueLabel);
                warrantyFormVBox.getChildren().add(5, machineIssueComboBox);
                machineIssueComboBox.setItems(Database.getMachineIssues(partNeededComboBox.getValue()));
                serialNumberTextField.setDisable(true);

                //Select item if only one
                if (machineIssueComboBox.getItems().size() == 1){
                    machineIssueComboBox.getSelectionModel().select(0);
                }
            }
        } else
            warrantyFormVBox.getChildren().removeAll(serialNumberTextField, serialNumberLabel, machineIssueLabel, machineIssueComboBox);
        serialNumberTextField.setDisable(true);
    }

    /** Set bindings */
    private void setBindings(){
        quantityLabel.visibleProperty().bind(dispatchListPopulated);
        queueCountLabel.visibleProperty().bind(dispatchListPopulated);
        queueCountLabel.textProperty().bind(dispatchListSize.asString());
    }

    /** Add new dispatch machine to the table */
    public void addDispatchMachine() {

        if (InputValidator.warrantyFormIsValid(this)) {

            //Add warranty machine to list
            dispatchMachines.add(Database.createDispatchMachine(serviceTagTextField.getText(),
                    serialNumberTextField.getText(),
                    machineIssueComboBox.getValue()));

            //Reset form
            resetForm();
        }

    }

    /** Remove dispatch machine from the table and database */
    private void removeDispatchMachine(){
        //Remove selected items from WarrantyMachines Table and List View
        ObservableList<DispatchMachine> selectedDispatchMachines = dispatchMachineTableView.getSelectionModel().getSelectedItems();

        //Remove from warranty machine table
        dispatchMachines.removeAll(selectedDispatchMachines);

        //Clear any selections
        dispatchMachineTableView.getSelectionModel().clearSelection();

    }

    /** Display Tach Direct Login Dialog */
    private void displayTechDirectLogin(){
        //Write warranty machines to database table
        Database.addDispatchMachines(dispatchMachines);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("TechDirectLoginDialog.fxml"));
            Parent root;
            root = loader.load();

            TechDirectLoginDialogController controller = loader.getController();
            controller.mainController = this;

            Stage stage = new Stage();
            Scene scene = new Scene(root);

            //Add stylesheet to dialog
            scene.getStylesheets().add(Main.stylesheetUrl.toExternalForm());

            stage.setScene(scene);
            stage.setResizable(false);
            stage.setTitle("Dell Tech Direct Login");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /** Reset the form on submit */
    private void resetForm(){
        partNeededComboBox.getSelectionModel().clearSelection();
        machineIssueComboBox.getSelectionModel().clearSelection();
        serviceTagTextField.clear();
        serialNumberTextField.clear();
        warrantyFormVBox.getChildren().removeAll(serialNumberTextField, serialNumberLabel, machineIssueLabel, machineIssueComboBox);
    }

    /** Submit dispatch machine to the self-dispatch API */
    public void submitDispatches(String techDirectEmail, String techDirectPass) throws IOException, SOAPException {


        //Create a hash set to store the distinct service tags
        Set<String> distinctServiceTags = new HashSet<>();

        //Loop through warranty machine and add unique values
        for (DispatchMachine machine : dispatchMachines){
            distinctServiceTags.add(machine.getServiceTag());
        }

        //Loop through dispatch machines
        for (String serviceTag : distinctServiceTags) {


            //Store multiple issues for a single service tag
            ArrayList<DispatchMachine> multiplePartMachineList = new ArrayList<>();

            //Add issues for the same service tag to the list
            for (DispatchMachine machine : dispatchMachines){
                if (machine.getServiceTag().equals(serviceTag)){
                    multiplePartMachineList.add(machine);
                }
            }

            //Create dispatch and get dispatch code
            String dispatchCode = DispatchAPI.createDispatch(multiplePartMachineList, techDirectEmail, techDirectPass);

            //Check if dispatch code is returned
            if (!dispatchCode.equals("")) {

                //Get machine model
                String model = DispatchAPI.getMachineModel(multiplePartMachineList.get(0).getServiceTag());

                //Log the machine and each issue warrantied
                for (DispatchMachine machine : multiplePartMachineList) {

                    //Log machine if dispatch successful
                    addEntryToLog(machine, dispatchCode, model);
                }

                //Remove all machines of current service tag from list
                dispatchMachines.removeAll(multiplePartMachineList);

            }
        }
    }

    /** Add new log entry */
    private void addEntryToLog(DispatchMachine machine, String dispatchCode, String model){
        Database.addMachineToLog(new LogEntry(LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")),
                                                    dispatchCode, machine.getServiceTag(), model, machine.getMachineIssue(),
                                                    machine.getPartNeeded()));
    }
}

