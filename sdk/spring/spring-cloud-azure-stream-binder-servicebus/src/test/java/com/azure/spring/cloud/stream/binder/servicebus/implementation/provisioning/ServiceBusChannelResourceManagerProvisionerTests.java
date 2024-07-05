// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.servicebus.implementation.provisioning;

import com.azure.spring.cloud.resourcemanager.implementation.provisioning.ServiceBusProvisioner;
import com.azure.spring.cloud.resourcemanager.implementation.provisioning.properties.ServiceBusQueueProperties;
import com.azure.spring.cloud.resourcemanager.implementation.provisioning.properties.ServiceBusTopicProperties;
import com.azure.spring.cloud.service.servicebus.properties.ServiceBusEntityType;
import com.azure.spring.cloud.stream.binder.servicebus.core.properties.ServiceBusConsumerProperties;
import com.azure.spring.cloud.stream.binder.servicebus.core.properties.ServiceBusProducerProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ServiceBusChannelResourceManagerProvisionerTests {

    private ServiceBusProvisioner provisioner;
    private ServiceBusProducerProperties producerProperties;
    private ServiceBusConsumerProperties consumerProperties;

    private ServiceBusChannelResourceManagerProvisioner resourceManagerProvisioner;

    private final String entityName = "entityName";

    @BeforeEach
    void beforeEach() {
        provisioner = spy(ServiceBusProvisioner.class);
        producerProperties = new ServiceBusProducerProperties();
        producerProperties.setEntityName(entityName);
        consumerProperties = new ServiceBusConsumerProperties();
        consumerProperties.setEntityName(entityName);
        String namespaceName = "namespaceName";
        resourceManagerProvisioner = spy(new ServiceBusChannelResourceManagerProvisioner(namespaceName, provisioner));
    }

    @ParameterizedTest
    @ValueSource(strings = { "QUEUE", "TOPIC" })
    void provisionProducerDestination(String entityType) {
        ServiceBusEntityType serviceBusEntityType = ServiceBusEntityType.valueOf(entityType);
        producerProperties.setEntityType(serviceBusEntityType);
        ExtendedProducerProperties<ServiceBusProducerProperties> extendedProperties =
            new ExtendedProducerProperties<>(producerProperties);
        resourceManagerProvisioner.provisionProducerDestination(entityName, extendedProperties);
        if (ServiceBusEntityType.QUEUE == serviceBusEntityType) {
            verify(provisioner, times(1)).provisionQueue(anyString(), anyString(), any(ServiceBusQueueProperties.class));
        } else {
            verify(provisioner, times(1)).provisionTopic(anyString(), anyString(), any(ServiceBusTopicProperties.class));
        }
        verify(resourceManagerProvisioner, times(1)).provisionProducerDestination(entityName, extendedProperties);
    }

    @ParameterizedTest
    @ValueSource(strings = { "QUEUE", "TOPIC" })
    void provisionConsumerDestination(String entityType) {
        ServiceBusEntityType serviceBusEntityType = ServiceBusEntityType.valueOf(entityType);
        consumerProperties.setEntityType(serviceBusEntityType);
        ExtendedConsumerProperties<ServiceBusConsumerProperties> extendedProperties =
            new ExtendedConsumerProperties<>(consumerProperties);

        String subscriptionName = "subscriptionName";
        resourceManagerProvisioner.provisionConsumerDestination(entityName, subscriptionName, extendedProperties);
        if (ServiceBusEntityType.QUEUE == serviceBusEntityType) {
            verify(provisioner, times(1)).provisionQueue(anyString(), anyString(), any(ServiceBusQueueProperties.class));
        } else {
            verify(provisioner, times(1)).provisionSubscription(anyString(), anyString(), anyString(), any(ServiceBusTopicProperties.class));
        }
        verify(resourceManagerProvisioner, times(1)).provisionConsumerDestination(entityName, subscriptionName, extendedProperties);
    }
}
