// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.eventhubs;

import com.azure.spring.cloud.stream.binder.eventhubs.properties.EventHubsConsumerProperties;
import com.azure.spring.cloud.stream.binder.eventhubs.properties.EventHubsProducerProperties;
import com.azure.spring.cloud.stream.binder.eventhubs.provisioning.EventHubsChannelProvisioner;
import com.azure.spring.integration.handler.DefaultMessageHandler;
import org.springframework.cloud.stream.binder.AbstractTestBinder;
import org.springframework.cloud.stream.binder.BinderHeaders;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.integration.core.MessageProducer;

/**
 * @author Warren Zhu
 */

public class EventHubsTestBinder extends
    AbstractTestBinder<TestEventHubsMessageChannelBinder, ExtendedConsumerProperties<EventHubsConsumerProperties>,
        ExtendedProducerProperties<EventHubsProducerProperties>> {

    public EventHubsTestBinder(DefaultMessageHandler messageHandler,
                               MessageProducer messageProducer) {
        TestEventHubsMessageChannelBinder binder = new TestEventHubsMessageChannelBinder(BinderHeaders.STANDARD_HEADERS,
            new EventHubsChannelProvisioner(), messageHandler, messageProducer);

        GenericApplicationContext applicationContext = new GenericApplicationContext();
        applicationContext.refresh();
        binder.setApplicationContext(applicationContext);
        this.setBinder(binder);
    }

    @Override
    public void cleanup() {
        // No-op
    }

}
