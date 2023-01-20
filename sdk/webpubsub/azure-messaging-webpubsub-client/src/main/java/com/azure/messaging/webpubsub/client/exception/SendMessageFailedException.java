// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.exception;

import com.azure.core.exception.AzureException;
import com.azure.messaging.webpubsub.client.implementation.AckMessage;

public class SendMessageFailedException extends AzureException {

    private final Long ackId;
    private final AckMessage error;

    public SendMessageFailedException(String message, Throwable cause, Long ackId, AckMessage error) {
        super(message, cause);
        this.ackId = ackId;
        this.error = error;
    }

    public Long getAckId() {
        return ackId;
    }

    public AckMessage getError() {
        return error;
    }
}
