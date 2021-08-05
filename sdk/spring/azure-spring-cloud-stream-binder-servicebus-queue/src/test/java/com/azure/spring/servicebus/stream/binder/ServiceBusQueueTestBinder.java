// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.stream.binder;

import com.azure.spring.integration.servicebus.queue.ServiceBusQueueOperation;
import com.azure.spring.servicebus.stream.binder.properties.ServiceBusConsumerProperties;
import com.azure.spring.servicebus.stream.binder.properties.ServiceBusProducerProperties;
import com.azure.spring.servicebus.stream.binder.provisioning.ServiceBusChannelProvisioner;
import org.springframework.cloud.stream.binder.AbstractTestBinder;
import org.springframework.cloud.stream.binder.BinderHeaders;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.context.support.GenericApplicationContext;

/**
 * @author Warren Zhu
 */
public class ServiceBusQueueTestBinder
    extends AbstractTestBinder<ServiceBusQueueMessageChannelBinder,
                                  ExtendedConsumerProperties<ServiceBusConsumerProperties>,
                                  ExtendedProducerProperties<ServiceBusProducerProperties>> {

    ServiceBusQueueTestBinder(ServiceBusQueueOperation operation) {
        ServiceBusQueueMessageChannelBinder binder = new ServiceBusQueueMessageChannelBinder(
            BinderHeaders.STANDARD_HEADERS, new ServiceBusChannelProvisioner(), operation);
        GenericApplicationContext context = new GenericApplicationContext();
        binder.setApplicationContext(context);
        this.setBinder(binder);
    }

    @Override
    public void cleanup() {
        // No-op
    }

}
