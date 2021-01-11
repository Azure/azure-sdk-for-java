// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.core;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;

/**
 * An exception that is the payload of an {@code ErrorMessage} when a send fails.
 *
 * @author Jacob Severson
 * @since 1.1
 */
public class AzureSendFailureException extends MessagingException {

    public AzureSendFailureException(Message<?> message, Throwable cause) {
        super(message, cause);
    }
}
