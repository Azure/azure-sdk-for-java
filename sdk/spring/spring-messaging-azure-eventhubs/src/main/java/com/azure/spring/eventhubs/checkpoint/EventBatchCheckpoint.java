// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.checkpoint;

import com.azure.messaging.eventhubs.models.EventBatchContext;

/**
 *
 */
public interface EventBatchCheckpoint {
    void checkpoint(EventBatchContext context);
}
