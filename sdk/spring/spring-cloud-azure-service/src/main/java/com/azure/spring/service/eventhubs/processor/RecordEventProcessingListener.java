// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.eventhubs.processor;


import com.azure.messaging.eventhubs.models.EventContext;

/**
 * A listener to process Event Hub events.
 */
public interface RecordEventProcessingListener extends EventProcessingListener {

    /**
     * Listener method on event.
     * @param eventContext the event context.
     */
    void onEvent(EventContext eventContext);

}
