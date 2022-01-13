// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.checkpoint;

import com.azure.messaging.eventhubs.models.EventBatchContext;

/**
 * Event batch checkpoint interface.
 */
public interface EventBatchCheckpoint {

    /**
     * Checkpoint for the event batch context.
     * @param context the event batch context.
     */
    void checkpoint(EventBatchContext context);
}
