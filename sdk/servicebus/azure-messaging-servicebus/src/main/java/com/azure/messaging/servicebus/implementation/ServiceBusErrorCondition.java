// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.servicebus.implementation;

import com.azure.core.util.ExpandableStringEnum;

import java.util.HashSet;
import java.util.Set;

/**
 * A set of error conditions thrown by Service Bus.
 */
class ServiceBusErrorCondition extends ExpandableStringEnum<ServiceBusErrorCondition> {
    private static final String VENDOR = "com.microsoft";
    private static final Set<ServiceBusErrorCondition> NON_TRANSIENT_ERRORS;

    static final ServiceBusErrorCondition OPERATION_CANCELLED = fromString("operation-cancelled");
    /**
     * Error condition when receiver attempts {@code complete}, {@code abandon}, {@code renewLock}, {@code deadLetter},
     * or {@code defer} on a peek-locked message whose lock had already expired.
     */
    static final ServiceBusErrorCondition MESSAGE_LOCK_LOST = fromString("message-lock-lost");

    /**
     * Error condition when a session receiver performs an operation on a session after its lock is expired. When a
     * client accepts a session, the session is locked to the receiver for a duration specified in the entity
     * definition. When the accepted session remains idle for the duration of lock, that is no operations performed on
     * the session, the lock expires and the session is made available to other clients.
     */
    static final ServiceBusErrorCondition SESSION_LOCK_LOST = fromString("session-lock-lost");

    /**
     * Error condition when a client attempts to accept a session that is already locked by another client.
     */
    static final ServiceBusErrorCondition SESSION_CANNOT_BE_LOCKED_ERROR = fromString("session-cannot-be-locked");

    /**
     * Error condition when a receiver attempts to receive a message with sequence number and the message with that
     * sequence number is not available in the queue or subscription.
     */
    static final ServiceBusErrorCondition MESSAGE_NOT_FOUND = fromString("message-not-found");

    /**
     * Error condition when a subscription client tries to create a rule with the name of an already existing rule.
     */
    static final ServiceBusErrorCondition ENTITY_ALREADY_EXISTS = fromString("entity-already-exists");

    static {
        NON_TRANSIENT_ERRORS = new HashSet<>();
        NON_TRANSIENT_ERRORS.add(OPERATION_CANCELLED);
        NON_TRANSIENT_ERRORS.add(MESSAGE_LOCK_LOST);
        NON_TRANSIENT_ERRORS.add(SESSION_LOCK_LOST);
        NON_TRANSIENT_ERRORS.add(SESSION_CANNOT_BE_LOCKED_ERROR);
        NON_TRANSIENT_ERRORS.add(MESSAGE_NOT_FOUND);
        NON_TRANSIENT_ERRORS.add(ENTITY_ALREADY_EXISTS);
    }

    static ServiceBusErrorCondition fromString(String condition) {
        return fromString(VENDOR + ":" + condition, ServiceBusErrorCondition.class);
    }

    boolean isTransient() {
        return !NON_TRANSIENT_ERRORS.contains(this);
    }
}
