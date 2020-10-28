// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.ExpandableStringEnum;

import java.io.Serializable;

/**
 * Represent the operation user was performing when the error happened.
 */
public final class ServiceBusErrorSource extends ExpandableStringEnum<ServiceBusErrorSource> implements Serializable {

    private static final long serialVersionUID = -2819764417333954922L;

    /** Error while abandoning the message.*/
    public static final ServiceBusErrorSource ABANDONED = fromString("ABANDONED", ServiceBusErrorSource.class);

    /** Error while completing the message.*/
    public static final ServiceBusErrorSource COMPLETE = fromString("COMPLETE", ServiceBusErrorSource.class);

    /** Error while deferring the message.*/
    public static final ServiceBusErrorSource DEFER = fromString("DEFER", ServiceBusErrorSource.class);

    /** Error while dead-lettering the message.*/
    public static final ServiceBusErrorSource DEAD_LETTER = fromString("DEAD_LETTER",
        ServiceBusErrorSource.class);

    /** Error while receiving the message(s).*/
    public static final ServiceBusErrorSource RECEIVE = fromString("RECEIVE", ServiceBusErrorSource.class);
    
    /** Error when we could not determine the source.*/
    public static final ServiceBusErrorSource UNKNOWN = fromString("UNKNOWN", ServiceBusErrorSource.class);

    /** Error while user's code is running for a message.*/
    public static final ServiceBusErrorSource USER_CALLBACK = fromString("USER_CALLBACK", ServiceBusErrorSource.class);

}
