// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.ExpandableStringEnum;

/**
 * Represents the operation a Service Bus client was performing when the error happened.
 */
public final class ServiceBusErrorSource extends ExpandableStringEnum<ServiceBusErrorSource> {

    /** Error while abandoning the message.*/
    public static final ServiceBusErrorSource ABANDON = fromString("ABANDON", ServiceBusErrorSource.class);

    /** Error while completing the message.*/
    public static final ServiceBusErrorSource COMPLETE = fromString("COMPLETE", ServiceBusErrorSource.class);

    /** Error while receiving the message(s).*/
    public static final ServiceBusErrorSource RECEIVE = fromString("RECEIVE", ServiceBusErrorSource.class);

    /** Error while renewing lock.*/
    public static final ServiceBusErrorSource RENEW_LOCK = fromString("RENEW_LOCK", ServiceBusErrorSource.class);

    /** Error when we could not determine the source.*/
    public static final ServiceBusErrorSource UNKNOWN = fromString("UNKNOWN", ServiceBusErrorSource.class);

    /** Error while user's code is running for a message.*/
    public static final ServiceBusErrorSource USER_CALLBACK = fromString("USER_CALLBACK",
        ServiceBusErrorSource.class);

    /** Error while session is accepted.*/
    public static final ServiceBusErrorSource ACCEPT_SESSION = fromString("ACCEPT_SESSION",
        ServiceBusErrorSource.class);

    /** Error while session is closed.*/
    public static final ServiceBusErrorSource CLOSE_SESSION = fromString("CLOSE_SESSION",
        ServiceBusErrorSource.class);

    /** Error while sending a message.*/
    public static final ServiceBusErrorSource SEND = fromString("SEND",
            ServiceBusErrorSource.class);

    /** Error while trying to do an operation on the management link. */
    public static final ServiceBusErrorSource MANAGEMENT = fromString("MANAGEMENT",
            ServiceBusErrorSource.class);
}
