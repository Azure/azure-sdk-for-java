// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging;

import org.springframework.messaging.Message;

/**
 * The listener mode for consuming messages, RECORD or BATCH.
 */
public enum ListenerMode {

    /**
     * Each {@link Message} will be converted from a single record
     */
    RECORD,

    /**
     * Each {@link Message} will be converted from a collection of records
     * returned by a poll.
     */
    BATCH
}
