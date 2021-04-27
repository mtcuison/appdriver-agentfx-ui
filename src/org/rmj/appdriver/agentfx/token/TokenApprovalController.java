package org.rmj.appdriver.agentfx.token;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.Tokenize;
import org.rmj.appdriver.agent.MsgBox;
import org.rmj.appdriver.agentfx.ShowMessageFX;
import org.rmj.appdriver.agentfx.service.ITokenize;
import org.rmj.appdriver.agentfx.service.TokenApprovalFactory;
import org.rmj.appdriver.agentfx.service.TokenRestAPI;
import org.rmj.appdriver.agentfx.ui.showFXDialog;
import org.rmj.appdriver.constants.TransactionStatus;

/**
 * FXML Controller class
 *
 * @author Michael Cuison
 *      2020.12.01 - Started creating this UI
 */
public class TokenApprovalController implements Initializable {
    @FXML
    private Button btnCancel;
    @FXML
    private Label lblRequested;
    @FXML
    private Button btnApprove;
    @FXML
    private Button btnSMSRequest;
    @FXML
    private Label lblRequestType;
    @FXML
    private Label lblRequestedTo;
    @FXML
    private Button btnCheckRequest;
    @FXML
    private TableView<TableModel> table;

    @Override
    public void initialize(URL url, ResourceBundle rb) {      
        if (poGRider == null){
            System.err.println("Application Driver is not set.");
            System.exit(1);
        }
        
        if (psTableNme.isEmpty()){
            System.err.println("TABLE requesting for APPROVAL is NOT SET.");
            System.exit(1);
        }
        
        if (psTransNox.isEmpty()){
            System.err.println("TABLE TRANSACTION NO. requesting for APPROVAL is NOT SET.");
            System.exit(1);
        }
        
        poApproval = TokenApprovalFactory.make(psTableNme);
        poApproval.setGRider(poGRider);
        poApproval.setTransNmbr(psTransNox);
        
        initGrid();
        clearFields();
        
        loadDetail();
        
        pbApproved = false;
    }    

    @FXML
    private void btnCancel_Click(ActionEvent event) {
        unloadScene(event);
    }
    
    @FXML
    private void btnApprove_Click(ActionEvent event) {
        String lsEmployID = (String) poData.get("sReqstdTo");
        
        JSONObject loJSON = showFXDialog.getApproval(poGRider, getStage());
        
        if (loJSON == null) return;
        
        if (!lsEmployID.equals((String) loJSON.get("sEmployID"))){
            ShowMessageFX.Warning(getStage(), "Approving officer account is NOT SAME from requested officer.", pxeModuleName, "Warning!!!");
            return;
        }
        
        //upload request
        String lsTransNox = poApproval.uploadCodeRequest((String) poData.get("sTransNox"), "0");
        
        if (lsTransNox.isEmpty()){
            ShowMessageFX.Warning(getStage(), poApproval.getMessage(), pxeModuleName, "Warning!!!");
            return;
        }
        
        //approve/disapprove request
        if (!poApproval.replyCodeRequest(lsTransNox, TransactionStatus.STATE_CLOSED, "0")){
            ShowMessageFX.Warning(getStage(), poApproval.getMessage(), pxeModuleName, "Warning!!!");
            return;
        }
        
        ShowMessageFX.Information(getStage(), poApproval.getMessage(), pxeModuleName, "Success!!!");
        
        loadDetail();
    }

    @FXML
    private void btnSMSRequest_Click(ActionEvent event) {
        //upload request
        String lsTransNox = poApproval.uploadCodeRequest((String) poData.get("sTransNox"), "1");
        
        if (lsTransNox.isEmpty()){
            ShowMessageFX.Warning(getStage(), poApproval.getMessage(), pxeModuleName, "Warning!!!");
            return;
        }
        
        ShowMessageFX.Information(getStage(), "SMS Request was submitted successfully.", pxeModuleName, "Success!!!");
        
        loadDetail();
    }

    @FXML
    private void btnCheckRequest_Click(ActionEvent event) {        
        loadDetail();
        
        //get required approval weight to approve the transaction
        int lnReqWeight = poApproval.getWeight2Apprv();
        
        if (lnReqWeight < 0){
            ShowMessageFX.Warning(getStage(), poApproval.getMessage(), pxeModuleName, "Warning!!!");
            return;
        }
        
        //count weight of approved requests
        int lnCount = 0;
        String lsAuthTokn;
        String lasAuthTokn [];
        
        for (Object obj : paData){
            poData = (JSONObject) obj;
        
            if ("1".equals((String) poData.get("cTranStat"))){
                lsAuthTokn = Tokenize.DecryptToken((String) poData.get("sAuthTokn"), (String) poData.get("sEmployID"));
                lasAuthTokn = lsAuthTokn.split(":");
                lnCount += Integer.parseInt(lasAuthTokn[3]);
                System.out.println(lnCount);
            }
        }
        
        if (lnCount < lnReqWeight){
            ShowMessageFX.Information(getStage(), 
                                        "Approvals has insufficient weight to approve your transaction.\n\n" +
                                        "Please ask for additional approval on the list.", 
                                        pxeModuleName, "Additional approval needed!!!");
        } else {
            pbApproved = true;
            getStage().close();
        }
    }

    @FXML
    private void table_click(MouseEvent event) {
        model = table.getSelectionModel().getSelectedItem();
        
        pnSelectd = table.getSelectionModel().getSelectedIndex();

        loadData();
    }
    
    private void loadData(){
        if (paData.isEmpty()) return;
         
        lblRequestType.setText(data.get(pnSelectd).getIndex01());
        lblRequestedTo.setText(data.get(pnSelectd).getIndex02());
        
        int lnCtr = -1;
        for (Object obj : paData){
            lnCtr += 1;
            
            if (lnCtr == pnSelectd){
                poData = (JSONObject) obj;
                
                btnApprove.setDisable(!"0".equals((String) poData.get("cTranStat")));
                
                if ("0".equals((String) poData.get("cTranStat"))){
                    lblRequested.setText("Request From:");
                    
                    if ("1".equals((String) poData.get("cApprType"))){
                        btnSMSRequest.setText("Requested");
                        btnSMSRequest.setDisable(true);
                    } else {
                        btnSMSRequest.setText("SMS Request");
                        btnSMSRequest.setDisable(false);
                    }
                } else if ("1".equals((String) poData.get("cTranStat"))){
                    btnSMSRequest.setDisable(true);
                    lblRequested.setText("Approved By:");
                    
                    if ("1".equals((String) poData.get("cApprType"))){
                        btnSMSRequest.setText("Requested");
                    } else {
                        btnSMSRequest.setText("SMS Request");
                    }
                }
            }
        }
    }
    
    private void clearFields(){      
        lblRequested.setText("Request From:");
        lblRequestedTo.setText("");
        
        btnApprove.setDisable(true);
        btnSMSRequest.setDisable(true);
        btnSMSRequest.setText("SMS Request");        
    }
    
    private void initGrid(){
        TableColumn index01 = new TableColumn("Type");
        TableColumn index02 = new TableColumn("Requested To");
        TableColumn index03 = new TableColumn("Status");
        
        index01.setSortable(false); index01.setResizable(false);
        index02.setSortable(false); index02.setResizable(false);
        index03.setSortable(false); index03.setResizable(false);
        
        table.getColumns().clear();
        
        table.getColumns().add(index01);
        index01.setCellValueFactory(new PropertyValueFactory<TableModel,String>("index01"));
        index01.prefWidthProperty().bind(table.widthProperty().multiply(0.25));
        
        table.getColumns().add(index02);
        index02.setCellValueFactory(new PropertyValueFactory<TableModel,String>("index02"));
        index02.prefWidthProperty().bind(table.widthProperty().multiply(0.50));
        
        table.getColumns().add(index03);
        index03.setCellValueFactory(new PropertyValueFactory<TableModel,String>("index03"));
        index03.prefWidthProperty().bind(table.widthProperty().multiply(0.25));

        /*Set data source to table*/
        data.clear();
        table.setItems(data);
    }
    
    private void loadDetail(){
        lblRequestType.setText("");
        lblRequestedTo.setText("");
        
        data.clear();
        
        JSONObject loJSON;
        JSONArray arr = poApproval.loadCodeRequest();

        if (arr == null){
            MsgBox.showOk(poApproval.getMessage());
        } else {
            for(Object o: arr){
                if ( o instanceof JSONObject ) {
                    loJSON = (JSONObject) o;

                    data.add(new TableModel( 
                                (String) loJSON.get("sRqstType"),
                                getEmployee((String) loJSON.get("sReqstdTo")),
                                getTranStat((String) loJSON.get("cTranStat")),
                                "",
                                "",
                                "",
                                "",
                                "",
                                "",
                                ""));
                }
            }
            //set the data array to public for future use
            paData = arr;

            table.getSelectionModel().selectFirst();
            pnSelectd = table.getSelectionModel().getSelectedIndex();
        }
    }
    
    private String getTranStat(String fsValue){
        switch(fsValue){
            case TransactionStatus.STATE_OPEN:
                return "OPEN";
            case TransactionStatus.STATE_CLOSED:
                return "APPROVED";
            case TransactionStatus.STATE_POSTED:
                return "POSTED";
            case TransactionStatus.STATE_CANCELLED:
                return "DISAPPROVED";
            case TransactionStatus.STATE_VOID:
                return "VOID";
            default:
                return "UNKNOWN";
        }
    }
    
    private String getEmployee(String fsValue){
        String lsSQL = "SELECT sCompnyNm FROM GGC_ISysDBF.Client_Master WHERE sClientID = " + SQLUtil.toSQL(fsValue);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        
        try {
            if (loRS.next())
                return loRS.getString("sCompnyNm");
        } catch (SQLException ex) {
            ex.printStackTrace();
            
            //retreive using API
            JSONObject loJSON = TokenRestAPI.downloadEmployeeInfo(poGRider, fsValue);
            if ("success".equals((String) loJSON.get("result"))) return (String) loJSON.get("sClientNm");   
        }
                
        return fsValue;
    }
    
    private void unloadScene(ActionEvent event){
        Node source = (Node)  event.getSource(); 
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }
    
    private Stage getStage(){
        return (Stage) btnApprove.getScene().getWindow();
    }
    
    public void setGRider(GRider foGRider){
        poGRider = foGRider;
    }
    
    public void setTableName(String fsValue){
        psTableNme = fsValue;
    }
    
    public void setTransNmbr(String fsValue){
        psTransNox = fsValue;
    }
    
    private void setMessage(String fsValue){
        psMessage = fsValue;
    }
    
    public String getMessage(){
        return psMessage;
    }
    
    public boolean IsApproved(){
        return pbApproved;
    }
    
    private GRider poGRider;
    private String psTableNme;
    private String psTransNox;
    private ITokenize poApproval;
    
    private JSONObject poData;
    private JSONArray paData;
    
    private boolean pbApproved;
    private String psMessage;
    private int pnSelectd = -1;
    
    private TableModel model;
    private ObservableList<TableModel> data = FXCollections.observableArrayList();
    
    private final String pxeModuleName = "Transaction Approval";
}
