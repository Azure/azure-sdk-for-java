// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.exception;

import com.azure.core.exception.AzureException;
import com.azure.messaging.webpubsub.client.models.AckMessageError;

public class SendMessageFailedException extends AzureException {

    private final boolean isTransient;

    private final Long ackId;
    private final AckMessageError error;

    public SendMessageFailedException(String message, Throwable cause,
                                      boolean isTransient, Long ackId, AckMessageError error) {
        super(message, cause);
        this.isTransient = isTransient;
        this.ackId = ackId;
        this.error = error;
    }

    public boolean isTransient() {
        return isTransient;
    }

    public Long getAckId() {
        return ackId;
    }

    public AckMessageError getError() {
        return error;
    }
}
