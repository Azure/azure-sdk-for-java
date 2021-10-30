// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.servicebus.factory;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.spring.service.core.PropertyMapper;
import com.azure.spring.service.servicebus.properties.ServiceBusCommonDescriptor;
import com.azure.spring.service.servicebus.properties.ServiceBusProducerDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public class ServiceBusSenderClientBuilderFactory extends CommonServiceBusClientBuilderFactory{

    private ServiceBusClientBuilder.ServiceBusSenderClientBuilder senderClientBuilder;
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceBusSenderClientBuilderFactory.class);

    public ServiceBusSenderClientBuilderFactory(ServiceBusCommonDescriptor serviceBusProperties) {
        super(serviceBusProperties);
    }

    @Override
    protected void configureService(ServiceBusClientBuilder builder) {
        super.configureService(builder);
        PropertyMapper mapper = new PropertyMapper();
        senderClientBuilder = builder.sender();

        mapper.from(((ServiceBusProducerDescriptor) this.serviceBusProperties).getQueueName())
              .whenNonNull().to(senderClientBuilder::queueName);
        mapper.from(((ServiceBusProducerDescriptor) this.serviceBusProperties).getTopicName())
              .whenNonNull().to(senderClientBuilder::topicName);

        if (StringUtils.hasText(((ServiceBusProducerDescriptor) serviceBusProperties).getQueueName())
            && StringUtils.hasText(((ServiceBusProducerDescriptor) serviceBusProperties).getTopicName())) {
            LOGGER.warn("Both queue and topic name configured for a service bus sender, but only the queue name will take effective");
        }
    }

    public ServiceBusClientBuilder.ServiceBusSenderClientBuilder getSenderClientBuilder() {
        return senderClientBuilder;
    }
}
