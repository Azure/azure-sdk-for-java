// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.integration.servicebus.converter;

import com.azure.spring.integration.core.AzureHeaders;

/**
 * Azure service bus internal headers for Spring Messaging messages.
 */
public class ServiceBusMessageHeaders extends AzureHeaders {

    private static final String PREFIX = AzureHeaders.PREFIX + "service_bus_";
    /**
     * There are 16 items can be set for service bus IMessage.
     * (1). 2 items are deprecated: ScheduledEnqueuedTimeUtc, Body.
     * (2). 3 items should be set by Spring message: ContentType, MessageBody. ReplyTo
     * (3). 1 item should not be set: Properties.
     * (4). The rest 10 items can be set by Spring message header:
     */
    public static final String MESSAGE_ID = PREFIX + "message_id";
    public static final String TIME_TO_LIVE = PREFIX + "time_to_live";
    public static final String SCHEDULED_ENQUEUE_TIME_UTC = PREFIX + "scheduled_enqueue_time_utc";
    public static final String SESSION_ID = PREFIX + "session_id";
    public static final String CORRELATION_ID = PREFIX + "correlation_id";
    public static final String TO = PREFIX + "to";
    public static final String LABEL = PREFIX + "label";
    public static final String REPLY_TO_SESSION_ID = PREFIX + "reply_to_session_id";
    public static final String PARTITION_KEY = PREFIX + "partition_key";
    public static final String VIA_PARTITION_KEY = PREFIX + "via_partition_key";
}
