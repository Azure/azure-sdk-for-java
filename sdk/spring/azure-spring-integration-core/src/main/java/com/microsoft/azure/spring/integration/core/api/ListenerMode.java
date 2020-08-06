// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.integration.core.api;

import org.springframework.messaging.Message;

/**
 * The listener mode, RECORD or BATCH.
 *
 * @author Warren Zhu
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
