// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.servicebus.properties;

import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.management.AzureEnvironment;
import com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider;
import org.junit.jupiter.api.Test;

import static com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider.CloudType.AZURE_CHINA;
import static com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider.CloudType.AZURE_US_GOVERNMENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AzureServiceBusPropertiesTest {

    @Test
    void defaultAmqpTransportTypeIsNull() {
        AzureServiceBusProperties serviceBusProperties = new AzureServiceBusProperties();

        AzureServiceBusProperties.Producer producer = serviceBusProperties.buildProducerProperties();
        AzureServiceBusProperties.Consumer consumer = serviceBusProperties.buildConsumerProperties();
        AzureServiceBusProperties.Processor processor = serviceBusProperties.buildProcessorProperties();

        assertNull(serviceBusProperties.getClient().getTransportType());
        assertNull(producer.getClient().getTransportType());
        assertNull(consumer.getClient().getTransportType());
        assertNull(processor.getClient().getTransportType());
    }

    @Test
    void defaultProfileCloudTypeIsAzure() {
        AzureServiceBusProperties serviceBusProperties = new AzureServiceBusProperties();

        AzureServiceBusProperties.Producer producer = serviceBusProperties.buildProducerProperties();
        AzureServiceBusProperties.Consumer consumer = serviceBusProperties.buildConsumerProperties();
        AzureServiceBusProperties.Processor processor = serviceBusProperties.buildProcessorProperties();

        assertNull(serviceBusProperties.getProfile().getCloudType());
        assertNull(producer.getProfile().getCloudType());
        assertNull(consumer.getProfile().getCloudType());
        assertNull(processor.getProfile().getCloudType());
    }

    @Test
    void defaultDomainNameIsAzure() {
        AzureServiceBusProperties serviceBusProperties = new AzureServiceBusProperties();

        AzureServiceBusProperties.Producer producer = serviceBusProperties.buildProducerProperties();
        AzureServiceBusProperties.Consumer consumer = serviceBusProperties.buildConsumerProperties();
        AzureServiceBusProperties.Processor processor = serviceBusProperties.buildProcessorProperties();

        assertEquals("servicebus.windows.net", serviceBusProperties.getDomainName());
        assertEquals("servicebus.windows.net", producer.getDomainName());
        assertEquals("servicebus.windows.net", consumer.getDomainName());
        assertEquals("servicebus.windows.net", processor.getDomainName());
    }

    @Test
    void childrenWillInheritParent() {
        AzureServiceBusProperties serviceBusProperties = new AzureServiceBusProperties();

        serviceBusProperties.getClient().setTransportType(AmqpTransportType.AMQP_WEB_SOCKETS);
        serviceBusProperties.getProfile().setCloudType(AZURE_US_GOVERNMENT);

        AzureServiceBusProperties.Producer producer = serviceBusProperties.buildProducerProperties();
        AzureServiceBusProperties.Consumer consumer = serviceBusProperties.buildConsumerProperties();
        AzureServiceBusProperties.Processor processor = serviceBusProperties.buildProcessorProperties();

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
        AzureServiceBusProperties serviceBusProperties = new AzureServiceBusProperties();
        serviceBusProperties.getProfile().setCloudType(AZURE_US_GOVERNMENT);
        serviceBusProperties.setDomainName("parent-domain");
        serviceBusProperties.getClient().setTransportType(AmqpTransportType.AMQP);

        AzureServiceBusProperties.Producer producer = serviceBusProperties.buildProducerProperties();
        assertEquals(AZURE_US_GOVERNMENT, producer.getProfile().getCloudType());
        assertEquals(AzureEnvironment.AZURE_US_GOVERNMENT.getActiveDirectoryEndpoint(),
            producer.getProfile().getEnvironment().getActiveDirectoryEndpoint());
        assertEquals("parent-domain", producer.getDomainName());
        assertEquals(AmqpTransportType.AMQP, producer.getClient().getTransportType());
    }

    @Test
    void buildProducerPropertiesUseChild() {
        AzureServiceBusProperties serviceBusProperties = new AzureServiceBusProperties();
        serviceBusProperties.getProfile().setCloudType(AZURE_US_GOVERNMENT);
        serviceBusProperties.setDomainName("parent-domain");
        serviceBusProperties.getClient().setTransportType(AmqpTransportType.AMQP);

        AzureServiceBusProperties.Producer producerProperties = serviceBusProperties.getProducer();
        producerProperties.setDomainName("child-domain");
        producerProperties.getProfile().setCloudType(AZURE_CHINA);
        producerProperties.getClient().setTransportType(AmqpTransportType.AMQP_WEB_SOCKETS);

        AzureServiceBusProperties.Producer producer = serviceBusProperties.buildProducerProperties();
        assertEquals(AZURE_CHINA, producer.getProfile().getCloudType());
        assertEquals(AzureEnvironment.AZURE_CHINA.getActiveDirectoryEndpoint(),
            producer.getProfile().getEnvironment().getActiveDirectoryEndpoint());
        assertEquals("child-domain", producer.getDomainName());
        assertEquals(AmqpTransportType.AMQP_WEB_SOCKETS, producer.getClient().getTransportType());
    }

    @Test
    void buildConsumerPropertiesUseParent() {
        AzureServiceBusProperties serviceBusProperties = new AzureServiceBusProperties();
        serviceBusProperties.getProfile().setCloudType(AZURE_US_GOVERNMENT);
        serviceBusProperties.setDomainName("parent-domain");
        serviceBusProperties.getClient().setTransportType(AmqpTransportType.AMQP);

        AzureServiceBusProperties.Consumer consumer = serviceBusProperties.buildConsumerProperties();
        assertEquals(AZURE_US_GOVERNMENT, consumer.getProfile().getCloudType());
        assertEquals(AzureEnvironment.AZURE_US_GOVERNMENT.getActiveDirectoryEndpoint(),
            consumer.getProfile().getEnvironment().getActiveDirectoryEndpoint());
        assertEquals("parent-domain", consumer.getDomainName());
        assertEquals(AmqpTransportType.AMQP, consumer.getClient().getTransportType());
    }

    @Test
    void buildConsumerPropertiesUseChild() {
        AzureServiceBusProperties serviceBusProperties = new AzureServiceBusProperties();
        serviceBusProperties.getProfile().setCloudType(AZURE_US_GOVERNMENT);
        serviceBusProperties.setDomainName("parent-domain");
        serviceBusProperties.getClient().setTransportType(AmqpTransportType.AMQP);

        AzureServiceBusProperties.Consumer consumerProperties = serviceBusProperties.getConsumer();
        consumerProperties.setDomainName("child-domain");
        consumerProperties.getProfile().setCloudType(AZURE_CHINA);
        consumerProperties.getClient().setTransportType(AmqpTransportType.AMQP_WEB_SOCKETS);

        AzureServiceBusProperties.Consumer consumer = serviceBusProperties.buildConsumerProperties();
        assertEquals(AzureProfileOptionsProvider.CloudType.AZURE_CHINA, consumer.getProfile().getCloudType());
        assertEquals(AzureEnvironment.AZURE_CHINA.getActiveDirectoryEndpoint(),
            consumer.getProfile().getEnvironment().getActiveDirectoryEndpoint());
        assertEquals("child-domain", consumer.getDomainName());
        assertEquals(AmqpTransportType.AMQP_WEB_SOCKETS, consumer.getClient().getTransportType());
    }

    @Test
    void buildProcessorPropertiesUseParent() {
        AzureServiceBusProperties serviceBusProperties = new AzureServiceBusProperties();
        serviceBusProperties.getProfile().setCloudType(AZURE_US_GOVERNMENT);
        serviceBusProperties.setDomainName("parent-domain");
        serviceBusProperties.getClient().setTransportType(AmqpTransportType.AMQP);

        AzureServiceBusProperties.Processor processor = serviceBusProperties.buildProcessorProperties();
        assertEquals(AZURE_US_GOVERNMENT, processor.getProfile().getCloudType());
        assertEquals(AzureEnvironment.AZURE_US_GOVERNMENT.getActiveDirectoryEndpoint(),
            processor.getProfile().getEnvironment().getActiveDirectoryEndpoint());
        assertEquals("parent-domain", processor.getDomainName());
    }

    @Test
    void buildProcessorPropertiesUseChild() {
        AzureServiceBusProperties serviceBusProperties = new AzureServiceBusProperties();
        serviceBusProperties.getProfile().setCloudType(AZURE_US_GOVERNMENT);
        serviceBusProperties.setDomainName("parent-domain");
        serviceBusProperties.getClient().setTransportType(AmqpTransportType.AMQP);

        AzureServiceBusProperties.Processor processorProperties = serviceBusProperties.getProcessor();
        processorProperties.getProfile().setCloudType(AZURE_CHINA);
        processorProperties.setDomainName("child-domain");
        processorProperties.getClient().setTransportType(AmqpTransportType.AMQP_WEB_SOCKETS);

        AzureServiceBusProperties.Processor processor = serviceBusProperties.buildProcessorProperties();
        assertEquals(AZURE_CHINA, processor.getProfile().getCloudType());
        assertEquals(AzureEnvironment.AZURE_CHINA.getActiveDirectoryEndpoint(),
            processor.getProfile().getEnvironment().getActiveDirectoryEndpoint());
        assertEquals("child-domain", processor.getDomainName());
        assertEquals(AmqpTransportType.AMQP_WEB_SOCKETS, processor.getClient().getTransportType());
    }
}
