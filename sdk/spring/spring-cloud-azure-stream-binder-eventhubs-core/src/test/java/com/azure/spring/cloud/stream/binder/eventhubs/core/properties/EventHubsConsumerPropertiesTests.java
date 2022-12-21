// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.eventhubs.core.properties;

import com.azure.messaging.eventhubs.LoadBalancingStrategy;
import com.azure.spring.cloud.service.eventhubs.properties.LoadBalancingProperties;
import com.azure.spring.messaging.eventhubs.core.checkpoint.CheckpointConfig;
import com.azure.spring.messaging.eventhubs.core.checkpoint.CheckpointMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.azure.spring.cloud.stream.binder.eventhubs.core.properties.EventHubsProducerPropertiesTests.CONNECTION_STRING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class EventHubsConsumerPropertiesTests {

    private EventHubsConsumerProperties consumerProperties;

    @BeforeEach
    void beforeEach() {
        consumerProperties = new EventHubsConsumerProperties();
    }

    @Test
    void checkpointDefaults() {
        CheckpointConfig checkpoint = consumerProperties.getCheckpoint();
        assertNotNull(checkpoint);
        assertEquals(CheckpointMode.RECORD, checkpoint.getMode());
        assertNull(checkpoint.getCount());
        assertNull(checkpoint.getInterval());
    }

    @Test
    void customCheckpoint() {
        CheckpointConfig checkpoint = consumerProperties.getCheckpoint();
        checkpoint.setMode(CheckpointMode.BATCH);
        assertEquals(CheckpointMode.BATCH, checkpoint.getMode());
    }

    @Test
    void loadBalancingDefaults() {
        LoadBalancingProperties loadBalancing = consumerProperties.getLoadBalancing();
        assertNotNull(loadBalancing);
        assertEquals(LoadBalancingStrategy.BALANCED, loadBalancing.getStrategy());
    }

    @Test
    void customLoadBalancing() {
        LoadBalancingProperties loadBalancing = consumerProperties.getLoadBalancing();
        loadBalancing.setStrategy(LoadBalancingStrategy.GREEDY);
        assertEquals(LoadBalancingStrategy.GREEDY, loadBalancing.getStrategy());
    }

    @Test
    void otherDefaults() {
        assertNotNull(consumerProperties.getInitialPartitionEventPosition());
        assertNotNull(consumerProperties.getBatch());
    }
    @Test
    void domainNameDefaultsToNull() {
        assertNull(consumerProperties.getDomainName());
    }

    @Test
    void customDomainNameShouldSet() {
        consumerProperties.setDomainName("new.servicebus.windows.net");
        assertEquals("new.servicebus.windows.net", consumerProperties.getDomainName());
    }

    @Test
    void getFqdnWhenNamespaceIsNullButConnectionStringIsNot() {
        consumerProperties.setConnectionString(CONNECTION_STRING);
        assertEquals("test.servicebus.windows.net", consumerProperties.getFullyQualifiedNamespace());
    }

    @Test
    void getFqdnWhenNamespaceAndDomainNameAreNotNull() {
        consumerProperties.setNamespace("dev-namespace");
        consumerProperties.setDomainName("servicebus.windows.net");
        assertEquals("dev-namespace.servicebus.windows.net", consumerProperties.getFullyQualifiedNamespace());
    }

    @Test
    void getFqdnWhenNamespaceAndDomainAreNull() {
        assertNull(consumerProperties.getFullyQualifiedNamespace());
    }

    @Test
    void getFqdnWhenNamespaceIsNullButDomainNameIsNot() {
        consumerProperties.setDomainName("servicebus.windows.net");
        assertNull(consumerProperties.getFullyQualifiedNamespace());
    }

    @Test
    void getFqdnWhenDomainNameIsNullButNamespaceIsNot() {
        consumerProperties.setNamespace("test");
        assertNull(consumerProperties.getFullyQualifiedNamespace());
    }

    @Test
    void getFqdnReturnNullWhenNamespaceAndConnectionStringAreNull() {
        assertNull(consumerProperties.getFullyQualifiedNamespace());
    }

    @Test
    void amqpTransportTypeDefaultIsNull() {
        assertNull(consumerProperties.getClient().getTransportType());
    }

    @Test
    void getEventHubNameWhenNamespaceIsNull() {
        consumerProperties.setConnectionString(CONNECTION_STRING);
        assertEquals("testeh", consumerProperties.getEventHubName());
    }

    @Test
    void getEventHubNameWhenNamespaceIsNotNull() {
        consumerProperties.setEventHubName("test");
        assertEquals("test", consumerProperties.getEventHubName());
    }

    @Test
    void getEventHubNameReturnNullWhenNamespaceAndConnectionStringAreNull() {
        assertNull(consumerProperties.getEventHubName());
    }
}
