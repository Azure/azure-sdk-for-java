package com.azure.storage.queue.models;

import java.time.OffsetDateTime;

public final class UpdatedMessage {
    /*
     * The pop receipt of the queue message.
     */
    private final String popReceipt;

    /*
     * A UTC date/time value that represents when the message will be visible
     * on the queue.
     */
    private final OffsetDateTime timeNextVisible;

    public UpdatedMessage(String popReceipt, OffsetDateTime timeNextVisible) {
        this.popReceipt = popReceipt;
        this.timeNextVisible = timeNextVisible;
    }

    public String popReceipt() {
        return popReceipt;
    }

    public OffsetDateTime timeNextVisible() {
        return timeNextVisible;
    }
}
