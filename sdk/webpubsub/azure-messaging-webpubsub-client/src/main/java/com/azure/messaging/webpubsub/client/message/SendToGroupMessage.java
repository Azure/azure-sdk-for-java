package com.azure.messaging.webpubsub.client.message;

import com.azure.core.util.BinaryData;

public class SendToGroupMessage extends WebPubSubMessage {

    private String type = "sendToGroup";

    private String group;

    private Long ackId;

    private boolean noEcho = false;

    private String dataType;

    private BinaryData data;

    public String getGroup() {
        return group;
    }

    public SendToGroupMessage setGroup(String group) {
        this.group = group;
        return this;
    }

    public Long getAckId() {
        return ackId;
    }

    public SendToGroupMessage setAckId(Long ackId) {
        this.ackId = ackId;
        return this;
    }

    public boolean isNoEcho() {
        return noEcho;
    }

    public SendToGroupMessage setNoEcho(boolean noEcho) {
        this.noEcho = noEcho;
        return this;
    }

    public String getDataType() {
        return dataType;
    }

    public SendToGroupMessage setDataType(String dataType) {
        this.dataType = dataType;
        return this;
    }

    public BinaryData getData() {
        return data;
    }

    public SendToGroupMessage setData(BinaryData data) {
        this.data = data;
        return this;
    }
}
