package org.rmj.appdriver.agentfx.ui;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * System User Approval form controller
 *
 * @author Michael Torres Cuison
 * @since 2019.06.10
 */
class UpdateGCardNumberController implements Initializable {

    @FXML
    private Button btnOkay;
    @FXML
    private Button btnCancel;
    @FXML
    private TextField txtField01;
    @FXML
    private TextField txtField02;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        txtField01.setText(psCardNmbr);
        txtField02.requestFocus();
    } 
    
    @FXML
    private void btnOkay_Click(ActionEvent event) {      
        psNewCardNo = txtField02.getText();
        
        pbCancelled = false;
        unloadScene(event);
    }

    @FXML
    private void btnCancel_Click(ActionEvent event) {
        psNewCardNo = "";
        pbCancelled = true;
        unloadScene(event);
    }
    
    private void unloadScene(ActionEvent event){
        Node source = (Node)  event.getSource(); 
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }
    
    public boolean isCancelled(){return pbCancelled;}
    
    public void setApplicNo(String fsValue){psCardNmbr = fsValue;}
    public String getCardNmbr(){return psNewCardNo;}

    private boolean pbCancelled = true;
    private String psCardNmbr = "";
    private String psNewCardNo = "";
    
    private final String pxeModuleNsame = this.getClass().getSimpleName();
}
