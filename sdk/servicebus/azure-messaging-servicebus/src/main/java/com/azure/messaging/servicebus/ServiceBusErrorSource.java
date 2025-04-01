// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import io.clientcore.core.utils.ExpandableEnum;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents the operation a Service Bus client was performing when the error happened.
 */
public final class ServiceBusErrorSource implements ExpandableEnum<String> {
    private static final Map<String, ServiceBusErrorSource> VALUES = new ConcurrentHashMap<>();
    private final String source;


    /** Error while abandoning the message.*/
    public static final ServiceBusErrorSource ABANDON = fromString("ABANDON");

    /** Error while completing the message.*/
    public static final ServiceBusErrorSource COMPLETE = fromString("COMPLETE");

    /** Error while receiving the message(s).*/
    public static final ServiceBusErrorSource RECEIVE = fromString("RECEIVE");

    /** Error while renewing lock.*/
    public static final ServiceBusErrorSource RENEW_LOCK = fromString("RENEW_LOCK");

    /** Error when we could not determine the source.*/
    public static final ServiceBusErrorSource UNKNOWN = fromString("UNKNOWN");

    /** Error while user's code is running for a message.*/
    public static final ServiceBusErrorSource USER_CALLBACK = fromString("USER_CALLBACK");

    /** Error while session is accepted.*/
    public static final ServiceBusErrorSource ACCEPT_SESSION
        = fromString("ACCEPT_SESSION");

    /** Error while session is closed.*/
    public static final ServiceBusErrorSource CLOSE_SESSION = fromString("CLOSE_SESSION");

    /** Error while sending a message.*/
    public static final ServiceBusErrorSource SEND = fromString("SEND");

    /** Error while trying to do an operation on the management link. */
    public static final ServiceBusErrorSource MANAGEMENT = fromString("MANAGEMENT");

    private ServiceBusErrorSource(String source) {
        this.source = source;
    }

    /**
     * Creates or finds an ServiceBusErrorSource from its string representation.
     *
     * @param source the source to look for
     * @return the corresponding ServiceBusErrorSource
     */
    public static ServiceBusErrorSource fromString(String source) {
        if (source == null) {
            return null;
        }
        return VALUES.computeIfAbsent(source, ServiceBusErrorSource::new);
    }

    @Override
    public String getValue() {
        return this.source;
    }

    @Override
    public String toString() {
        return this.source;
    }
}
