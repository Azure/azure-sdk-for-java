// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.servicebus;

import com.azure.spring.cloud.stream.binder.servicebus.properties.ServiceBusConsumerProperties;
import com.azure.spring.cloud.stream.binder.servicebus.properties.ServiceBusProducerProperties;
import com.azure.spring.cloud.stream.binder.servicebus.provisioning.ServiceBusChannelProvisioner;
import org.springframework.cloud.stream.binder.AbstractTestBinder;
import org.springframework.cloud.stream.binder.BinderHeaders;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.context.support.GenericApplicationContext;

/**
 * @author Warren Zhu
 */
public class ServiceBusTestBinder
    extends AbstractTestBinder<ServiceBusMessageChannelBinder,
                                  ExtendedConsumerProperties<ServiceBusConsumerProperties>,
                                  ExtendedProducerProperties<ServiceBusProducerProperties>> {

    ServiceBusTestBinder() {
        ServiceBusMessageChannelBinder binder = new ServiceBusMessageChannelBinder(
            BinderHeaders.STANDARD_HEADERS, new ServiceBusChannelProvisioner());
        GenericApplicationContext context = new GenericApplicationContext();
        context.refresh();
        binder.setApplicationContext(context);
        this.setBinder(binder);
    }

    @Override
    public void cleanup() {
        // No-op
    }

}
