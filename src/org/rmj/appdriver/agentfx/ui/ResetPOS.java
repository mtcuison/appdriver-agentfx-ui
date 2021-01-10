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
import org.rmj.appdriver.GRider;

class ResetPOS extends Application {
    public final static String pxeMainForm = "ResetPOS.fxml";
    public final static String pxeStageIcon = "org/rmj/appdriver/agentfx/styles/64.png";
    public final static String pxeMainFormTitle = "Reset Point-of-Sales";
    
    private double xOffset = 0; 
    private double yOffset = 0;
    
    private GRider poGRider;
    private boolean pbCancelled = true;
    private String psMessagex = "";
    
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource(pxeMainForm));
        
        //get the controller of the main interface
        ResetPOSController loControl = new ResetPOSController();
        loControl.setGRider(poGRider);
        
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
        
        if (!loControl.isCancelled()){
            pbCancelled = false;
        }
        psMessagex = loControl.getMessage();
    }

    public static void main(String[] args) {launch(args);}

    public void setGRider(GRider foGRider){poGRider = foGRider;}
    public boolean isCancelled(){return pbCancelled;}
    public String getMessage(){return psMessagex;}
}
