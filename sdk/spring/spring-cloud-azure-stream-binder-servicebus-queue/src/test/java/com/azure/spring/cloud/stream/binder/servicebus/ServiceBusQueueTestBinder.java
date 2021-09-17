// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.servicebus;

import com.azure.spring.cloud.stream.binder.servicebus.properties.ServiceBusConsumerProperties;
import com.azure.spring.cloud.stream.binder.servicebus.properties.ServiceBusProducerProperties;
import com.azure.spring.cloud.stream.binder.servicebus.provisioning.ServiceBusChannelProvisioner;
import com.azure.spring.servicebus.core.queue.ServiceBusQueueOperation;
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
