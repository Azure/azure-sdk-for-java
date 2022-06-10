// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.eventhubs.consumer;


import com.azure.messaging.eventhubs.models.EventBatchContext;
import com.azure.spring.cloud.service.listener.MessageListener;

/**
 * A listener to process Event Hub batch events.
 */
@FunctionalInterface
public interface EventHubsBatchMessageListener extends MessageListener<EventBatchContext> {

}
