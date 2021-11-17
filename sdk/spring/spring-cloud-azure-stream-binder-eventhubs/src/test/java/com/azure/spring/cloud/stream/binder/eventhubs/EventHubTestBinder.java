// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.eventhubs;

import com.azure.spring.cloud.stream.binder.eventhubs.properties.EventHubsConsumerProperties;
import com.azure.spring.cloud.stream.binder.eventhubs.properties.EventHubsProducerProperties;
import com.azure.spring.cloud.stream.binder.eventhubs.provisioning.EventHubsChannelProvisioner;
import org.springframework.cloud.stream.binder.AbstractTestBinder;
import org.springframework.cloud.stream.binder.BinderHeaders;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.context.support.GenericApplicationContext;

/**
 * @author Warren Zhu
 */

public class EventHubTestBinder extends
        AbstractTestBinder<EventHubsMessageChannelBinder, ExtendedConsumerProperties<EventHubsConsumerProperties>,
                ExtendedProducerProperties<EventHubsProducerProperties>> {

    EventHubTestBinder() {
        EventHubsMessageChannelBinder binder = new EventHubsMessageChannelBinder(BinderHeaders.STANDARD_HEADERS,
            new EventHubsChannelProvisioner());

        binder.setApplicationContext(new GenericApplicationContext());
        this.setBinder(binder);
    }

    @Override
    public void cleanup() {
        // No-op
    }

}
