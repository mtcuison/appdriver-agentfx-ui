/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rmj.appdriver.agentfx.ui;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.json.simple.JSONObject;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agentfx.CommonUtils;
import org.rmj.appdriver.agentfx.ShowMessageFX;

/**
 * FXML Controller class
 *
 * @author Mac
 */
public class ResetPOSController implements Initializable {

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
        txtField01.setOnKeyPressed(this::txtField_KeyPressed);
        txtField02.setOnKeyPressed(this::txtField_KeyPressed);
        
        txtField02.focusedProperty().addListener(txtField_Focus);
        
        txtField02.setText(CommonUtils.xsDateMedium(poGRider.getServerDate()));
        txtField01.requestFocus();
        
        pbLoaded = true;
    }    

    @FXML
    private void btnOkay_Click(ActionEvent event) {
        pbCancelled = !resetPOS();
            
        unloadScene();
    }

    @FXML
    private void btnCancel_Click(ActionEvent event) {
        psMessagex= "POS Reset was cancelled.";
        pbCancelled = true;
        unloadScene();
        
    }
    
    private void txtField_KeyPressed(KeyEvent event){
        TextField txtField = (TextField)event.getSource();
        
        switch(event.getCode()){
            case DOWN:
            case ENTER:
                CommonUtils.SetNextFocus(txtField);
                break;
            case UP:
                CommonUtils.SetPreviousFocus(txtField);
        }
    }
    
    private boolean resetPOS() {
        if (poGRider == null){
            psMessagex = "Application Driver is not set.";
            return false;
        }
        
        String lsResetKey = CommonUtils.getConfiguration(poGRider, "POSKey");
        String lsValue = txtField01.getText();
        
        
        if (!lsResetKey.equals(lsValue)){
            psMessagex = "Invalid RESET KEY detected.";
            return false;
        }
        
        String lsSQL;
        
        poGRider.beginTrans();
        
        lsSQL = "DELETE FROM Sales_Master";
        if (poGRider.executeQuery(lsSQL, "Sales_Master", poGRider.getBranchCode(), "") <= 0){
            if (!poGRider.getErrMsg().isEmpty()){
                psMessagex = poGRider.getErrMsg();
                poGRider.rollbackTrans();
                return false;
            }
        }
        
        lsSQL = "DELETE FROM Sales_Detail";
        if (poGRider.executeQuery(lsSQL, "Sales_Detail", poGRider.getBranchCode(), "") <= 0){
            if (!poGRider.getErrMsg().isEmpty()){
                psMessagex = poGRider.getErrMsg();
                poGRider.rollbackTrans();
                return false;
            }
        }
        
        lsSQL = "DELETE FROM Sales_Payment";
        if (poGRider.executeQuery(lsSQL, "Sales_Payment", poGRider.getBranchCode(), "") <= 0){
            if (!poGRider.getErrMsg().isEmpty()){
                psMessagex = poGRider.getErrMsg();
                poGRider.rollbackTrans();
                return false;
            }
        }
        
        lsSQL = "DELETE FROM Receipt_Master";
        if (poGRider.executeQuery(lsSQL, "Receipt_Master", poGRider.getBranchCode(), "") <= 0){
            if (!poGRider.getErrMsg().isEmpty()){
                psMessagex = poGRider.getErrMsg();
                poGRider.rollbackTrans();
                return false;
            }
        }
        
        lsSQL = "DELETE FROM Credit_Card_Trans";
        if (poGRider.executeQuery(lsSQL, "Credit_Card_Trans", poGRider.getBranchCode(), "") <= 0){
            if (!poGRider.getErrMsg().isEmpty()){
                psMessagex = poGRider.getErrMsg();
                poGRider.rollbackTrans();
                return false;
            }
        }
        
        lsSQL = "DELETE FROM Gift_Certificate_Trans";
        if (poGRider.executeQuery(lsSQL, "Gift_Certificate_Trans", poGRider.getBranchCode(), "") <= 0){
            if (!poGRider.getErrMsg().isEmpty()){
                psMessagex = poGRider.getErrMsg();
                poGRider.rollbackTrans();
                return false;
            }
        }
        
        lsSQL = "DELETE FROM Check_Payment_Trans";
        if (poGRider.executeQuery(lsSQL, "Check_Payment_Trans", poGRider.getBranchCode(), "") <= 0){
            if (!poGRider.getErrMsg().isEmpty()){
                psMessagex = poGRider.getErrMsg();
                poGRider.rollbackTrans();
                return false;
            }
        }  
        
        lsSQL = "DELETE FROM Charge_Invoice";
        if (poGRider.executeQuery(lsSQL, "Charge_Invoice", poGRider.getBranchCode(), "") <= 0){
            if (!poGRider.getErrMsg().isEmpty()){
                psMessagex = poGRider.getErrMsg();
                poGRider.rollbackTrans();
                return false;
            }
        } 
        
        lsSQL = "DELETE FROM Daily_Summary";
        if (poGRider.executeQuery(lsSQL, "Daily_Summary", poGRider.getBranchCode(), "") <= 0){
            if (!poGRider.getErrMsg().isEmpty()){
                psMessagex = poGRider.getErrMsg();
                poGRider.rollbackTrans();
                return false;
            }
        } 
        
        lsSQL = "DELETE FROM Event_Master";
        if (poGRider.executeQuery(lsSQL, "Event_Master", poGRider.getBranchCode(), "") <= 0){
            if (!poGRider.getErrMsg().isEmpty()){
                psMessagex = poGRider.getErrMsg();
                poGRider.rollbackTrans();
                return false;
            }
        } 
        
        lsSQL = "DELETE FROM Inv_Ledger";
        if (poGRider.executeQuery(lsSQL, "Inv_Ledger", poGRider.getBranchCode(), "") <= 0){
            if (!poGRider.getErrMsg().isEmpty()){
                psMessagex = poGRider.getErrMsg();
                poGRider.rollbackTrans();
                return false;
            }
        }
        
        lsSQL = "DELETE FROM Cash_Drawer";
        poGRider.executeQuery(lsSQL, "Cash_Drawer", poGRider.getBranchCode(), "");
        
        lsSQL = "UPDATE Inv_Serial SET cSoldStat = '0', cLocation = '1'";
        poGRider.executeQuery(lsSQL, "Inv_Serial", poGRider.getBranchCode(), "");
        
        int lnCtr = 0;
        int lnRow = 1;
        
        try {
            ResultSet loRS;
        
            lsSQL = "SELECT nZReadCtr FROM Cash_Reg_Machine WHERE sIDNumber = " + SQLUtil.toSQL(System.getProperty("pos.clt.crm.no"));
            loRS = poGRider.executeQuery(lsSQL);
            
            if (loRS.next()) lnCtr = loRS.getInt("nZReadCtr");
            
            lsSQL = "SELECT nEntryNox FROM Cash_Reg_Machine_Reset_History WHERE sIDNumber = " + SQLUtil.toSQL(System.getProperty("pos.clt.crm.no")) + " ORDER BY nEntryNox DESC LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            
            if (loRS.next()) lnRow = loRS.getInt("nEntryNox") + 1;
        } catch (SQLException ex) {
            Logger.getLogger(ResetPOSController.class.getName()).log(Level.SEVERE, null, ex);
            poGRider.rollbackTrans();
            return false;
        }
        
        lsSQL = "INSERT INTO Cash_Reg_Machine_Reset_History SET" +
                    "  sIDNumber = " + SQLUtil.toSQL(System.getProperty("pos.clt.crm.no")) +
                    ", nEntryNox = " + lnRow +
                    ", nLastZCtr = " + lnCtr +
                    ", dResetDte = " + SQLUtil.toSQL(poGRider.getServerDate()) +
                    ", sModified = " + SQLUtil.toSQL(poGRider.getUserID());
        if (poGRider.executeQuery(lsSQL, "Cash_Reg_Machine_Reset_History", poGRider.getBranchCode(), "") <= 0){
            if (!poGRider.getErrMsg().isEmpty()){
                psMessagex = poGRider.getErrMsg();
                poGRider.rollbackTrans();
                return false;
            }
        } 

        lsSQL = "UPDATE Cash_Reg_Machine SET" +
                    "  sORNoxxxx = ''" +
                    ", nSalesTot = 0.00" +
                    ", nVATSales = 0.00" +
                    ", nVATAmtxx = 0.00" +
                    ", nNonVATxx = 0.00" +
                    ", nZeroRatd = 0.00" +
                    ", nZReadCtr = 0" +
                    ", sTransNox = ''" +
                    ", sInvoicex = ''" +
                    ", nResetCtr = nResetCtr + 1" +
                    ", dPOSStart = " + SQLUtil.toSQL(SQLUtil.toDate(txtField02.getText(), "MMM dd, yyyy")) +
                " WHERE sIDNumber = " + SQLUtil.toSQL(System.getProperty("pos.clt.crm.no"));
        if (poGRider.executeQuery(lsSQL, "Cash_Reg_Machine", poGRider.getBranchCode(), "") <= 0){
            if (!poGRider.getErrMsg().isEmpty()){
                psMessagex = poGRider.getErrMsg();
                poGRider.rollbackTrans();
                return false;
            }
        } 
        
        lsSQL = "DELETE FROM Sales_Transaction";
        poGRider.executeQuery(lsSQL, "Sales_Transaction", poGRider.getBranchCode(), "");
        
        poGRider.commitTrans();
        
        return true;
    }
    
    private void unloadScene(){
        Stage stage = (Stage) txtField01.getScene().getWindow();
        stage.close();
    }
    
    private Stage getStage(){
        return (Stage) txtField01.getScene().getWindow();
    }
        
    public void setGRider(GRider foApp){poGRider = foApp;}
    public boolean isCancelled(){return pbCancelled;}
    public String getMessage(){return psMessagex;}
    
    private boolean pbCancelled = true;
    private String psMessagex = "";
    private GRider poGRider = null;
    
    private final String pxeDateFormat = "yyyy-MM-dd";
    private boolean pbLoaded = false;
    private int pnIndex = -1;
    
    private final String pxeModuleNsame = this.getClass().getSimpleName();
    
    final ChangeListener<? super Boolean> txtField_Focus = (o,ov,nv)->{
        if (!pbLoaded) return;
        
        TextField txtField = (TextField)((ReadOnlyBooleanPropertyBase)o).getBean();
        int lnIndex = Integer.parseInt(txtField.getId().substring(8, 10));
        String lsValue = txtField.getText();
        
        if (lsValue == null) return;
        
        if(!nv){ /*Lost Focus*/
            switch (lnIndex){
                case 2:
                   if(CommonUtils.isDate(txtField.getText(), pxeDateFormat)){
                        Date loDate = SQLUtil.toDate(txtField.getText(), pxeDateFormat);
                        
                        if (loDate.before(CommonUtils.dateAdd(poGRider.getServerDate(), 1)))
                            txtField.setText(CommonUtils.xsDateMedium(poGRider.getServerDate()));
                        else
                            txtField.setText(CommonUtils.xsDateMedium(CommonUtils.toDate(txtField.getText())));
                    }else{
                        txtField.setText(CommonUtils.xsDateMedium(poGRider.getServerDate()));
                    }
                   break;
                default:
                    ShowMessageFX.Warning(null, pxeModuleNsame, "Text field with name " + txtField.getId() + " not registered.");
            }
            pnIndex = lnIndex;
        }else{
            switch (lnIndex){
                case 2:
                    try{
                        txtField.setText(CommonUtils.xsDateShort(lsValue));
                    }catch(ParseException e){
                        ShowMessageFX.Error(getStage(), e.getMessage(), pxeModuleNsame, null);
                    }
                    txtField.selectAll();
                    break;
                default:
            }
            pnIndex = lnIndex;
            txtField.selectAll();
        }
    };
}
