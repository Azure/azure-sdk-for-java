package com.azure.messaging.webpubsub.client.models;

import com.azure.core.util.BinaryData;

public class SendToGroupMessage {

    private String type = "sendToGroup";

    private String group;

    private int ackId;

    private boolean noEcho = false;

    private String dataType;

    private BinaryData data;

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public int getAckId() {
        return ackId;
    }

    public void setAckId(int ackId) {
        this.ackId = ackId;
    }

    public boolean isNoEcho() {
        return noEcho;
    }

    public void setNoEcho(boolean noEcho) {
        this.noEcho = noEcho;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public BinaryData getData() {
        return data;
    }

    public void setData(BinaryData data) {
        this.data = data;
    }
}
