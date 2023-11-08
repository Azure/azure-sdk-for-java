// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.implementation.listener;

import org.springframework.core.NestedRuntimeException;

/**
 * Exception to be thrown when the execution of a listener method failed.
 *
 */
@SuppressWarnings("serial")
public class ListenerExecutionFailedException extends NestedRuntimeException {

    /**
     * Constructor for ListenerExecutionFailedException.
     *
     * @param msg the detail message
     * @param cause the exception thrown by the listener method
     */
    public ListenerExecutionFailedException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
