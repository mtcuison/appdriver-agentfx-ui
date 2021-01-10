package org.rmj.appdriver.agentfx.callback;

public interface ISMSCallback {
    public void NotifyFailed(String fsGateway);
    public void NotifySuccess(String fsGateway);
    public void NotifySMSReceived(String fsGateway);
    public void NotifyCallReceived(String fsGateway);
}
