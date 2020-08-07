/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.messaging.listener;

import org.springframework.core.NestedRuntimeException;

/**
 * Exception to be thrown when the execution of a listener method failed.
 *
 * @author Warren Zhu
 */
@SuppressWarnings("serial")
class ListenerExecutionFailedException extends NestedRuntimeException {

    /**
     * Constructor for ListenerExecutionFailedException.
     *
     * @param msg   the detail message
     * @param cause the exception thrown by the listener method
     */
    ListenerExecutionFailedException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
