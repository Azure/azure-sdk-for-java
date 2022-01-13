// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.eventhubs.processor;


import com.azure.messaging.eventhubs.models.EventBatchContext;

/**
 * A listener to process Event Hub events.
 */
public interface BatchEventProcessingListener extends EventProcessingListener {

    /**
     * The event batch processing callback.
     * @param eventBatchContext the event batch context.
     */
    void onEventBatch(EventBatchContext eventBatchContext);

}
