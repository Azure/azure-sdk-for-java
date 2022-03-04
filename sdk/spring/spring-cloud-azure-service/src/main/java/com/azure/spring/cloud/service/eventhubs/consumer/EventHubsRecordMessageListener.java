// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.eventhubs.consumer;


import com.azure.messaging.eventhubs.models.EventContext;

/**
 * A listener to process Event Hub record events.
 */
public interface EventHubsRecordMessageListener extends EventHubsMessageListener {

    /**
     * The event processing callback.
     * @param eventContext the event context.
     */
    void onEvent(EventContext eventContext);

}
