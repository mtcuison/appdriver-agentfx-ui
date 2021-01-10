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
public class ValidateOTP extends Application{    
    private static GRider oApp;
    private static boolean bOkay;
    private static JSONObject oJSON;
    
    
    public void setGRider(GRider foValue){
        oApp = foValue;
    }
    
    public void setTransaction(JSONObject foJSON){
        oJSON = foJSON;
    }
    
    public boolean isOkay(){
        return bOkay;
    }
    
    @Override
    public void start(Stage stage) throws Exception {        
        bOkay = showFXDialog.validateOTP(oApp, oJSON);
    }
    
    public static void main(String [] args){
        launch(args);
    }
}
