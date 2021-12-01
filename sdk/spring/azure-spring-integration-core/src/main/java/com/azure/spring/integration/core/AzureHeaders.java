// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.core;

/**
 * Azure internal headers for Spring Messaging messages.
 *
 * @author Warren Zhu
 */
public class AzureHeaders {

    /**
     * The prefix for all azure headers.
     */
    protected static final String PREFIX = "azure_";

    /**
     * The header of partition id.
     */
    public static final String PARTITION_ID = PREFIX + "partition_id";

    /**
     * The header of raw partition id.
     */
    public static final String RAW_PARTITION_ID = PREFIX + "raw_partition_id";

    /**
     * @deprecated Please use ServiceBusMessageHeaders.MESSAGE_ID instead.
     */
    @Deprecated
    public static final String RAW_ID = "raw_id";

    /**
     * The header of partition id.
     */
    public static final String PARTITION_KEY = PREFIX + "partition_key";

    /**
     * The header of name.
     */
    public static final String NAME = PREFIX + "name";

    /**
     * The header of scheduled enqueue message.
     */
    public static final String SCHEDULED_ENQUEUE_MESSAGE = "x-delay";

    /**
     * The check pointer header for checkpoint the specific message.
     */
    public static final String CHECKPOINTER = PREFIX + "checkpointer";

    /**
     * The header of lock token.
     */
    public static final String LOCK_TOKEN = PREFIX + "locktoken";

    /**
     * The header of message session.
     */
    public static final String MESSAGE_SESSION = PREFIX + "message_session";
}
