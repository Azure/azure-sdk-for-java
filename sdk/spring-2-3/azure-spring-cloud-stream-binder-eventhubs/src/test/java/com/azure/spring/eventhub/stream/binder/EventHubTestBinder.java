// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhub.stream.binder;

import com.azure.spring.eventhub.stream.binder.properties.EventHubProducerProperties;
import com.azure.spring.eventhub.stream.binder.provisioning.EventHubChannelProvisioner;
import com.azure.spring.eventhub.stream.binder.properties.EventHubConsumerProperties;
import com.azure.spring.integration.eventhub.api.EventHubOperation;
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
