package JosephSmith.view;

import JosephSmith.Database.Database;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class VisualLogController implements Initializable {

    @FXML
    public HBox pieChartLogUIButtonBox;
    @FXML
    public PieChart logPieChart;
    @FXML
    public Button pieChartSortButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        //Display the pie chart
        createPartNeededPieChart();

    }

    //Update pie chart when combo box selection is made
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

        //Get unique parts list from LogSheet table
        ObservableList<String> uniqueParts = Database.createUniqueValueList("Part_Needed", "MachineLog");

        //Populate list with count of each part
        ArrayList<Integer> uniquePartCount = new ArrayList<>();
        for (String uniquePart : uniqueParts) {
            uniquePartCount.add(Database.getValueCount("MachineLog", "Part_Needed", uniquePart));
        }

        //Populate list of PieChart.Data objects
        ArrayList<PieChart.Data> pieChartDataTemp = new ArrayList<>();
        for (int i = 0; i < uniqueParts.size(); i++){
            pieChartDataTemp.add(new PieChart.Data(uniqueParts.get(i) + " - (" + uniquePartCount.get(i) + ")", uniquePartCount.get(i)));
        }

        //Convert pie chart data to observable list
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(pieChartDataTemp);

        //Set data
        logPieChart.setData(pieChartData);
    }

    //Sort by model
    public void createModelPieChart(){

        //Get unique models from Logsheet table
       ObservableList<String> uniqueModels = Database.createUniqueValueList("Model", "MachineLog");

        //Populate list with count of each part
        ArrayList<Integer> uniqueModelCount = new ArrayList<>();
        for (String uniqueModel : uniqueModels){
            uniqueModelCount.add(Database.getValueCount("MachineLog", "Model", uniqueModel));
        }

        //Populate list of PieChart.Data objects
        ArrayList<PieChart.Data> pieChartDataTemp = new ArrayList<>();
        for (int i = 0; i < uniqueModels.size(); i++){
            pieChartDataTemp.add(new PieChart.Data(uniqueModels.get(i) + " - (" + uniqueModelCount.get(i) + ")", uniqueModelCount.get(i)));
        }

        //Convert pie chart data to observable list
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(pieChartDataTemp);

        //Set data
        logPieChart.setData(pieChartData);

    }
}
