// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus.stream.binder;

import com.microsoft.azure.servicebus.stream.binder.properties.ServiceBusConsumerProperties;
import com.microsoft.azure.servicebus.stream.binder.properties.ServiceBusProducerProperties;
import com.microsoft.azure.servicebus.stream.binder.provisioning.ServiceBusChannelProvisioner;
import com.microsoft.azure.spring.integration.servicebus.topic.ServiceBusTopicOperation;
import org.springframework.cloud.stream.binder.AbstractTestBinder;
import org.springframework.cloud.stream.binder.BinderHeaders;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.context.support.GenericApplicationContext;

/**
 * @author Warren Zhu
 */
public class ServiceBusTopicTestBinder extends
        AbstractTestBinder<ServiceBusTopicMessageChannelBinder,
                ExtendedConsumerProperties<ServiceBusConsumerProperties>,
                ExtendedProducerProperties<ServiceBusProducerProperties>> {

    ServiceBusTopicTestBinder(ServiceBusTopicOperation operation) {

        ServiceBusTopicMessageChannelBinder binder =
                new ServiceBusTopicMessageChannelBinder(BinderHeaders.STANDARD_HEADERS,
                        new ServiceBusChannelProvisioner(), operation);
        GenericApplicationContext context = new GenericApplicationContext();
        binder.setApplicationContext(context);
        this.setBinder(binder);
    }

    @Override
    public void cleanup() {
        // No-op
    }

}
