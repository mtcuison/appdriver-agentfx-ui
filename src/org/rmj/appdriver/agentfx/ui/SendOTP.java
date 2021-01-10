/**
 * @author  Michael Cuison
 * 
 * @since    2019-06-10
 */

package org.rmj.appdriver.agentfx.ui;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.json.simple.JSONObject;
import org.rmj.appdriver.GRider;

public class SendOTP extends Application {
    public final static String pxeMainForm = "RequestOTP.fxml";
    public final static String pxeStageIcon = "org/rmj/appdriver/agentfx/styles/64.png";
    public final static String pxeMainFormTitle = "One-Time-Pin";
    
    private double xOffset = 0; 
    private double yOffset = 0;
    
    private static JSONObject poTransx = null;
    private static boolean pbCancelled = true;
    private static GRider poGRider = null;
    
    public static void main(String [] args){launch(args);}
    
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource(pxeMainForm));
        
        //get the controller of the main interface
        RequestOTPController loControl = new RequestOTPController();
        loControl.setGRider(poGRider);
        loControl.setTransData(poTransx);

        //the controller class to the main interface
        fxmlLoader.setController(loControl);
        
        //load the main interface
        Parent parent = fxmlLoader.load();
        
        parent.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            }
        });
        parent.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);
            }
        });
        
        //set the main interface as the scene
        Scene scene = new Scene(parent);
        
        stage.setScene(scene);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.getIcons().add(new Image(pxeStageIcon));
        stage.setTitle(pxeMainFormTitle);
        stage.showAndWait();
        
        pbCancelled = loControl.isCancelled();
    }
    
    public boolean isCancelled(){return pbCancelled;}
    public void setGRider(GRider foGRider){poGRider = foGRider;}
    public void setTransaction(JSONObject foJSON){poTransx = foJSON;}
}
