package com.azure.messaging.webpubsub.client.models;

public class JoinGroupMessage extends WebPubSubMessage {

    private String type = "joinGroup";
    private String group;
    private int ackId;

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
}
