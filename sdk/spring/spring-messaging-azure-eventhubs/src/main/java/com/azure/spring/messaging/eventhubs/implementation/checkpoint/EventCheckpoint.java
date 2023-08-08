// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.eventhubs.implementation.checkpoint;

import com.azure.messaging.eventhubs.models.EventContext;

/**
 * Event checkpoint interface.
 */
interface EventCheckpoint {

    /**
     * Checkpoint for the event context.
     * @param context the event context.
     */
    void checkpoint(EventContext context);
}
