package org.rmj.appdriver.agentfx.ui;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.stage.Stage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agentfx.CommonUtils;
import org.rmj.appdriver.agentfx.ShowMessageFX;
import org.rmj.appdriver.agentfx.WebClient;
import org.rmj.appdriver.constants.UserLockState;
import org.rmj.appdriver.constants.UserState;
import org.rmj.appdriver.constants.UserType;
import org.rmj.appdriver.agentfx.token.TokenApproval;

public class showFXDialog {
    private final static String pxeModuleName = "org.rmj.appdriver.agentfx.showFXDialog";   
    
    public static JSONObject updateGCardNmbr(GRider foGRider, String fsApplicNo){
        String lsSQL = "SELECT a.sGCardNox, a.sCardNmbr" + 
                        " FROM G_Card_Master a" + 
                            ", G_Card_Application b" +
                        " WHERE a.cCardStat IN ('0', '1')" +
                            " AND a.cDigitalx = '0'" +
                            " AND a.sApplicNo = b.sTransNox" +
                            " AND b.dTransact >= " + SQLUtil.toSQL(CommonUtils.getConfiguration(foGRider, "ChngeCrdNo")) +
                            " AND a.sGCardNox LIKE " + SQLUtil.toSQL(foGRider.getBranchCode() + "%") +
                            " AND b.sTransNox = " + SQLUtil.toSQL(fsApplicNo);
        
        ResultSet loRS = foGRider.executeQuery(lsSQL);
        
        try {
            if (loRS.next()){
                UpdateGCardNo instance = new UpdateGCardNo();
                instance.setCardNmbr(loRS.getString("sCardNmbr"));
                CommonUtils.showModal(instance);
            
                if (instance.isCancelled()){
                    JSONObject err_detl = new JSONObject();
                    err_detl.put("code", "100");
                    err_detl.put("message", "Entry was cancelled.");

                    JSONObject err_mstr = new JSONObject();
                    err_mstr.put("result", "error");
                    err_mstr.put("error", err_detl);
                    return err_mstr;
                }
                
                if (instance.getCardNmbr().isEmpty()){
                    JSONObject err_detl = new JSONObject();
                    err_detl.put("code", "105");
                    err_detl.put("message", "Card number must not be empty.");

                    JSONObject err_mstr = new JSONObject();
                    err_mstr.put("result", "error");
                    err_mstr.put("error", err_detl);
                    return err_mstr;
                }
                
                if (!loRS.getString("sCardNmbr").equals(instance.getCardNmbr())){
                    lsSQL = "UPDATE G_Card_Master SET" +
                                "  sCardNmbr = " + SQLUtil.toSQL(instance.getCardNmbr()) +
                                ", cDigitalx = '2'" + 
                                ", cCardStat = '1'" +
                            " WHERE sGCardNox = " + SQLUtil.toSQL(loRS.getString("sGCardNox"));
                    
                    foGRider.beginTrans();
                    
                    if (foGRider.executeQuery(lsSQL, "G_Card_Master", foGRider.getBranchCode(), "") <= 0){
                        JSONObject err_detl = new JSONObject();
                        err_detl.put("code", "104");
                        err_detl.put("message", foGRider.getErrMsg());

                        JSONObject err_mstr = new JSONObject();
                        err_mstr.put("result", "error");
                        err_mstr.put("error", err_detl);
                        
                        foGRider.rollbackTrans();
                        return err_mstr;
                    }
                    
                    lsSQL = "UPDATE G_Card_Application SET" +
                                " cDigitalx = '2'" +                                
                            " WHERE sTransNox = " + SQLUtil.toSQL(fsApplicNo);
                    
                    if (foGRider.executeQuery(lsSQL, "G_Card_Application", foGRider.getBranchCode(), "") <= 0){
                        JSONObject err_detl = new JSONObject();
                        err_detl.put("code", "106");
                        err_detl.put("message", foGRider.getErrMsg());

                        JSONObject err_mstr = new JSONObject();
                        err_mstr.put("result", "error");
                        err_mstr.put("error", err_detl);
                        
                        foGRider.rollbackTrans();
                        return err_mstr;
                    }
                    
                    foGRider.commitTrans();
                    
                }
                
                JSONObject loJSON = new JSONObject();
                loJSON.put("result", "success");
                loJSON.put("message", "G-Card Number was updated successfully.");
                
                return loJSON;
                
            } else {
                JSONObject err_detl = new JSONObject();
                err_detl.put("code", "101");
                err_detl.put("message", "No OPEN/PRINTED SMARTCARD application on record for the given application.");
                
                JSONObject err_mstr = new JSONObject();
                err_mstr.put("result", "error");
                err_mstr.put("error", err_detl);
                return err_mstr;
            }
        } catch (SQLException ex) {
            JSONObject err_detl = new JSONObject();
            err_detl.put("code", "102");
            err_detl.put("message", ex.getMessage());

            JSONObject err_mstr = new JSONObject();
            err_mstr.put("result", "error");
            err_mstr.put("error", err_detl);
            return err_mstr;
        } catch (Exception ex) {
            JSONObject err_detl = new JSONObject();
            err_detl.put("code", "103");
            err_detl.put("message", ex.getMessage());

            JSONObject err_mstr = new JSONObject();
            err_mstr.put("result", "error");
            err_mstr.put("error", err_detl);
            return err_mstr;
        }
    }
    
    public static boolean validateOTPx(GRider foGRider, JSONObject foValue){
        try {
            SendOTPx instance = new SendOTPx();
            instance.setGRider(foGRider);
            instance.setTransaction(foValue);
            CommonUtils.showModal(instance);
            
            return !instance.isCancelled();
        } catch (Exception ex) {
            Logger.getLogger(showFXDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    public static boolean validateOTP(GRider foGRider, JSONObject foValue){
        try {        
            SendOTP instance = new SendOTP();
            instance.setGRider(foGRider);
            instance.setTransaction(foValue);
            CommonUtils.showModal(instance);
            
            return !instance.isCancelled();
        } catch (Exception ex){     
            Logger.getLogger(showFXDialog.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    public static JSONObject updateMobileNo(GRider foGRider, String fsClientID, String fsMobileNo){
        JSONObject loJSON = new JSONObject();
        
        try {
            System.err.println("ClientID: " + fsClientID);
            System.err.println("MobileNo: " + fsMobileNo);
            
            UpdateMobile instance = new UpdateMobile();
            instance.setMobileNo(fsMobileNo);
            CommonUtils.showModal(instance);
            
            if (instance.isCancelled()){
                JSONObject err_detl = new JSONObject();
                err_detl.put("code", "100");
                err_detl.put("message", "Transaction was cancelled.");
                
                JSONObject err_mstr = new JSONObject();
                err_mstr.put("result", "error");
                err_mstr.put("error", err_detl);
                return err_mstr;
            }
            
            String lsMobileNo = instance.getMobileNo();
            
            if (lsMobileNo.isEmpty()) {
                JSONObject err_detl = new JSONObject();
                err_detl.put("code", "250");
                err_detl.put("message", "Mobile number is not set.");
                
                JSONObject err_mstr = new JSONObject();
                err_mstr.put("result", "error");
                err_mstr.put("error", err_detl);
                return err_mstr;
            }
            
            if (fsClientID.isEmpty()){  
                JSONObject err_detl = new JSONObject();
                err_detl.put("code", "250");
                err_detl.put("message", "Client ID is not set.");
                
                JSONObject err_mstr = new JSONObject();
                err_mstr.put("result", "error");
                err_mstr.put("error", err_detl);
                return err_mstr;
            }
            
            Calendar calendar = Calendar.getInstance();
            //Create the header section needed by the API
            Map<String, String> headers =
                    new HashMap<String, String>();
            headers.put("Accept", "application/json");
            headers.put("Content-Type", "application/json");
            headers.put("g-api-id", foGRider.getProductID());
            headers.put("g-api-imei", MiscUtil.getPCName());
            headers.put("g-api-key", SQLUtil.dateFormat(calendar.getTime(), "yyyyMMddHHmmss"));
            headers.put("g-api-hash", org.apache.commons.codec.digest.DigestUtils.md5Hex((String)headers.get("g-api-imei") + (String)headers.get("g-api-key")));
            headers.put("g-api-client", foGRider.getClientID());
            headers.put("g-api-user", foGRider.getUserID());
            headers.put("g-api-log", "");
            headers.put("g-api-token", "");
            
            
            JSONObject param = new JSONObject();
            param.put("id", fsClientID);
            param.put("mobile", lsMobileNo);
            
            String sURL = CommonUtils.getConfiguration(foGRider, "WebSvr") + "gcard/ms/update_client_mobile.php";
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
            
            json_obj.put("mobile", lsMobileNo);
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
            err_detl.put("code", ex.getErrorType());
            
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
    
    /**
     * System Approval
     * 
     * @param foGRider Application Driver
     * @return JSONObject of keys {sUserIDxx, nUserLevl}
     */
    public static JSONObject getApproval(GRider foGRider){
        JSONObject loJSON = new JSONObject();
        String [] arr = new String [2];
        
        try {
            
            SysApprovalFX instance = new SysApprovalFX();
            CommonUtils.showModal(instance);

            arr[0] = instance.getUsername();
            arr[1] = instance.getPassword();

            if (arr[0].isEmpty() || arr[1].isEmpty()) return null;

            arr[0] = foGRider.Encrypt(arr[0]);
            arr[1] = foGRider.Encrypt(arr[1]);

            String lsSQL = "SELECT * FROM xxxSysUser" + 
                            " WHERE sLogNamex = " + SQLUtil.toSQL(arr[0]) +
                                " AND sPassword = " + SQLUtil.toSQL(arr[1]) +
                                " AND (cUserType = '1' OR sProdctID = " + 
                                        SQLUtil.toSQL(foGRider.getProductID()) + ")";

            ResultSet loRS = foGRider.executeQuery(lsSQL);

            String lsUserIDxx;
            String lsEmployID;
            int lnRights;
            
            if (!loRS.next()){
                ShowMessageFX.Information("Verify your log name and/or password", pxeModuleName, "Login Error");
                return null;
            }
            
            lsUserIDxx = loRS.getString("sUserIDxx");
            lsEmployID = loRS.getString("sEmployNo");
            lnRights = loRS.getInt("nUserLevl");
            
            if (loRS.getString("cUserStat").equals(UserState.SUSPENDED)){
                ShowMessageFX.Information("User has no Rights for Procedure Approval!!!", pxeModuleName, "User is currently Suspended!!!");
                return null;
            }
            
            if (loRS.getString("cLockStat").equals(UserLockState.LOCKED)){
                ShowMessageFX.Information("User has no Rights for Procedure Approval!!!", pxeModuleName, "User Rights is Currently Locked!!!");
                return null;
            }
            
            if (loRS.getString("cUserType").equals(UserType.LOCAL)){
                if (!loRS.getString("sProdctID").equalsIgnoreCase(foGRider.getProductID())){
                    ShowMessageFX.Information("User has no Rights for Procedure Approval!!!", pxeModuleName, "User has no Rights for Procedure Approval!!!");
                    return null;
                }
            }
            
            loJSON.put("sUserIDxx", (String) lsUserIDxx);
            loJSON.put("sEmployID", (String) lsEmployID);
            loJSON.put("nUserLevl", (int) lnRights);
            return loJSON;
        } catch (Exception ex) {
            ex.printStackTrace();
            ShowMessageFX.Information(ex.getMessage(), pxeModuleName, "Exception detected???");
        }
        
        return null;
    }
    
    /**
     * System Approval
     * 
     * @param foGRider Application Driver
     * @return JSONObject of keys {sUserIDxx, nUserLevl}
     */
    public static JSONObject getApproval(GRider foGRider, Stage foStage){
        JSONObject loJSON = new JSONObject();
        String [] arr = new String [2];
        
        try {
            
            SysApprovalFX instance = new SysApprovalFX();
            CommonUtils.showModal(instance);

            arr[0] = instance.getUsername();
            arr[1] = instance.getPassword();

            if (arr[0].isEmpty() || arr[1].isEmpty()) return null;

            arr[0] = foGRider.Encrypt(arr[0]);
            arr[1] = foGRider.Encrypt(arr[1]);

            String lsSQL = "SELECT * FROM xxxSysUser" + 
                            " WHERE sLogNamex = " + SQLUtil.toSQL(arr[0]) +
                                " AND sPassword = " + SQLUtil.toSQL(arr[1]) +
                                " AND (cUserType = '1' OR sProdctID = " + 
                                        SQLUtil.toSQL(foGRider.getProductID()) + ")";

            ResultSet loRS = foGRider.executeQuery(lsSQL);

            String lsUserIDxx;
            String lsEmployID;
            int lnRights;
            
            if (!loRS.next()){
                ShowMessageFX.Information("Verify your log name and/or password", pxeModuleName, "Login Error");
                return null;
            }
            
            lsUserIDxx = loRS.getString("sUserIDxx");
            lsEmployID = loRS.getString("sEmployNo");
            lnRights = loRS.getInt("nUserLevl");
            
            if (loRS.getString("cUserStat").equals(UserState.SUSPENDED)){
                ShowMessageFX.Information(foStage, "User has no Rights for Procedure Approval!!!", pxeModuleName, "User is currently Suspended!!!");
                return null;
            }
            
            if (loRS.getString("cLockStat").equals(UserLockState.LOCKED)){
                ShowMessageFX.Information(foStage, "User has no Rights for Procedure Approval!!!", pxeModuleName, "User Rights is Currently Locked!!!");
                return null;
            }
            
            if (loRS.getString("cUserType").equals(UserType.LOCAL)){
                if (!loRS.getString("sProdctID").equalsIgnoreCase(foGRider.getProductID())){
                    ShowMessageFX.Information(foStage, "User has no Rights for Procedure Approval!!!", pxeModuleName, "User has no Rights for Procedure Approval!!!");
                    return null;
                }
            }
            
            loJSON.put("sUserIDxx", (String) lsUserIDxx);
            loJSON.put("sEmployID", (String) lsEmployID);
            loJSON.put("nUserLevl", (int) lnRights);
            return loJSON;
        } catch (Exception ex) {
            ex.printStackTrace();
            ShowMessageFX.Information(foStage, ex.getMessage(), pxeModuleName, "Exception detected???");
        }
        
        return null;
    }
    
    public static JSONObject jsonSearch(GRider foGRider,
                                        String fsSQL,
                                        String fsValue,
                                        String fsHeader,
                                        String fsColName,
                                        String fsColCrit,
                                        int fnSort){
        
        String [] laSplit = fsColCrit.split("»");
        String lsCondition;
                
        lsCondition = laSplit[fnSort] + " LIKE " + SQLUtil.toSQL(fsValue + "%");
        fsSQL = MiscUtil.addCondition(fsSQL, lsCondition);
        
        try {
            ResultSet loRS = foGRider.executeQuery(fsSQL);
                
            if (MiscUtil.RecordCount(loRS) == 1) return CommonUtils.loadJSON(loRS);

            if (MiscUtil.RecordCount(loRS) > 1){
                loRS.first();

                QuickSearch loSearch = new QuickSearch();
                loSearch.setGRider(foGRider);
                loSearch.setResultSet(loRS);
                loSearch.setSQLSource(fsSQL);
                loSearch.setConditionValue(fsValue);
                loSearch.setColumnHeader(fsHeader);
                loSearch.setColunmName(fsColName);
                loSearch.setColumnCriteria(fsColCrit);
                loSearch.setColumnIndex(fnSort);

                CommonUtils.showModal(loSearch);
                return loSearch.getJSON();   
            }
        } catch (SQLException ex) {
            Logger.getLogger(showFXDialog.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(showFXDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
            
        return null;
    }
       
    public static JSONObject jsonBrowse(GRider foGRider,
                                        ResultSet loRS,
                                        String fsHeader,
                                        String fsColName){
       
        long lnRow = MiscUtil.RecordCount(loRS);
        
        if (lnRow == 1)
            return CommonUtils.loadJSON(loRS);
        else{
            if (MiscUtil.RecordCount(loRS) >= 1){
                try {
                    loRS.first();
                    
                    QuickSearch loSearch = new QuickSearch();
                    loSearch.setGRider(foGRider);
                    loSearch.setResultSet(loRS);
                    loSearch.setSQLSource("");
                    loSearch.setConditionValue("");
                    loSearch.setColumnHeader(fsHeader);
                    loSearch.setColunmName(fsColName);
                    loSearch.setColumnCriteria("");
                    loSearch.setColumnIndex(-1);

                    CommonUtils.showModal(loSearch);
                    return loSearch.getJSON();
                } catch (Exception ex) {
                    Logger.getLogger(showFXDialog.class.getName()).log(Level.SEVERE, null, ex);
                }
            }         
        }
        
        return null;
    }
    
    public static JSONObject jsonBrowse(JSONArray foJSON,
                                        String fsHeader,
                                        String fsColName){    
                                        
        try {
            JSONBrowse instance = new JSONBrowse();
        
            instance.setData(foJSON);
            instance.setColumnHeader(fsHeader);
            instance.setColunmName(fsColName);
            
            CommonUtils.showModal(instance);
            return instance.getJSON();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    public static boolean getTokenApproval(GRider foGRider, String fsTableNme, String fsTransNox){
        try {
            TokenApproval instance = new TokenApproval();
            instance.setGRider(foGRider);
            instance.setTableName(fsTableNme);
            instance.setTransNmbr(fsTransNox);
                    
            CommonUtils.showModal(instance);
            return instance.isApproved();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        return false;
    }
    
    public static boolean resetPOS(GRider foGRider){
        try {
            ResetPOS instance = new ResetPOS();
            instance.setGRider(foGRider);
            CommonUtils.showModal(instance);
            
            if (instance.isCancelled()){
                ShowMessageFX.Warning(null, "Warning", instance.getMessage());
                return false;
            } else
                return true;
        } catch (Exception ex) {
            Logger.getLogger(showFXDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
}
