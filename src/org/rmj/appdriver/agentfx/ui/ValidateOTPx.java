/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rmj.appdriver.agentfx.ui;

import javafx.application.Application;
import javafx.stage.Stage;
import org.json.simple.JSONObject;
import org.rmj.appdriver.GRider;

/**
 *
 * @author micha
 */
public class ValidateOTPx extends Application{    
    private static GRider oApp;
    private static boolean bOkay;
    private static String sCardNmbr;
    private static String sSourceNo;
    private static String sMobileNo;
    
    public void setGRider(GRider foValue){
        oApp = foValue;
    }
    
    public void setCardNumber(String fsValue){
        sCardNmbr = fsValue;
    }
    
    public void setSourceNo(String fsValue){
        sSourceNo = fsValue;
    }
    
    public void setMobileNo(String fsValue){
        sMobileNo = fsValue;
    }
    
    public boolean isOkay(){
        return bOkay;
    }
    
    @Override
    public void start(Stage stage) throws Exception {        
        JSONObject loJSON = new JSONObject();
        loJSON.put("sourceno", sSourceNo); //sSourceNo
        loJSON.put("cardnmbr", sCardNmbr); //sCardNmbr
        loJSON.put("mobileno", sMobileNo); //sMobileNo
        
        bOkay = showFXDialog.validateOTPx(oApp, loJSON);
    }
    
    public static void main(String [] args){
        launch(args);
    }
}
