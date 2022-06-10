// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.eventhubs.implementation.checkpoint;

import com.azure.messaging.eventhubs.models.EventBatchContext;

/**
 * Event batch checkpoint interface.
 */
interface EventBatchCheckpoint {

    /**
     * Checkpoint for the event batch context.
     * @param context the event batch context.
     */
    void checkpoint(EventBatchContext context);
}
