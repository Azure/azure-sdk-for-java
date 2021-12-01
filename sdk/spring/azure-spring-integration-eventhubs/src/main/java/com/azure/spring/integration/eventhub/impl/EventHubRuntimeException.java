// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.eventhub.impl;

import org.springframework.core.NestedRuntimeException;

/**
 * The Azure Event Hub specific {@link NestedRuntimeException}.
 *
 * @author Warren Zhu
 */
public class EventHubRuntimeException extends NestedRuntimeException {

    /**
     *
     * @param msg The message.
     */
    public EventHubRuntimeException(String msg) {
        super(msg);
    }

    /**
     *
     * @param msg The message.
     * @param cause The cause of the throwable.
     */
    public EventHubRuntimeException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
