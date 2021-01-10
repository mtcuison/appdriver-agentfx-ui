/**
* Object for token approval
* 
* @author  Michael Torres Cuison
* @version 1.0
* @since   2020-11-10 © Guanzon MIS-SEG 2018 and beyond 
*/

package org.rmj.appdriver.agentfx.token;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import org.rmj.appdriver.GRider;

public class TokenApproval extends Application {
    public static String pxeQuickSearch = "TokenApproval";
    public static String pxeQuickSearchScreen = "TokenApproval.fxml";
       
    private double xOffset = 0; 
    private double yOffset = 0;
   
    
    private static GRider poGRider;
    private static String psTableNme;
    private static String psTransNox;
    private static boolean pbApproved;
        
    public void setGRider(GRider foGRider){poGRider = foGRider;}
    public void setTableName(String fsValue){psTableNme = fsValue;}
    public void setTransNmbr(String fsValue){psTransNox = fsValue;}
   
    public boolean isApproved(){return pbApproved;}
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource(pxeQuickSearchScreen));

        /*SET PARAMETERS TO CLASS*/
        TokenApprovalController instance = new TokenApprovalController();
        instance.setGRider(poGRider);
        instance.setTableName(psTableNme);
        instance.setTransNmbr(psTransNox);
        
        fxmlLoader.setController(instance);
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
                primaryStage.setX(event.getScreenX() - xOffset);
                primaryStage.setY(event.getScreenY() - yOffset);
            }
        });

        Scene scene = new Scene(parent);
        primaryStage.setScene(scene);
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.initModality(Modality.APPLICATION_MODAL);
        primaryStage.getIcons().add(new Image("org/rmj/appdriver/agentfx/styles/64.png"));
        primaryStage.setTitle("Transaction Approval v1.0");
        primaryStage.showAndWait();
        
        pbApproved = instance.IsApproved();
    }

    public static void main(String[] args) {
        launch(args);
    }
}