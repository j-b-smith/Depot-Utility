package JosephSmith;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LogPieChartController implements Initializable {

    public Button returnToLog;
    public HBox pieChartLogUIButtonBox;
    public PieChart logPieChart;
    public Button pieChartSortButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        //Display the pie chart
        createPartNeededPieChart();

    }

    //Display log when return to log button is pressed
    @FXML
    private void displayLog() throws IOException {
        try {
            Stage stage = (Stage) returnToLog.getScene().getWindow();
            GridPane root = FXMLLoader.load(getClass().getResource("logSheetUI.fxml"));
            Scene scene = new Scene(root, 1200, 700);
            stage.setScene(scene);
        } catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    //Update pie chart when combo box selection is made
    @FXML
    public void onSortButtonClick(){
        if (pieChartSortButton.getText().equals("Sort By Model")){
            createModelPieChart();
            pieChartSortButton.setText("Sort By Part Needed");
        } else {
            createPartNeededPieChart();
            pieChartSortButton.setText("Sort By Model");
        }
    }

    //Sort by part
    public void createPartNeededPieChart(){

        DatabaseHelper database = new DatabaseHelper();
        database.connect();

        //Get part counts
        int systemBoardCount = database.getValueCount("LogSheet", "Part_Needed", "System Board / Motherboard");
        int lcdKitCount = database.getValueCount("LogSheet", "Part_Needed", "LCD, Kit");
        int batteryCount = database.getValueCount("LogSheet", "Part_Needed", "Battery, Removable");
        int webcamCount = database.getValueCount("LogSheet", "Part_Needed", "Camera/Webcam");
        int keyboardCount = database.getValueCount("LogSheet", "Part_Needed", "Keyboard, Portable Internal");
        int videoCardCount = database.getValueCount("LogSheet", "Part_Needed", "Expansion Card, Video");
        int palmRestCount = database.getValueCount("LogSheet", "Part_Needed", "Palm Rest (incl Touch Pad)");
        int speakersCount = database.getValueCount("LogSheet", "Part_Needed", "Speakers, Internal Notebook");
        int dcCount = database.getValueCount("LogSheet", "Part_Needed", "DC-in connector");
        int coolingFanCount = database.getValueCount("LogSheet", "Part_Needed", "Cooling Fan");
        int ramCount = database.getValueCount("LogSheet", "Part_Needed", "Memory, SIMM/DIMM/CRIMM");
        int ssdCount = database.getValueCount("LogSheet", "Part_Needed", "Storage, Hard Drive, SSD");
        int processorFanCount = database.getValueCount("LogSheet", "Part_Needed", "Processor Fan");

        //Create data for pie chart
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                        new PieChart.Data("System Board / Motherboard - (" + systemBoardCount + ")", systemBoardCount),
                        new PieChart.Data("LCD, Kit - (" + lcdKitCount + ")", lcdKitCount),
                        new PieChart.Data("Battery, Removable - (" + batteryCount + ")", batteryCount),
                        new PieChart.Data("Camera/Webcam - (" + webcamCount + ")", webcamCount),
                        new PieChart.Data("Keyboard, Portable Internal - (" + keyboardCount + ")", keyboardCount),
                        new PieChart.Data("Expansion Card, Video - (" + videoCardCount + ")", videoCardCount),
                        new PieChart.Data("Palm Rest (incl Touch Pad) - (" + palmRestCount + ")", palmRestCount),
                        new PieChart.Data("Speakers, Internal Notebook - (" + speakersCount + ")", speakersCount),
                        new PieChart.Data("DC-in connector - (" + dcCount + ")", dcCount),
                        new PieChart.Data("Cooling Fan - (" + coolingFanCount + ")", coolingFanCount),
                        new PieChart.Data("Memory, SIMM/DIMM/CRIMM - (" + ramCount + ")", ramCount),
                        new PieChart.Data("Storage, Hard Drive, SSD - (" + ssdCount + ")", ssdCount),
                        new PieChart.Data("Processor Fan - (" + processorFanCount + ")", processorFanCount)
                );

        //Set title, data, and legend position
        logPieChart.setTitle("Repairs By Part");
        logPieChart.setData(pieChartData);
        logPieChart.setLegendSide(Side.LEFT);
        database.closeConnection();

    }

    //Sort by model
    public void createModelPieChart(){

        DatabaseHelper database = new DatabaseHelper();
        database.connect();

        //Get model counts
        int model_7480 = database.getValueCount("LogSheet", "Model", "Latitude 7480");
        int model_7490 = database.getValueCount("LogSheet", "Model", "Latitude 7490");
        int model_5520 = database.getValueCount("LogSheet", "Model", "Precision 5520");
        int model_5530 = database.getValueCount("LogSheet", "Model", "Precision 5530");
        int model_7720 = database.getValueCount("LogSheet", "Model", "Precision 7720");
        int model_7730 = database.getValueCount("LogSheet", "Model", "Precision 7730");
        int model_5290 = database.getValueCount("LogSheet", "Model", "Latitude 5290 2-in-1");
        int model_7414 = database.getValueCount("LogSheet", "Model", "Latitude 14 rugged extreme");

        //Create data for pie chart
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Latitude 7480 - (" + model_7480 + ")", model_7480),
                new PieChart.Data("Latitude 7490 - (" + model_7490 + ")", model_7490),
                new PieChart.Data("Precision 5520 - (" + model_5520 + ")", model_5520),
                new PieChart.Data("Precision 5530 - (" + model_5530 + ")", model_5530),
                new PieChart.Data("Precision 7720 - (" + model_7720 + ")", model_7720),
                new PieChart.Data("Precision 7730 - (" + model_7730 + ")", model_7730),
                new PieChart.Data("Latitude 5290 2-in-1 - (" + model_5290 + ")", model_5290),
                new PieChart.Data("Latitude 14 rugged extreme - (" + model_7414 + ")", model_7414)
        );

        //Set title, data, and legend position
        logPieChart.setTitle("Repairs By Model");
        logPieChart.setData(pieChartData);
        logPieChart.setLegendSide(Side.LEFT);
        database.closeConnection();
    }
}
