package JosephSmith.view;

import com.jfoenix.controls.JFXButton;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.*;

public class AssetInformationController implements Initializable {
    @FXML
    public TextField serviceTagTextField;
    @FXML
    public ComboBox<String> requestComboBox;
    @FXML
    public JFXButton getInfoButton;
    @FXML
    public TextArea infoTextArea;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        requestComboBox.setItems(FXCollections.observableList(Arrays.asList("Asset Information", "Asset Entitlement", "Asset Details", "Asset Summary")));
        setEventHandlers();
    }

    private void setEventHandlers(){
/*        getInfoButton.setOnAction(e ->{
            ArrayList<String> serviceTagList = new ArrayList<>();
            serviceTagList.add(serviceTagTextField.getText());
            try {
                //infoTextArea.setText(DellAPI.getAssetInfo(serviceTagList, requestComboBox.getValue()));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });*/
    }
}
