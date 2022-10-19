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

    /**
     * Header prefix.
     */
    protected static final String PREFIX = "azure_";

    /**
     * Partition ID.
     */
    public static final String PARTITION_ID = PREFIX + "partition_id";

    /**
     * Raw partition ID.
     */
    public static final String RAW_PARTITION_ID = PREFIX + "raw_partition_id";

    /**
     * Partition key.
     */
    public static final String PARTITION_KEY = PREFIX + "partition_key";

    /**
     * Batch converted partition key.
     */
    public static final String BATCH_CONVERTED_PARTITION_KEY = PREFIX + "batch_converted_partition_key";

    /**
     * Name.
     */
    public static final String NAME = PREFIX + "name";

    /**
     * Scheduled enqueue message.
     */
    public static final String SCHEDULED_ENQUEUE_MESSAGE = "x-delay";

    /**
     * The CHECKPOINTER header for checkpoint the specific message.
     */
    public static final String CHECKPOINTER = PREFIX + "checkpointer";

    /**
     * Message session.
     */
    public static final String MESSAGE_SESSION = PREFIX + "message_session";
}
