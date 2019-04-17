// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.eventhubs.impl;

import org.apache.qpid.proton.message.Message;

import java.util.Objects;
import java.util.function.Consumer;

class MessageOperationResult implements OperationResult<Message, Exception> {
    private final Consumer<Message> onComplete;
    private final Consumer<Exception> onError;

    MessageOperationResult(Consumer<Message> onComplete, Consumer<Exception> onError) {
        Objects.requireNonNull(onComplete);
        Objects.requireNonNull(onError);

        this.onComplete = onComplete;
        this.onError = onError;
    }

    @Override
    public void onComplete(final Message response) {
        final int statusCode = (int) response.getApplicationProperties().getValue()
            .get(ClientConstants.PUT_TOKEN_STATUS_CODE);
        final String statusDescription = (String) response.getApplicationProperties().getValue()
            .get(ClientConstants.PUT_TOKEN_STATUS_DESCRIPTION);

        if (statusCode == AmqpResponseCode.ACCEPTED.getValue() || statusCode == AmqpResponseCode.OK.getValue()) {
            onComplete.accept(response);
        } else {
            this.onError(ExceptionUtil.amqpResponseCodeToException(statusCode, statusDescription));
        }
    }

    @Override
    public void onError(final Exception error) {
        onError.accept(error);
    }
}

