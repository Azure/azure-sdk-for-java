package com.microsoft.windowsazure.services.queue.models;

public class PeekMessagesOptions extends QueueServiceOptions {
    private Integer numberOfMessages;

    @Override
    public PeekMessagesOptions setTimeout(Integer timeout) {
        super.setTimeout(timeout);
        return this;
    }

    public Integer getNumberOfMessages() {
        return numberOfMessages;
    }

    public PeekMessagesOptions setNumberOfMessages(Integer numberOfMessages) {
        this.numberOfMessages = numberOfMessages;
        return this;
    }
}
