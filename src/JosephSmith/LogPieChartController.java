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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
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

        //Get database connection
        DatabaseHelper database = new DatabaseHelper();
        database.connect();

        //Get unique parts list from LogSheet table
        ArrayList<String> uniqueParts = database.createUniqueValueList("Part_Needed", "LogSheet");

        //Populate list with count of each part
        ArrayList<Integer> uniquePartCount = new ArrayList<>();
        for (String uniquePart : uniqueParts) {
            uniquePartCount.add(database.getValueCount("Logsheet", "Part_Needed", uniquePart));
        }

        //Populate list of PieChart.Data objects
        ArrayList<PieChart.Data> pieChartDataTemp = new ArrayList<>();
        for (int i = 0; i < uniqueParts.size(); i++){
            pieChartDataTemp.add(new PieChart.Data(uniqueParts.get(i) + " - (" + uniquePartCount.get(i) + ")", uniquePartCount.get(i)));
        }

        //Convert pie chart data to observable list
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(pieChartDataTemp);

        //Set title, data, and legend position
        logPieChart.setTitle("Repairs By Part");
        logPieChart.setData(pieChartData);
        logPieChart.setLegendSide(Side.LEFT);
        database.closeConnection();

    }

    //Sort by model
    public void createModelPieChart(){

        //Get database connection
        DatabaseHelper database = new DatabaseHelper();
        database.connect();

        //Get unique models from Logsheet table
        ArrayList<String> uniqueModels = database.createUniqueValueList("Model", "Logsheet");

        //Populate list with count of each part
        ArrayList<Integer> uniqueModelCount = new ArrayList<>();
        for (String uniqueModel : uniqueModels){
            uniqueModelCount.add(database.getValueCount("LogSheet", "Model", uniqueModel));
        }

        //Populate list of PieChart.Data objects
        ArrayList<PieChart.Data> pieChartDataTemp = new ArrayList<>();
        for (int i = 0; i < uniqueModels.size(); i++){
            pieChartDataTemp.add(new PieChart.Data(uniqueModels.get(i) + " - (" + uniqueModelCount.get(i) + ")", uniqueModelCount.get(i)));
        }

        //Convert pie chart data to observable list
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(pieChartDataTemp);

        //Set title, data, and legend position
        logPieChart.setTitle("Repairs By Model");
        logPieChart.setData(pieChartData);
        logPieChart.setLegendSide(Side.LEFT);
        database.closeConnection();
    }
}
