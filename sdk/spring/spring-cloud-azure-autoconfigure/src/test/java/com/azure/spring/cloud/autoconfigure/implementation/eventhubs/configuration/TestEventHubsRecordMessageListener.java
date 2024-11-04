// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.eventhubs.configuration;

import com.azure.messaging.eventhubs.models.EventContext;
import com.azure.spring.cloud.service.eventhubs.consumer.EventHubsRecordMessageListener;

public class TestEventHubsRecordMessageListener implements EventHubsRecordMessageListener {

    @Override
    public void onMessage(EventContext eventContext) {

    }
}
