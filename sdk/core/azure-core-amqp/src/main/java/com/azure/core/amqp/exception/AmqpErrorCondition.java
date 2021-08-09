// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.exception;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Contains constants common to the AMQP protocol and constants shared by Azure services.
 *
 * @see <a href="https://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-transport-v1.0-os.html#type-amqp-error">AMQP
 *     1.0: Transport Errors</a>
 * @see <a href="https://docs.microsoft.com/azure/event-hubs/event-hubs-messaging-exceptions">Azure Messaging
 *     Exceptions</a>
 */
public enum AmqpErrorCondition {
    /**
     * A peer attempted to work with a remote entity that does not exist.
     */
    NOT_FOUND("amqp:not-found"),
    /**
     * A peer attempted to work with a remote entity to which it has no access due to security settings.
     */
    UNAUTHORIZED_ACCESS("amqp:unauthorized-access"),
    /**
     * A peer exceeded its resource allocation.
     */
    RESOURCE_LIMIT_EXCEEDED("amqp:resource-limit-exceeded"),
    /**
     * The peer tried to use a frame in a manner that is inconsistent with the semantics defined in the specification.
     */
    NOT_ALLOWED("amqp:not-allowed"),
    /**
     * An internal error occurred. Operator intervention might be necessary to resume normal operation.
     */
    INTERNAL_ERROR("amqp:internal-error"),
    /**
     * The peer sent a frame that is not permitted in the current state.
     */
    ILLEGAL_STATE("amqp:illegal-state"),
    /**
     * The peer tried to use functionality that is not implemented in its partner.
     */
    NOT_IMPLEMENTED("amqp:not-implemented"),

    /**
     * The link has been attached elsewhere, causing the existing attachment to be forcibly closed.
     */
    LINK_STOLEN("amqp:link:stolen"),
    /**
     * The peer sent a larger message than is supported on the link.
     */
    LINK_PAYLOAD_SIZE_EXCEEDED("amqp:link:message-size-exceeded"),
    /**
     * An operator intervened to detach for some reason.
     */
    LINK_DETACH_FORCED("amqp:link:detach-forced"),

    /**
     * An operator intervened to close the connection for some reason. The client could retry at some later date.
     */
    CONNECTION_FORCED("amqp:connection:forced"),

    // These are errors that are specific to Azure services.
    SERVER_BUSY_ERROR("com.microsoft:server-busy"),
    /**
     * One or more arguments supplied to the method are invalid.
     */
    ARGUMENT_ERROR("com.microsoft:argument-error"),
    /**
     * One or more arguments supplied to the method are invalid.
     */
    ARGUMENT_OUT_OF_RANGE_ERROR("com.microsoft:argument-out-of-range"),
    /**
     * Request for a runtime operation on a disabled entity.
     */
    ENTITY_DISABLED_ERROR("com.microsoft:entity-disabled"),
    /**
     * Partition is not owned.
     */
    PARTITION_NOT_OWNED_ERROR("com.microsoft:partition-not-owned"),
    /**
     * Lock token associated with the message or session has expired, or the lock token is not found.
     */
    STORE_LOCK_LOST_ERROR("com.microsoft:store-lock-lost"),
    /**
     * The TokenProvider object could not acquire a token, the token is invalid, or the token does not contain the
     * claims required to perform the operation.
     */
    PUBLISHER_REVOKED_ERROR("com.microsoft:publisher-revoked"),
    /**
     * The server did not respond to the requested operation within the specified time. The server may have completed
     * the requested operation. This can happen due to network or other infrastructure delays.
     */
    TIMEOUT_ERROR("com.microsoft:timeout"),
    /**
     * Tracking Id for an exception.
     */
    TRACKING_ID_PROPERTY("com.microsoft:tracking-id"),
    /**
     * IO exceptions that occur in proton-j library.
     */
    PROTON_IO("proton:io"),
    /**
     * A connection error occurred. A valid frame header cannot be formed from the incoming byte stream.
     */
    CONNECTION_FRAMING_ERROR("amqp:connection:framing-error"),
    /**
     * The operation was cancelled.
     */
    OPERATION_CANCELLED("com.microsoft:operation-cancelled"),
    /**
     * Error condition when receiver attempts {@code complete}, {@code abandon}, {@code renewLock}, {@code deadLetter},
     * or {@code defer} on a peek-locked message whose lock had already expired.
     */
    MESSAGE_LOCK_LOST("com.microsoft:message-lock-lost"),
    /**
     * Error condition when a session receiver performs an operation on a session after its lock is expired. When a
     * client accepts a session, the session is locked to the receiver for a duration specified in the entity
     * definition. When the accepted session remains idle for the duration of lock, that is no operations performed on
     * the session, the lock expires and the session is made available to other clients.
     */
    SESSION_LOCK_LOST("com.microsoft:session-lock-lost"),
    /**
     * Error condition when a client attempts to accept a session that is already locked by another client.
     */
    SESSION_CANNOT_BE_LOCKED("com.microsoft:session-cannot-be-locked"),
    /**
     * Error condition when a receiver attempts to receive a message with sequence number and the message with that
     * sequence number is not available in the queue or subscription.
     */
    MESSAGE_NOT_FOUND("com.microsoft:message-not-found"),
    /**
     * Error condition when a receiver attempts to receive from a session that does not exist.
     */
    SESSION_NOT_FOUND("com.microsoft:session-not-found"),
    /**
     * Error condition when a subscription client tries to create a rule with the name of an already existing rule.
     */
    ENTITY_ALREADY_EXISTS("com.microsoft:entity-already-exists"),

    /**
     * The container is no longer available on the current connection. The peer SHOULD attempt reconnection to the
     * container using the details provided in the info map.
     *
     * The address provided cannot be resolved to a terminus at the current container. The info map MAY contain the
     * following information to allow the client to locate the attach to the terminus.
     *
     * hostname:
     * the hostname of the container. This is the value that SHOULD be supplied in the hostname field of the open frame,
     * and during the SASL and TLS negotiation (if used).
     *
     * network-host:
     * the DNS hostname or IP address of the machine hosting the container.
     *
     * port:
     * the port number on the machine hosting the container.
     */
    CONNECTION_REDIRECT("amqp:connection:redirect"),

    /**
     * The address provided cannot be resolved to a terminus at the current container. The info map MAY contain the
     * following information to allow the client to locate the attach to the terminus.
     *
     * hostname:
     * the hostname of the container hosting the terminus. This is the value that SHOULD be supplied in the hostname
     * field of the open frame, and during SASL and TLS negotiation (if used).
     *
     * network-host:
     * the DNS hostname or IP address of the machine hosting the container.
     *
     * port:
     * the port number on the machine hosting the container.
     *
     * address:
     * the address of the terminus at the container.
     */
    LINK_REDIRECT("amqp:link:redirect"),

    /**
     * The peer sent more message transfers than currently allowed on the link.
     */
    TRANSFER_LIMIT_EXCEEDED("amqp:link:transfer-limit-exceeded");

    private static final Map<String, AmqpErrorCondition> ERROR_CONSTANT_MAP = new HashMap<>();
    private final String errorCondition;

    static {
        for (AmqpErrorCondition error : AmqpErrorCondition.values()) {
            ERROR_CONSTANT_MAP.put(error.getErrorCondition(), error);
        }
    }

    /**
     * Creates an instance with the error condition header.
     *
     * @param errorCondition Error condition header value.
     */
    AmqpErrorCondition(String errorCondition) {
        this.errorCondition = errorCondition;
    }

    /**
     * Gets the AMQP header value for this error condition.
     *
     * @return AMQP header value for this error condition.
     */
    public String getErrorCondition() {
        return errorCondition;
    }

    /**
     * Parses a serialized value to an ErrorCondition instance.
     *
     * @param errorCondition the serialized value to parse.
     * @return the parsed ErrorCondition object, or null if unable to parse.
     * @throws NullPointerException if {@code errorCondition} is {@code null}.
     */
    public static AmqpErrorCondition fromString(String errorCondition) {
        Objects.requireNonNull(errorCondition);

        return ERROR_CONSTANT_MAP.get(errorCondition);
    }
}
