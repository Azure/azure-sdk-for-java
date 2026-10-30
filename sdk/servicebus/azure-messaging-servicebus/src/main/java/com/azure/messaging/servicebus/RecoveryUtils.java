// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.implementation.RecoveryKind;
import reactor.core.Exceptions;

import java.util.concurrent.TimeoutException;

/**
 * Package-private utilities shared by the sender and receiver call sites that compose
 * tiered recovery on top of {@link com.azure.core.amqp.implementation.RetryUtil#withRetry}.
 */
final class RecoveryUtils {

    private RecoveryUtils() {
    }

    static Throwable unwrapRetryExhaustion(Throwable error) {
        while (Exceptions.isRetryExhausted(error) && error.getCause() != null) {
            error = error.getCause();
        }
        return error;
    }

    /**
     * Ensures the error is retriable by the standard
     * {@link com.azure.core.amqp.implementation.RetryUtil#createRetry} filter.
     * That filter only accepts {@link TimeoutException} or transient {@link AmqpException}.
     * Non-AMQP errors like {@code IllegalStateException("Cannot publish when disposed")}
     * that {@link RecoveryKind} classifies as LINK would otherwise be rejected, bypassing
     * the recovery just performed. This wraps such errors as transient {@link AmqpException}
     * so the same recovery + retry composition works for both sender and receiver paths.
     *
     * @param error the original error emitted from a send/receive operation.
     * @return the same error if it already passes the retry filter, otherwise a transient
     *     {@link AmqpException} wrapping it for LINK/CONNECTION errors or a same-link resource-limit retry.
     */
    static Throwable asRetriable(Throwable error) {
        if (error instanceof TimeoutException
            || (error instanceof AmqpException && ((AmqpException) error).isTransient())) {
            return error;
        }
        if (error instanceof AmqpException) {
            final AmqpException amqpError = (AmqpException) error;
            if (amqpError.getErrorCondition() == AmqpErrorCondition.RESOURCE_LIMIT_EXCEEDED) {
                return new AmqpException(true, amqpError.getErrorCondition(), amqpError, amqpError.getContext());
            }
        }
        final RecoveryKind kind = RecoveryKind.classify(error);
        if (kind == RecoveryKind.LINK || kind == RecoveryKind.CONNECTION) {
            return new AmqpException(true, error.getMessage(), error, null);
        }
        return error;
    }
}
