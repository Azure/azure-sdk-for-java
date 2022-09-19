// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.servicebus.properties;

import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.management.AzureEnvironment;
import com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider;
import org.junit.jupiter.api.Test;

import static com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider.CloudType.AZURE;
import static com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider.CloudType.AZURE_CHINA;
import static com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider.CloudType.AZURE_US_GOVERNMENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AzureServiceBusPropertiesTest {

    @Test
    void defaultAmqpTransportTypeIsNull() {
        AzureServiceBusProperties eventHubsProperties = new AzureServiceBusProperties();

        AzureServiceBusProperties.Producer producer = eventHubsProperties.buildProducerProperties();
        AzureServiceBusProperties.Consumer consumer = eventHubsProperties.buildConsumerProperties();
        AzureServiceBusProperties.Processor processor = eventHubsProperties.buildProcessorProperties();

        assertNull(eventHubsProperties.getClient().getTransportType());
        assertNull(producer.getClient().getTransportType());
        assertNull(consumer.getClient().getTransportType());
        assertNull(processor.getClient().getTransportType());
    }

    @Test
    void defaultProfileCloudTypeIsAzure() {
        AzureServiceBusProperties eventHubsProperties = new AzureServiceBusProperties();

        AzureServiceBusProperties.Producer producer = eventHubsProperties.buildProducerProperties();
        AzureServiceBusProperties.Consumer consumer = eventHubsProperties.buildConsumerProperties();
        AzureServiceBusProperties.Processor processor = eventHubsProperties.buildProcessorProperties();

        assertEquals(AZURE, eventHubsProperties.getProfile().getCloudType());
        assertEquals(AZURE, producer.getProfile().getCloudType());
        assertEquals(AZURE, consumer.getProfile().getCloudType());
        assertEquals(AZURE, processor.getProfile().getCloudType());

        assertEquals(AzureEnvironment.AZURE.getActiveDirectoryEndpoint(), eventHubsProperties.getProfile().getEnvironment().getActiveDirectoryEndpoint());
        assertEquals(AzureEnvironment.AZURE.getActiveDirectoryEndpoint(), producer.getProfile().getEnvironment().getActiveDirectoryEndpoint());
        assertEquals(AzureEnvironment.AZURE.getActiveDirectoryEndpoint(), consumer.getProfile().getEnvironment().getActiveDirectoryEndpoint());
        assertEquals(AzureEnvironment.AZURE.getActiveDirectoryEndpoint(), processor.getProfile().getEnvironment().getActiveDirectoryEndpoint());
    }

    @Test
    void defaultDomainNameIsAzure() {
        AzureServiceBusProperties eventHubsProperties = new AzureServiceBusProperties();

        AzureServiceBusProperties.Producer producer = eventHubsProperties.buildProducerProperties();
        AzureServiceBusProperties.Consumer consumer = eventHubsProperties.buildConsumerProperties();
        AzureServiceBusProperties.Processor processor = eventHubsProperties.buildProcessorProperties();

        assertEquals("servicebus.windows.net", eventHubsProperties.getDomainName());
        assertEquals("servicebus.windows.net", producer.getDomainName());
        assertEquals("servicebus.windows.net", consumer.getDomainName());
        assertEquals("servicebus.windows.net", processor.getDomainName());
    }

    @Test
    void childrenWillInheritParent() {
        AzureServiceBusProperties eventHubsProperties = new AzureServiceBusProperties();

        eventHubsProperties.getClient().setTransportType(AmqpTransportType.AMQP_WEB_SOCKETS);
        eventHubsProperties.getProfile().setCloudType(AZURE_US_GOVERNMENT);

        AzureServiceBusProperties.Producer producer = eventHubsProperties.buildProducerProperties();
        AzureServiceBusProperties.Consumer consumer = eventHubsProperties.buildConsumerProperties();
        AzureServiceBusProperties.Processor processor = eventHubsProperties.buildProcessorProperties();

        assertEquals(AZURE_US_GOVERNMENT, producer.getProfile().getCloudType());
        assertEquals(AzureEnvironment.AZURE_US_GOVERNMENT.getActiveDirectoryEndpoint(),
            producer.getProfile().getEnvironment().getActiveDirectoryEndpoint());
        assertEquals(AmqpTransportType.AMQP_WEB_SOCKETS, producer.getClient().getTransportType());

        assertEquals(AZURE_US_GOVERNMENT, consumer.getProfile().getCloudType());
        assertEquals(AzureEnvironment.AZURE_US_GOVERNMENT.getActiveDirectoryEndpoint(),
            consumer.getProfile().getEnvironment().getActiveDirectoryEndpoint());
        assertEquals(AmqpTransportType.AMQP_WEB_SOCKETS, consumer.getClient().getTransportType());

        assertEquals(AZURE_US_GOVERNMENT, processor.getProfile().getCloudType());
        assertEquals(AzureEnvironment.AZURE_US_GOVERNMENT.getActiveDirectoryEndpoint(),
            processor.getProfile().getEnvironment().getActiveDirectoryEndpoint());
        assertEquals(AmqpTransportType.AMQP_WEB_SOCKETS, processor.getClient().getTransportType());
    }

    @Test
    void buildProducerPropertiesUseParent() {
        AzureServiceBusProperties eventHubsProperties = new AzureServiceBusProperties();
        eventHubsProperties.getProfile().setCloudType(AZURE_US_GOVERNMENT);
        eventHubsProperties.setDomainName("parent-domain");
        eventHubsProperties.getClient().setTransportType(AmqpTransportType.AMQP);

        AzureServiceBusProperties.Producer producer = eventHubsProperties.buildProducerProperties();
        assertEquals(AZURE_US_GOVERNMENT, producer.getProfile().getCloudType());
        assertEquals(AzureEnvironment.AZURE_US_GOVERNMENT.getActiveDirectoryEndpoint(),
            producer.getProfile().getEnvironment().getActiveDirectoryEndpoint());
        assertEquals("parent-domain", producer.getDomainName());
        assertEquals(AmqpTransportType.AMQP, producer.getClient().getTransportType());
    }

    @Test
    void buildProducerPropertiesUseChild() {
        AzureServiceBusProperties eventHubsProperties = new AzureServiceBusProperties();
        eventHubsProperties.getProfile().setCloudType(AZURE_US_GOVERNMENT);
        eventHubsProperties.setDomainName("parent-domain");
        eventHubsProperties.getClient().setTransportType(AmqpTransportType.AMQP);

        AzureServiceBusProperties.Producer producerProperties = eventHubsProperties.getProducer();
        producerProperties.setDomainName("child-domain");
        producerProperties.getProfile().setCloudType(AZURE_CHINA);
        producerProperties.getClient().setTransportType(AmqpTransportType.AMQP_WEB_SOCKETS);

        AzureServiceBusProperties.Producer producer = eventHubsProperties.buildProducerProperties();
        assertEquals(AZURE_CHINA, producer.getProfile().getCloudType());
        assertEquals(AzureEnvironment.AZURE_CHINA.getActiveDirectoryEndpoint(),
            producer.getProfile().getEnvironment().getActiveDirectoryEndpoint());
        assertEquals("child-domain", producer.getDomainName());
        assertEquals(AmqpTransportType.AMQP_WEB_SOCKETS, producer.getClient().getTransportType());
    }

    @Test
    void buildConsumerPropertiesUseParent() {
        AzureServiceBusProperties eventHubsProperties = new AzureServiceBusProperties();
        eventHubsProperties.getProfile().setCloudType(AZURE_US_GOVERNMENT);
        eventHubsProperties.setDomainName("parent-domain");
        eventHubsProperties.getClient().setTransportType(AmqpTransportType.AMQP);

        AzureServiceBusProperties.Consumer consumer = eventHubsProperties.buildConsumerProperties();
        assertEquals(AZURE_US_GOVERNMENT, consumer.getProfile().getCloudType());
        assertEquals(AzureEnvironment.AZURE_US_GOVERNMENT.getActiveDirectoryEndpoint(),
            consumer.getProfile().getEnvironment().getActiveDirectoryEndpoint());
        assertEquals("parent-domain", consumer.getDomainName());
        assertEquals(AmqpTransportType.AMQP, consumer.getClient().getTransportType());
    }

    @Test
    void buildConsumerPropertiesUseChild() {
        AzureServiceBusProperties eventHubsProperties = new AzureServiceBusProperties();
        eventHubsProperties.getProfile().setCloudType(AZURE_US_GOVERNMENT);
        eventHubsProperties.setDomainName("parent-domain");
        eventHubsProperties.getClient().setTransportType(AmqpTransportType.AMQP);

        AzureServiceBusProperties.Consumer consumerProperties = eventHubsProperties.getConsumer();
        consumerProperties.setDomainName("child-domain");
        consumerProperties.getProfile().setCloudType(AZURE_CHINA);
        consumerProperties.getClient().setTransportType(AmqpTransportType.AMQP_WEB_SOCKETS);

        AzureServiceBusProperties.Consumer consumer = eventHubsProperties.buildConsumerProperties();
        assertEquals(AzureProfileOptionsProvider.CloudType.AZURE_CHINA, consumer.getProfile().getCloudType());
        assertEquals(AzureEnvironment.AZURE_CHINA.getActiveDirectoryEndpoint(),
            consumer.getProfile().getEnvironment().getActiveDirectoryEndpoint());
        assertEquals("child-domain", consumer.getDomainName());
        assertEquals(AmqpTransportType.AMQP_WEB_SOCKETS, consumer.getClient().getTransportType());
    }

    @Test
    void buildProcessorPropertiesUseParent() {
        AzureServiceBusProperties eventHubsProperties = new AzureServiceBusProperties();
        eventHubsProperties.getProfile().setCloudType(AZURE_US_GOVERNMENT);
        eventHubsProperties.setDomainName("parent-domain");
        eventHubsProperties.getClient().setTransportType(AmqpTransportType.AMQP);

        AzureServiceBusProperties.Processor processor = eventHubsProperties.buildProcessorProperties();
        assertEquals(AZURE_US_GOVERNMENT, processor.getProfile().getCloudType());
        assertEquals(AzureEnvironment.AZURE_US_GOVERNMENT.getActiveDirectoryEndpoint(),
            processor.getProfile().getEnvironment().getActiveDirectoryEndpoint());
        assertEquals("parent-domain", processor.getDomainName());
    }

    @Test
    void buildProcessorPropertiesUseChild() {
        AzureServiceBusProperties eventHubsProperties = new AzureServiceBusProperties();
        eventHubsProperties.getProfile().setCloudType(AZURE_US_GOVERNMENT);
        eventHubsProperties.setDomainName("parent-domain");
        eventHubsProperties.getClient().setTransportType(AmqpTransportType.AMQP);

        AzureServiceBusProperties.Processor processorProperties = eventHubsProperties.getProcessor();
        processorProperties.getProfile().setCloudType(AZURE_CHINA);
        processorProperties.setDomainName("child-domain");
        processorProperties.getClient().setTransportType(AmqpTransportType.AMQP_WEB_SOCKETS);

        AzureServiceBusProperties.Processor processor = eventHubsProperties.buildProcessorProperties();
        assertEquals(AZURE_CHINA, processor.getProfile().getCloudType());
        assertEquals(AzureEnvironment.AZURE_CHINA.getActiveDirectoryEndpoint(),
            processor.getProfile().getEnvironment().getActiveDirectoryEndpoint());
        assertEquals("child-domain", processor.getDomainName());
        assertEquals(AmqpTransportType.AMQP_WEB_SOCKETS, processor.getClient().getTransportType());
    }
}
