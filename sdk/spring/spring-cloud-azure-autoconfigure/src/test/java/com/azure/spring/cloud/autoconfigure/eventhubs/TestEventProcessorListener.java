// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhubs;

import com.azure.messaging.eventhubs.models.EventContext;
import com.azure.spring.service.eventhubs.processor.RecordEventProcessingListener;

public class TestEventProcessorListener implements RecordEventProcessingListener {

    @Override
    public void onEvent(EventContext eventContext) {

    }
}
