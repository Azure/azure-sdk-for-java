// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.ErrorCondition;

import java.time.Duration;

class AmqpExceptionUtil {
    /**
     * Base sleep wait time.
     */
    private static final int SERVER_BUSY_BASE_SLEEP_TIME_IN_SECS = 4;

    /**
     * Check if the existing exception is a retriable exception.
     *
     * @param exception An exception that was observed for the operation to be retried.
     * @return true if the exception is a retriable exception, otherwise false.
     */
    static boolean isRetriableException(Exception exception) {
        return (exception instanceof AmqpException) && ((AmqpException) exception).isTransient();
    }

    static Duration getBaseWait(AmqpException lastException, Duration baseWaitTime) {
        if (lastException.getErrorCondition() == ErrorCondition.SERVER_BUSY_ERROR) {
            return baseWaitTime.plus(Duration.ofSeconds(SERVER_BUSY_BASE_SLEEP_TIME_IN_SECS));
        } else {
            return baseWaitTime;
        }
    }
}
