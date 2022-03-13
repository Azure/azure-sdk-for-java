// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.core;

/**
 * Azure Event Hub internal headers for Spring Messaging messages.
 */
public class EventHubHeaders extends AzureHeaders {

    private static final String PREFIX = AzureHeaders.PREFIX + "eventhub_";

    public static final String ENQUEUED_TIME = PREFIX + "enqueued_time";
    public static final String OFFSET = PREFIX + "offset";
    public static final String SEQUENCE_NUMBER = PREFIX + "sequence_number";

    public static final String BATCH_CONVERTED_SYSTEM_PROPERTIES = PREFIX + "batch_converted_system_properties";
    public static final String BATCH_CONVERTED_APPLICATION_PROPERTIES = PREFIX + "batch_converted_application_properties";
}
