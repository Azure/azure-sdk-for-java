package com.microsoft.windowsazure.services.queue.models;

public class ListMessagesOptions extends QueueServiceOptions {
    private Integer numberOfMessages;
    private Integer visibilityTimeoutInSeconds;

    public Integer getNumberOfMessages() {
        return numberOfMessages;
    }

    public ListMessagesOptions setNumberOfMessages(Integer numberOfMessages) {
        this.numberOfMessages = numberOfMessages;
        return this;
    }

    public Integer getVisibilityTimeoutInSeconds() {
        return visibilityTimeoutInSeconds;
    }

    public ListMessagesOptions setVisibilityTimeoutInSeconds(Integer visibilityTimeoutInSeconds) {
        this.visibilityTimeoutInSeconds = visibilityTimeoutInSeconds;
        return this;
    }
}
