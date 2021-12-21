// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;

/**
 * An exception that is the payload of an {@code ErrorMessage} when a send fails.
 */
public class AzureSendFailureException extends MessagingException {

    /**
     * Construct the {@link AzureSendFailureException} with the specified detail message and nested exception.
     * @param message the specified detail message.
     * @param cause the nested exception.
     */
    public AzureSendFailureException(Message<?> message, Throwable cause) {
        super(message, cause);
    }
}
