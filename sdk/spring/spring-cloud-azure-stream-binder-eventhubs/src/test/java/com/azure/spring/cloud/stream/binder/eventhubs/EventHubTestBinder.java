// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.eventhubs;

import com.azure.spring.cloud.stream.binder.eventhubs.EventHubMessageChannelBinder;
import com.azure.spring.cloud.stream.binder.eventhubs.properties.EventHubProducerProperties;
import com.azure.spring.cloud.stream.binder.eventhubs.provisioning.EventHubChannelProvisioner;
import com.azure.spring.cloud.stream.binder.eventhubs.properties.EventHubConsumerProperties;
import com.azure.spring.eventhubs.core.EventHubOperation;
import org.springframework.cloud.stream.binder.AbstractTestBinder;
import org.springframework.cloud.stream.binder.BinderHeaders;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.context.support.GenericApplicationContext;

/**
 * @author Warren Zhu
 */

public class EventHubTestBinder extends
        AbstractTestBinder<EventHubMessageChannelBinder, ExtendedConsumerProperties<EventHubConsumerProperties>,
                ExtendedProducerProperties<EventHubProducerProperties>> {

    EventHubTestBinder(EventHubOperation eventHubOperation) {
        EventHubMessageChannelBinder binder =
                new EventHubMessageChannelBinder(BinderHeaders.STANDARD_HEADERS, new EventHubChannelProvisioner(),
                        eventHubOperation);
        GenericApplicationContext context = new GenericApplicationContext();
        binder.setApplicationContext(context);
        this.setBinder(binder);
    }

    @Override
    public void cleanup() {
        // No-op
    }

}
