// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.ExpandableStringEnum;

/**
 * The set of well-known reasons for an Service Bus operation failure that was the cause of an exception.
 */
public final class ServiceBusFailureReason extends ExpandableStringEnum<ServiceBusFailureReason> {
    // NOTE: this list is intended to mirror the reasons we have in .net
    // https://github.com/Azure/azure-sdk-for-net/blob/main/sdk/servicebus/Azure.Messaging
    // .ServiceBus/src/Primitives/ServiceBusFailureReason.cs

    /** The exception was the result of a general error within the client library. */
    public static final ServiceBusFailureReason GENERAL_ERROR = fromString("GENERAL_ERROR",
        ServiceBusFailureReason.class);

    /** The lock on the message is lost. Callers should call attempt to receive and process the message again. */
    public static final ServiceBusFailureReason MESSAGE_LOCK_LOST = fromString("MESSAGE_LOCK_LOST",
        ServiceBusFailureReason.class);

    /** The requested message was not found. */
    public static final ServiceBusFailureReason MESSAGE_NOT_FOUND = fromString("MESSAGE_NOT_FOUND",
        ServiceBusFailureReason.class);

    /** A message is larger than the maximum size allowed for its transport. */
    public static final ServiceBusFailureReason MESSAGE_SIZE_EXCEEDED = fromString("MESSAGE_SIZE_EXCEEDED",
        ServiceBusFailureReason.class);

    /** An entity with the same name exists under the same namespace. */
    public static final ServiceBusFailureReason MESSAGING_ENTITY_ALREADY_EXISTS = fromString(
        "MESSAGING_ENTITY_ALREADY_EXISTS", ServiceBusFailureReason.class);

    /** The Messaging Entity is disabled. Enable the entity again using Portal. */
    public static final ServiceBusFailureReason MESSAGING_ENTITY_DISABLED = fromString("MESSAGING_ENTITY_DISABLED",
        ServiceBusFailureReason.class);

    /** A Service Bus resource cannot be found by the Service Bus service. */
    public static final ServiceBusFailureReason MESSAGING_ENTITY_NOT_FOUND = fromString("MESSAGING_ENTITY_NOT_FOUND",
        ServiceBusFailureReason.class);

    /** The quota applied to an Service Bus resource has been exceeded while interacting with the Azure Service Bus
     * service. */
    public static final ServiceBusFailureReason QUOTA_EXCEEDED = fromString("QUOTA_EXCEEDED",
        ServiceBusFailureReason.class);

    /** The Azure Service Bus service reports that it is busy in response to a client request to perform an operation
     * . */
    public static final ServiceBusFailureReason SERVICE_BUSY = fromString("SERVICE_BUSY",
        ServiceBusFailureReason.class);

    /** An operation or other request timed out while interacting with the Azure Service Bus service. */
    public static final ServiceBusFailureReason SERVICE_TIMEOUT = fromString("SERVICE_TIMEOUT",
        ServiceBusFailureReason.class);

    /** There was a general communications error encountered when interacting with the Azure Service Bus service. */
    public static final ServiceBusFailureReason SERVICE_COMMUNICATION_ERROR = fromString(
        "SERVICE_COMMUNICATION_ERROR", ServiceBusFailureReason.class);

    /** The requested session cannot be locked. */
    public static final ServiceBusFailureReason SESSION_CANNOT_BE_LOCKED = fromString("SESSION_CANNOT_BE_LOCKED",
        ServiceBusFailureReason.class);

    /** The lock on the session has expired. Callers should request the session again. */
    public static final ServiceBusFailureReason SESSION_LOCK_LOST = fromString("SESSION_LOCK_LOST",
        ServiceBusFailureReason.class);

    /** The user doesn't have access to the entity. */
    public static final ServiceBusFailureReason UNAUTHORIZED = fromString("UNAUTHORIZED",
        ServiceBusFailureReason.class);
}
