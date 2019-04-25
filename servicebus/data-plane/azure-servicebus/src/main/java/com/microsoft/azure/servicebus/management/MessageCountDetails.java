package com.microsoft.azure.servicebus.management;

public class MessageCountDetails {
    private long activeMessageCount;
    private long deadLetterMessageCount;
    private long scheduledMessageCount;
    private long transferMessageCount;
    private long transferDeadLetterMessageCount;

    public long getActiveMessageCount() {
        return activeMessageCount;
    }

    public void setActiveMessageCount(long activeMessageCount) {
        this.activeMessageCount = activeMessageCount;
    }

    public long getDeadLetterMessageCount() {
        return deadLetterMessageCount;
    }

    public void setDeadLetterMessageCount(long deadLetterMessageCount) {
        this.deadLetterMessageCount = deadLetterMessageCount;
    }

    public long getScheduledMessageCount() {
        return scheduledMessageCount;
    }

    public void setScheduledMessageCount(long scheduledMessageCount) {
        this.scheduledMessageCount = scheduledMessageCount;
    }

    public long getTransferMessageCount() {
        return transferMessageCount;
    }

    public void setTransferMessageCount(long transferMessageCount) {
        this.transferMessageCount = transferMessageCount;
    }

    public long getTransferDeadLetterMessageCount() {
        return transferDeadLetterMessageCount;
    }

    public void setTransferDeadLetterMessageCount(long transferDeadLetterMessageCount) {
        this.transferDeadLetterMessageCount = transferDeadLetterMessageCount;
    }
}
