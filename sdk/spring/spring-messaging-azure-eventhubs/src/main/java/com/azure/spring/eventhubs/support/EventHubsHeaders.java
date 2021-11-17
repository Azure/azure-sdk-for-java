// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.support;

import com.azure.spring.messaging.AzureHeaders;
/**
 * Azure Event Hubs internal headers for Spring Messaging messages.
 */
public class EventHubsHeaders extends AzureHeaders {

    private static final String PREFIX = AzureHeaders.PREFIX + "eventhub_";

    public static final String ENQUEUED_TIME = PREFIX + "enqueued_time";
    public static final String OFFSET = PREFIX + "offset";
    public static final String SEQUENCE_NUMBER = PREFIX + "sequence_number";

}
