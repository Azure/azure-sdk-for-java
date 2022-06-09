// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.eventhubs.support;

import com.azure.spring.messaging.AzureHeaders;
/**
 * Azure Event Hubs internal headers for Spring Messaging messages.
 */
public final class EventHubsHeaders extends AzureHeaders {

    private EventHubsHeaders() {

    }

    /**
     * Header prefix.
     */
    private static final String PREFIX = AzureHeaders.PREFIX + "eventhubs_";

    /**
     * Enqueued time.
     */
    public static final String ENQUEUED_TIME = PREFIX + "enqueued_time";

    /**
     * Batch converted enqueued time.
     */
    public static final String BATCH_CONVERTED_ENQUEUED_TIME = PREFIX + "batch_converted_enqueued_time";

    /**
     * Offset.
     */
    public static final String OFFSET = PREFIX + "offset";

    /**
     * Batch converted offset.
     */
    public static final String BATCH_CONVERTED_OFFSET = PREFIX + "batch_converted_offset";

    /**
     * Sequence number.
     */
    public static final String SEQUENCE_NUMBER = PREFIX + "sequence_number";

    /**
     * Batch converted sequence number.
     */
    public static final String BATCH_CONVERTED_SEQUENCE_NUMBER = PREFIX + "batch_converted_sequence_number";

    /**
     * Last enqueued event properties.
     */
    public static final String LAST_ENQUEUED_EVENT_PROPERTIES = PREFIX + "last_enqueued_event_properties";

    /**
     * Batch converted system properties.
     */
    public static final String BATCH_CONVERTED_SYSTEM_PROPERTIES = PREFIX + "batch_converted_system_properties";

    /**
     * Batch converted application properties.
     */
    public static final String BATCH_CONVERTED_APPLICATION_PROPERTIES = PREFIX + "batch_converted_application_properties";
}
