// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.implementation.RecoveryKind;
import com.azure.core.amqp.implementation.RetryUtil;

import java.util.concurrent.TimeoutException;

/**
 * Package-private utilities shared by the sender and receiver call sites that compose
 * tiered recovery on top of {@link RetryUtil#withRetry}.
 */
final class RecoveryUtils {

    private RecoveryUtils() {
    }

    /**
     * Ensures the error is retriable by the standard {@link RetryUtil#createRetry} filter.
     * That filter only accepts {@link TimeoutException} or transient {@link AmqpException}.
     * Non-AMQP errors like {@code IllegalStateException("Cannot publish when disposed")}
     * that {@link RecoveryKind} classifies as LINK would otherwise be rejected, bypassing
     * the recovery just performed. This wraps such errors as transient {@link AmqpException}
     * so the same recovery + retry composition works for both sender and receiver paths.
     *
     * @param error the original error emitted from a send/receive operation.
     * @return the same error if it already passes the retry filter, otherwise a transient
     *     {@link AmqpException} wrapping it (only for LINK/CONNECTION-classified errors).
     */
    static Throwable asRetriable(Throwable error) {
        if (error instanceof TimeoutException
            || (error instanceof AmqpException && ((AmqpException) error).isTransient())) {
            return error;
        }
        final RecoveryKind kind = RecoveryKind.classify(error);
        if (kind == RecoveryKind.LINK || kind == RecoveryKind.CONNECTION) {
            return new AmqpException(true, error.getMessage(), error, null);
        }
        return error;
    }
}
