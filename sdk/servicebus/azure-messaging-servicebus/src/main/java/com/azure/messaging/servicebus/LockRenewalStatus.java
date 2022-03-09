// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import java.time.Duration;
import java.util.function.Consumer;

/**
 * Represents the current lock renewal status.
 *
 * @see ServiceBusReceiverAsyncClient#renewMessageLock(ServiceBusReceivedMessage, Duration)
 * @see ServiceBusReceiverClient#renewSessionLock(Duration, Consumer)
 */
enum LockRenewalStatus {
    /**
     * Lock renewal operation is still running.
     */
    RUNNING,
    /**
     * Lock renewal operation is complete.
     */
    COMPLETE,
    /**
     * An exception occurred while lock was being renewed.
     */
    FAILED,
    /**
     * The lock renewal operation was cancelled.
     */
    CANCELLED,
}
