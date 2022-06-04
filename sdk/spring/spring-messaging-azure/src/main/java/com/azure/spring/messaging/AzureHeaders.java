// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging;

/**
 * The Azure specific message headers constants.
 */
public class AzureHeaders {

    /**
     * To construct an {@link AzureHeaders} instance.
     */
    protected AzureHeaders() {

    }

    protected static final String PREFIX = "azure_";

    public static final String PARTITION_ID = PREFIX + "partition_id";
    public static final String RAW_PARTITION_ID = PREFIX + "raw_partition_id";

    public static final String PARTITION_KEY = PREFIX + "partition_key";
    public static final String BATCH_CONVERTED_PARTITION_KEY = PREFIX + "batch_converted_partition_key";

    public static final String NAME = PREFIX + "name";

    public static final String SCHEDULED_ENQUEUE_MESSAGE = "x-delay";

    /**
     * The {@value CHECKPOINTER} header for checkpoint the specific message.
     */
    public static final String CHECKPOINTER = PREFIX + "checkpointer";

    public static final String MESSAGE_SESSION = PREFIX + "message_session";
}
