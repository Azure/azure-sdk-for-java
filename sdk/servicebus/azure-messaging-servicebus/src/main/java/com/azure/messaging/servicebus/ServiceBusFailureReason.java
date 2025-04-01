// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import io.clientcore.core.utils.ExpandableEnum;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The set of well-known reasons for a Service Bus operation failure that was the cause of an exception.
 */
public final class ServiceBusFailureReason implements ExpandableEnum<String> {
    private static final Map<String, ServiceBusFailureReason> VALUES = new ConcurrentHashMap<>();
    private final String reason;

    // NOTE: this list is intended to mirror the reasons we have in .net
    // https://github.com/Azure/azure-sdk-for-net/blob/main/sdk/servicebus/Azure.Messaging
    // .ServiceBus/src/Primitives/ServiceBusFailureReason.cs

    /** The exception was the result of a general error within the client library. */
    public static final ServiceBusFailureReason GENERAL_ERROR
        = fromString("GENERAL_ERROR");

    /** The lock on the message is lost. Callers should call attempt to receive and process the message again. */
    public static final ServiceBusFailureReason MESSAGE_LOCK_LOST
        = fromString("MESSAGE_LOCK_LOST");

    /** The requested message was not found. */
    public static final ServiceBusFailureReason MESSAGE_NOT_FOUND
        = fromString("MESSAGE_NOT_FOUND");

    /** A message is larger than the maximum size allowed for its transport. */
    public static final ServiceBusFailureReason MESSAGE_SIZE_EXCEEDED
        = fromString("MESSAGE_SIZE_EXCEEDED");

    /** An entity with the same name exists under the same namespace. */
    public static final ServiceBusFailureReason MESSAGING_ENTITY_ALREADY_EXISTS
        = fromString("MESSAGING_ENTITY_ALREADY_EXISTS");

    /** The Messaging Entity is disabled. Enable the entity again using Portal. */
    public static final ServiceBusFailureReason MESSAGING_ENTITY_DISABLED
        = fromString("MESSAGING_ENTITY_DISABLED");

    /** A Service Bus resource cannot be found by the Service Bus service. */
    public static final ServiceBusFailureReason MESSAGING_ENTITY_NOT_FOUND
        = fromString("MESSAGING_ENTITY_NOT_FOUND");

    /** The quota applied to an Service Bus resource has been exceeded while interacting with the Azure Service Bus
     * service. */
    public static final ServiceBusFailureReason QUOTA_EXCEEDED
        = fromString("QUOTA_EXCEEDED");

    /** The Azure Service Bus service reports that it is busy in response to a client request to perform an operation
     * . */
    public static final ServiceBusFailureReason SERVICE_BUSY
        = fromString("SERVICE_BUSY");

    /** An operation or other request timed out while interacting with the Azure Service Bus service. */
    public static final ServiceBusFailureReason SERVICE_TIMEOUT
        = fromString("SERVICE_TIMEOUT");

    /** There was a general communications error encountered when interacting with the Azure Service Bus service. */
    public static final ServiceBusFailureReason SERVICE_COMMUNICATION_ERROR
        = fromString("SERVICE_COMMUNICATION_ERROR");

    /** The requested session cannot be locked. */
    public static final ServiceBusFailureReason SESSION_CANNOT_BE_LOCKED
        = fromString("SESSION_CANNOT_BE_LOCKED");

    /** The lock on the session has expired. Callers should request the session again. */
    public static final ServiceBusFailureReason SESSION_LOCK_LOST
        = fromString("SESSION_LOCK_LOST");

    /** The user doesn't have access to the entity. */
    public static final ServiceBusFailureReason UNAUTHORIZED
        = fromString("UNAUTHORIZED");

    private ServiceBusFailureReason(String reason) {
        this.reason = reason;
    }

    /**
     * Creates or finds an ServiceBusFailureReason from its string representation.
     *
     * @param reason the source to look for
     * @return the corresponding ServiceBusFailureReason
     */
    public static ServiceBusFailureReason fromString(String reason) {
        if (reason == null) {
            return null;
        }
        return VALUES.computeIfAbsent(reason, ServiceBusFailureReason::new);
    }

    @Override
    public String getValue() {
        return this.reason;
    }

    @Override
    public String toString() {
        return this.reason;
    }
}
