// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpException;

import java.util.concurrent.TimeoutException;

/**
 * Classifies errors into recovery tiers, determining what resources should be closed
 * between retry attempts. This follows the tiered recovery pattern used by the Go, .NET,
 * Python, and JS Azure SDKs.
 *
 * <ul>
 *   <li>{@link #NONE} — Retry on the same link (server-busy, timeouts).</li>
 *   <li>{@link #LINK} — Close the send/receive link; next retry creates a fresh link on the same connection.</li>
 *   <li>{@link #CONNECTION} — Close the entire connection; next retry creates a fresh connection and link.</li>
 *   <li>{@link #FATAL} — Do not retry (unauthorized, not-found, message too large).</li>
 * </ul>
 */
public enum RecoveryKind {
    /**
     * No recovery needed — retry on the same link and connection.
     * Applies to: server-busy, timeouts, operation-cancelled.
     */
    NONE,

    /**
     * Close the link (and its session) before retrying. The next retry creates a fresh link
     * on the same connection.
     * Applies to: link:detach-forced, link:stolen, transient AMQP errors on the link.
     */
    LINK,

    /**
     * Close the entire connection before retrying. The next retry creates a fresh connection,
     * session, and link.
     * Applies to: connection:forced, connection:framing-error, proton:io, internal-error.
     */
    CONNECTION,

    /**
     * Do not retry — the error is permanent.
     * Applies to: unauthorized-access, not-found, message-size-exceeded.
     */
    FATAL;

    /**
     * Classifies the given error into a {@link RecoveryKind} that determines what resources
     * should be invalidated between retry attempts.
     *
     * @param error The error to classify.
     * @return The recovery kind for the given error.
     */
    public static RecoveryKind classify(Throwable error) {
        if (error == null) {
            return NONE;
        }

        // Timeouts — retry on same link, the link may still be healthy.
        if (error instanceof TimeoutException) {
            return NONE;
        }

        if (error instanceof AmqpException) {
            final AmqpException amqpError = (AmqpException) error;
            final AmqpErrorCondition condition = amqpError.getErrorCondition();

            if (condition != null) {
                switch (condition) {
                    // Connection-level errors — close the entire connection.
                    case CONNECTION_FORCED:
                    case CONNECTION_FRAMING_ERROR:
                    case CONNECTION_REDIRECT:
                    case PROTON_IO:
                    case INTERNAL_ERROR:
                        return CONNECTION;

                    // Link-level errors — close the link, keep the connection.
                    case LINK_DETACH_FORCED:
                    case LINK_STOLEN:
                    case LINK_REDIRECT:
                    case PARTITION_NOT_OWNED_ERROR:
                    case TRANSFER_LIMIT_EXCEEDED:
                        return LINK;

                    // Fatal errors — do not retry.
                    case NOT_FOUND:
                    case UNAUTHORIZED_ACCESS:
                    case LINK_PAYLOAD_SIZE_EXCEEDED:
                    case RESOURCE_LIMIT_EXCEEDED:
                    case NOT_ALLOWED:
                    case NOT_IMPLEMENTED:
                    case ENTITY_DISABLED_ERROR:
                    case ENTITY_ALREADY_EXISTS:
                    case PUBLISHER_REVOKED_ERROR:
                    case ARGUMENT_ERROR:
                    case ARGUMENT_OUT_OF_RANGE_ERROR:
                    case ILLEGAL_STATE:
                    case MESSAGE_LOCK_LOST:
                    case STORE_LOCK_LOST_ERROR:
                        return FATAL;

                    // Server-busy and timeouts — retry on same link.
                    case SERVER_BUSY_ERROR:
                    case TIMEOUT_ERROR:
                    case OPERATION_CANCELLED:
                        return NONE;

                    // Session/lock errors — link-level recovery.
                    // Session lock loss means the session link is invalid and
                    // a fresh link must be acquired for a new session.
                    case SESSION_LOCK_LOST:
                    case SESSION_CANNOT_BE_LOCKED:
                    case SESSION_NOT_FOUND:
                    case MESSAGE_NOT_FOUND:
                        return LINK;

                    default:
                        break;
                }
            }

            // Transient AMQP errors without a specific condition — link recovery.
            if (amqpError.isTransient()) {
                return LINK;
            }

            // Non-transient AMQP errors without a recognized condition — fatal.
            return FATAL;
        }

        // RequestResponseChannelClosedException — link-level (parent connection disposing).
        if (error instanceof RequestResponseChannelClosedException) {
            return LINK;
        }

        // Unknown non-AMQP errors — treat as fatal (don't retry application or SDK bugs).
        // The Go SDK defaults to CONNECTION for unknown errors, but those are AMQP-layer
        // errors (io.EOF, net.Error). Java's non-AMQP exceptions (e.g., AzureException,
        // RuntimeException) should fail fast rather than trigger connection recovery.
        return FATAL;
    }
}
