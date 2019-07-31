// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue.models;

import java.time.OffsetDateTime;

/**
 * Response from the service when updating a message. Contains the information needed to continue working with
 * the specific message.
 */
public final class UpdatedMessage {
    private final String popReceipt;
    private final OffsetDateTime timeNextVisible;

    /**
     * Creates an instance of updated message information.
     *
     * @param popReceipt Unique identifier that verifies the operation on a message is valid
     * @param timeNextVisible The next time the message will be visible to other operations in the queue
     */
    public UpdatedMessage(String popReceipt, OffsetDateTime timeNextVisible) {
        this.popReceipt = popReceipt;
        this.timeNextVisible = timeNextVisible;
    }

    /**
     * @return the unique identifier used to verify that the operation is allowed on the message
     */
    public String popReceipt() {
        return popReceipt;
    }

    /**
     * @return the next time the message will be visible to other operations in the queue
     */
    public OffsetDateTime timeNextVisible() {
        return timeNextVisible;
    }
}
