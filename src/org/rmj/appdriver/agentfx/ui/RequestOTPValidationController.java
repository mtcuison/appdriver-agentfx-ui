package org.rmj.appdriver.agentfx.ui;

import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agent.MsgBox;
import org.rmj.appdriver.agentfx.CommonUtils;
import org.rmj.appdriver.agentfx.ShowMessageFX;
import org.rmj.appdriver.agentfx.WebClient;

public class RequestOTPValidationController implements Initializable {
    @FXML
    private Button btnOkay;
    @FXML
    private Button btnCancel;
    @FXML
    private TextField txtField01;
    @FXML
    private Label lblReferNox;
    @FXML
    private Label lblMobileNo;
    @FXML
    private Button btnResend;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        txtField01.requestFocus();
        
        JSONObject loJSON = sendRequest();
        String lsResult = (String) loJSON.get("result");
        if (!lsResult.equalsIgnoreCase("success")){
            try {
                loJSON = (JSONObject) new JSONParser().parse(loJSON.get("error").toString());
                
                MsgBox.showOk((String) loJSON.get("message") + " Unable to send OTP.");
                System.exit(1);
            } catch (ParseException ex) {
                Logger.getLogger(RequestOTPValidationController.class.getName()).log(Level.SEVERE, null, ex);
                MsgBox.showOk(ex.getMessage());
                System.exit(1);
            }
            pbCancelled = true;
         } else
            psOTP = (String) loJSON.get("otpasswd");
        
        lblReferNox.setText((String) poTransx.get("sourceno"));
        lblMobileNo.setText((String) poTransx.get("mobileno"));
    }    

    @FXML
    private void btnOkay_Click(ActionEvent event) {
        if (txtField01.getText().isEmpty()){
            ShowMessageFX.Warning(getStage(), "OTP must not be empty.", "Warning", "Invalid OTP Detected.");
            return;
        }
        
        if (!txtField01.getText().equals(psOTP)){
            ShowMessageFX.Warning(getStage(), "OTP entered is not for this transaction. Please verify your entry.", "Warning", "Invalid OTP Detected.");
            return;
        }

        pbCancelled = false;
        unloadScene();
    }

    @FXML
    private void btnCancel_Click(ActionEvent event) {
        psMessagex = "OTP entry was cancelled.";
        
        pbCancelled = true;
        unloadScene();
    }
    
    @FXML
    private void btnResend_Click(ActionEvent event) {
        JSONObject loJSON = sendRequest();
        String lsResult = (String) loJSON.get("result");
        if (!lsResult.equalsIgnoreCase("success")){
            try {
                loJSON = (JSONObject) new JSONParser().parse(loJSON.get("error").toString());
                
                ShowMessageFX.Warning(getStage(), (String) loJSON.get("message"), "Warning", "Unable to send OTP.");
            } catch (ParseException ex) {
                Logger.getLogger(RequestOTPValidationController.class.getName()).log(Level.SEVERE, null, ex);
                ShowMessageFX.Error(getStage(), ex.getMessage(), "Error", "Parse Exception");
            }
        } else {
            psOTP = (String) loJSON.get("otpasswd");
            ShowMessageFX.Information(getStage(), (String) loJSON.get("message"), "Success", "Request to re-send OTP.");
        }
    }
    
    private void unloadScene(){
        Stage stage = (Stage) txtField01.getScene().getWindow();
        stage.close();
    }
    
    private Stage getStage(){
        return (Stage) txtField01.getScene().getWindow();
    }
    
    private JSONObject sendRequest(){
        if(poGRider == null){
            JSONObject err_detl = new JSONObject();
            err_detl.put("code", "100");
            err_detl.put("message", "UNSET Application Driver.");

            JSONObject err_mstr = new JSONObject();
            err_mstr.put("result", "error");
            err_mstr.put("error", err_detl);
            return err_mstr;
        }
        
        if(poTransx == null){
            JSONObject err_detl = new JSONObject();
            err_detl.put("code", "100");
            err_detl.put("message", "UNSET Transaction Detail Object.");

            JSONObject err_mstr = new JSONObject();
            err_mstr.put("result", "error");
            err_mstr.put("error", err_detl);
            return err_mstr;
        }
        
        String lsCardNmbr = (String) poTransx.get("cardnmbr");
        
        if (lsCardNmbr.isEmpty()){
            JSONObject err_detl = new JSONObject();
            err_detl.put("code", "100");
            err_detl.put("message", "UNSET Card Number.");

            JSONObject err_mstr = new JSONObject();
            err_mstr.put("result", "error");
            err_mstr.put("error", err_detl);
            return err_mstr;
        }
        
        String lsSourceNo = (String) poTransx.get("sourceno");
        
        if (lsSourceNo.isEmpty()){
            JSONObject err_detl = new JSONObject();
            err_detl.put("code", "100");
            err_detl.put("message", "UNSET Source Number.");

            JSONObject err_mstr = new JSONObject();
            err_mstr.put("result", "error");
            err_mstr.put("error", err_detl);
            return err_mstr;
        }
        
        //send otp request to API
        try{
            Calendar calendar = Calendar.getInstance();
            //Create the header section needed by the API
            Map<String, String> headers =
                    new HashMap<String, String>();
            headers.put("Accept", "application/json");
            headers.put("Content-Type", "application/json");
            headers.put("g-api-id", poGRider.getProductID());
            headers.put("g-api-imei", MiscUtil.getPCName());
            headers.put("g-api-key", SQLUtil.dateFormat(calendar.getTime(), "yyyyMMddHHmmss"));
            headers.put("g-api-hash", org.apache.commons.codec.digest.DigestUtils.md5Hex((String)headers.get("g-api-imei") + (String)headers.get("g-api-key")));
            headers.put("g-api-client", poGRider.getClientID());
            headers.put("g-api-user", poGRider.getUserID());
            headers.put("g-api-log", "");
            headers.put("g-api-token", "");
            headers.put("g-api-mobile", "");   
            
            JSONObject param = new JSONObject();
            param.put("cardnmbr", lsCardNmbr);
            param.put("sourceno", lsSourceNo);
            
            String sURL = CommonUtils.getConfiguration(poGRider, "WebSvr") + "gcard/ms/request_otp_validation.php";
            String response = WebClient.sendHTTP(sURL, param.toJSONString(), (HashMap<String, String>) headers);
            
            if(response == null){
                JSONObject err_detl = new JSONObject();
                err_detl.put("message", System.getProperty("store.error.info"));
                
                JSONObject err_mstr = new JSONObject();
                err_mstr.put("result", "ERROR");
                err_mstr.put("error", err_detl);
                return err_mstr;
            } 
            
            JSONParser oParser = new JSONParser();
            JSONObject json_obj = (JSONObject) oParser.parse(response);
            
            return json_obj;
         } catch (IOException ex) {
            JSONObject err_detl = new JSONObject();
            err_detl.put("message", "IO Exception: " + ex.getMessage());
            err_detl.put("code", "250");
            
            JSONObject err_mstr = new JSONObject();
            err_mstr.put("result", "ERROR");
            err_mstr.put("error", err_detl);
            return err_mstr;
        } catch (ParseException ex) {
            JSONObject err_detl = new JSONObject();
            err_detl.put("message", "Parse Exception: " + ex.getMessage());
            err_detl.put("code", "250");
            
            JSONObject err_mstr = new JSONObject();
            err_mstr.put("result", "ERROR");
            err_mstr.put("error", err_detl);
            return err_mstr;
        }  catch (Exception ex) {
            JSONObject err_detl = new JSONObject();
            err_detl.put("message", "Exception: " + ex.getMessage());
            err_detl.put("code", "250");
            
            JSONObject err_mstr = new JSONObject();
            err_mstr.put("result", "ERROR");
            err_mstr.put("error", err_detl);
            return err_mstr;
        }
    }
    
    public void setGRider(GRider foApp){poGRider = foApp;}
    public void setTransData(JSONObject foJSON){poTransx = foJSON;}
    
    public boolean isCancelled(){return pbCancelled;}
    public String getMessage(){return psMessagex;}
    
    private boolean pbCancelled = true;
    private String psMessagex = "";
    private String psOTP = "";
    private JSONObject poTransx = null;
    private GRider poGRider = null;
    
    private final String pxeModuleNsame = this.getClass().getSimpleName();
}
