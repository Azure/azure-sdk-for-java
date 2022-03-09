// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.eventhubs.consumer;


import com.azure.messaging.eventhubs.models.EventContext;
import com.azure.spring.cloud.service.listener.MessageListener;

/**
 * A listener to process Event Hub record events.
 */
@FunctionalInterface
public interface EventHubsRecordMessageListener extends MessageListener<EventContext> {

}
