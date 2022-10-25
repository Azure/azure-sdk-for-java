package com.azure.messaging.webpubsub.client.message;

public class LeaveGroupMessage extends WebPubSubMessage {

    private String type = "joinGroup";
    private String group;
    private Long ackId;

    public String getGroup() {
        return group;
    }

    public LeaveGroupMessage setGroup(String group) {
        this.group = group;
        return this;
    }

    public Long getAckId() {
        return ackId;
    }

    public LeaveGroupMessage setAckId(Long ackId) {
        this.ackId = ackId;
        return this;
    }
}
