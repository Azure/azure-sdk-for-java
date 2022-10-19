// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.eventhubs.properties;

import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.management.AzureEnvironment;
import com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider;
import org.junit.jupiter.api.Test;

import static com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider.CloudType.AZURE;
import static com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider.CloudType.AZURE_CHINA;
import static com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider.CloudType.AZURE_US_GOVERNMENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AzureEventHubsPropertiesTest {

    @Test
    void defaultAmqpTransportTypeIsNull() {
        AzureEventHubsProperties eventHubsProperties = new AzureEventHubsProperties();

        AzureEventHubsProperties.Producer producer = eventHubsProperties.buildProducerProperties();
        AzureEventHubsProperties.Consumer consumer = eventHubsProperties.buildConsumerProperties();
        AzureEventHubsProperties.Processor processor = eventHubsProperties.buildProcessorProperties();

        assertNull(eventHubsProperties.getClient().getTransportType());
        assertNull(producer.getClient().getTransportType());
        assertNull(consumer.getClient().getTransportType());
        assertNull(processor.getClient().getTransportType());
    }

    @Test
    void defaultProfileCloudTypeIsAzure() {
        AzureEventHubsProperties eventHubsProperties = new AzureEventHubsProperties();

        AzureEventHubsProperties.Producer producer = eventHubsProperties.buildProducerProperties();
        AzureEventHubsProperties.Consumer consumer = eventHubsProperties.buildConsumerProperties();
        AzureEventHubsProperties.Processor processor = eventHubsProperties.buildProcessorProperties();
        AzureEventHubsProperties.Processor.BlobCheckpointStore checkpointStore = processor.getCheckpointStore();

        assertNull(eventHubsProperties.getProfile().getCloudType());
        assertNull(producer.getProfile().getCloudType());
        assertNull(consumer.getProfile().getCloudType());
        assertNull(processor.getProfile().getCloudType());
        assertNull(checkpointStore.getProfile().getCloudType());
    }

    @Test
    void defaultDomainNameIsAzure() {
        AzureEventHubsProperties eventHubsProperties = new AzureEventHubsProperties();

        AzureEventHubsProperties.Producer producer = eventHubsProperties.buildProducerProperties();
        AzureEventHubsProperties.Consumer consumer = eventHubsProperties.buildConsumerProperties();
        AzureEventHubsProperties.Processor processor = eventHubsProperties.buildProcessorProperties();

        assertEquals("servicebus.windows.net", eventHubsProperties.getDomainName());
        assertEquals("servicebus.windows.net", producer.getDomainName());
        assertEquals("servicebus.windows.net", consumer.getDomainName());
        assertEquals("servicebus.windows.net", processor.getDomainName());
    }

    @Test
    void childrenWillInheritParent() {
        AzureEventHubsProperties eventHubsProperties = new AzureEventHubsProperties();

        eventHubsProperties.getClient().setTransportType(AmqpTransportType.AMQP_WEB_SOCKETS);
        eventHubsProperties.getProfile().setCloudType(AZURE_US_GOVERNMENT);

        AzureEventHubsProperties.Producer producer = eventHubsProperties.buildProducerProperties();
        AzureEventHubsProperties.Consumer consumer = eventHubsProperties.buildConsumerProperties();
        AzureEventHubsProperties.Processor processor = eventHubsProperties.buildProcessorProperties();
        AzureEventHubsProperties.Processor.BlobCheckpointStore checkpointStore = processor.getCheckpointStore();

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

        assertEquals(AZURE_US_GOVERNMENT, checkpointStore.getProfile().getCloudType());
        assertEquals(AzureEnvironment.AZURE_US_GOVERNMENT.getActiveDirectoryEndpoint(),
            checkpointStore.getProfile().getEnvironment().getActiveDirectoryEndpoint());
    }

    @Test
    void buildProducerPropertiesUseParent() {
        AzureEventHubsProperties eventHubsProperties = new AzureEventHubsProperties();
        eventHubsProperties.getProfile().setCloudType(AZURE_US_GOVERNMENT);
        eventHubsProperties.setDomainName("parent-domain");
        eventHubsProperties.getClient().setTransportType(AmqpTransportType.AMQP);

        AzureEventHubsProperties.Producer producer = eventHubsProperties.buildProducerProperties();
        assertEquals(AZURE_US_GOVERNMENT, producer.getProfile().getCloudType());
        assertEquals(AzureEnvironment.AZURE_US_GOVERNMENT.getActiveDirectoryEndpoint(),
            producer.getProfile().getEnvironment().getActiveDirectoryEndpoint());
        assertEquals("parent-domain", producer.getDomainName());
        assertEquals(AmqpTransportType.AMQP, producer.getClient().getTransportType());
    }

    @Test
    void buildProducerPropertiesUseChild() {
        AzureEventHubsProperties eventHubsProperties = new AzureEventHubsProperties();
        eventHubsProperties.getProfile().setCloudType(AZURE_US_GOVERNMENT);
        eventHubsProperties.setDomainName("parent-domain");
        eventHubsProperties.getClient().setTransportType(AmqpTransportType.AMQP);

        AzureEventHubsProperties.Producer producerProperties = eventHubsProperties.getProducer();
        producerProperties.setDomainName("child-domain");
        producerProperties.getProfile().setCloudType(AZURE_CHINA);
        producerProperties.getClient().setTransportType(AmqpTransportType.AMQP_WEB_SOCKETS);

        AzureEventHubsProperties.Producer producer = eventHubsProperties.buildProducerProperties();
        assertEquals(AZURE_CHINA, producer.getProfile().getCloudType());
        assertEquals(AzureEnvironment.AZURE_CHINA.getActiveDirectoryEndpoint(),
            producer.getProfile().getEnvironment().getActiveDirectoryEndpoint());
        assertEquals("child-domain", producer.getDomainName());
        assertEquals(AmqpTransportType.AMQP_WEB_SOCKETS, producer.getClient().getTransportType());
    }

    @Test
    void buildConsumerPropertiesUseParent() {
        AzureEventHubsProperties eventHubsProperties = new AzureEventHubsProperties();
        eventHubsProperties.getProfile().setCloudType(AZURE_US_GOVERNMENT);
        eventHubsProperties.setDomainName("parent-domain");
        eventHubsProperties.getClient().setTransportType(AmqpTransportType.AMQP);

        AzureEventHubsProperties.Consumer consumer = eventHubsProperties.buildConsumerProperties();
        assertEquals(AZURE_US_GOVERNMENT, consumer.getProfile().getCloudType());
        assertEquals(AzureEnvironment.AZURE_US_GOVERNMENT.getActiveDirectoryEndpoint(),
            consumer.getProfile().getEnvironment().getActiveDirectoryEndpoint());
        assertEquals("parent-domain", consumer.getDomainName());
        assertEquals(AmqpTransportType.AMQP, consumer.getClient().getTransportType());
    }

    @Test
    void buildConsumerPropertiesUseChild() {
        AzureEventHubsProperties eventHubsProperties = new AzureEventHubsProperties();
        eventHubsProperties.getProfile().setCloudType(AZURE_US_GOVERNMENT);
        eventHubsProperties.setDomainName("parent-domain");
        eventHubsProperties.getClient().setTransportType(AmqpTransportType.AMQP);

        AzureEventHubsProperties.Consumer consumerProperties = eventHubsProperties.getConsumer();
        consumerProperties.setDomainName("child-domain");
        consumerProperties.getProfile().setCloudType(AZURE_CHINA);
        consumerProperties.getClient().setTransportType(AmqpTransportType.AMQP_WEB_SOCKETS);

        AzureEventHubsProperties.Consumer consumer = eventHubsProperties.buildConsumerProperties();
        assertEquals(AzureProfileOptionsProvider.CloudType.AZURE_CHINA, consumer.getProfile().getCloudType());
        assertEquals(AzureEnvironment.AZURE_CHINA.getActiveDirectoryEndpoint(),
            consumer.getProfile().getEnvironment().getActiveDirectoryEndpoint());
        assertEquals("child-domain", consumer.getDomainName());
        assertEquals(AmqpTransportType.AMQP_WEB_SOCKETS, consumer.getClient().getTransportType());
    }

    @Test
    void buildProcessorPropertiesUseParent() {
        AzureEventHubsProperties eventHubsProperties = new AzureEventHubsProperties();
        eventHubsProperties.getProfile().setCloudType(AZURE_US_GOVERNMENT);
        eventHubsProperties.setDomainName("parent-domain");
        eventHubsProperties.getClient().setTransportType(AmqpTransportType.AMQP);

        AzureEventHubsProperties.Processor processor = eventHubsProperties.buildProcessorProperties();
        assertEquals(AZURE_US_GOVERNMENT, processor.getProfile().getCloudType());
        assertEquals(AzureEnvironment.AZURE_US_GOVERNMENT.getActiveDirectoryEndpoint(),
            processor.getProfile().getEnvironment().getActiveDirectoryEndpoint());
        assertEquals("parent-domain", processor.getDomainName());

        assertEquals(AZURE_US_GOVERNMENT, processor.getCheckpointStore().getProfile().getCloudType());
        assertEquals(AzureEnvironment.AZURE_US_GOVERNMENT.getActiveDirectoryEndpoint(),
            processor.getCheckpointStore().getProfile().getEnvironment().getActiveDirectoryEndpoint());
        assertEquals(AmqpTransportType.AMQP, processor.getClient().getTransportType());
    }

    @Test
    void buildProcessorPropertiesUseChild() {
        AzureEventHubsProperties eventHubsProperties = new AzureEventHubsProperties();
        eventHubsProperties.getProfile().setCloudType(AZURE_US_GOVERNMENT);
        eventHubsProperties.setDomainName("parent-domain");
        eventHubsProperties.getClient().setTransportType(AmqpTransportType.AMQP);

        AzureEventHubsProperties.Processor processorProperties = eventHubsProperties.getProcessor();
        processorProperties.getProfile().setCloudType(AZURE_CHINA);
        processorProperties.setDomainName("child-domain");
        processorProperties.getClient().setTransportType(AmqpTransportType.AMQP_WEB_SOCKETS);

        AzureEventHubsProperties.Processor processor = eventHubsProperties.buildProcessorProperties();
        assertEquals(AZURE_CHINA, processor.getProfile().getCloudType());
        assertEquals(AzureEnvironment.AZURE_CHINA.getActiveDirectoryEndpoint(),
            processor.getProfile().getEnvironment().getActiveDirectoryEndpoint());
        assertEquals("child-domain", processor.getDomainName());
        assertEquals(AmqpTransportType.AMQP_WEB_SOCKETS, processor.getClient().getTransportType());
    }

    @Test
    void buildCheckpointStorePropertiesUseParent() {
        AzureEventHubsProperties eventHubsProperties = new AzureEventHubsProperties();
        eventHubsProperties.getProfile().setCloudType(AZURE_US_GOVERNMENT);
        eventHubsProperties.setDomainName("parent-domain");

        AzureEventHubsProperties.Processor processor = eventHubsProperties.buildProcessorProperties();

        assertEquals(AZURE_US_GOVERNMENT, processor.getCheckpointStore().getProfile().getCloudType());
        assertEquals(AzureEnvironment.AZURE_US_GOVERNMENT.getActiveDirectoryEndpoint(),
            processor.getCheckpointStore().getProfile().getEnvironment().getActiveDirectoryEndpoint());
    }

    @Test
    void buildCheckpointStorePropertiesUseChild() {
        AzureEventHubsProperties eventHubsProperties = new AzureEventHubsProperties();
        eventHubsProperties.getProfile().setCloudType(AZURE_US_GOVERNMENT);
        eventHubsProperties.setDomainName("parent-domain");

        AzureEventHubsProperties.Processor processorProperties = eventHubsProperties.getProcessor();
        processorProperties.getProfile().setCloudType(AZURE_CHINA);
        processorProperties.setDomainName("child-domain");

        AzureEventHubsProperties.Processor processor = eventHubsProperties.buildProcessorProperties();

        assertEquals(AZURE_CHINA, processor.getCheckpointStore().getProfile().getCloudType());
        assertEquals(AzureEnvironment.AZURE_CHINA.getActiveDirectoryEndpoint(),
            processor.getCheckpointStore().getProfile().getEnvironment().getActiveDirectoryEndpoint());
    }

    @Test
    void buildCheckpointStorePropertiesUseCheckpointStore() {
        AzureEventHubsProperties eventHubsProperties = new AzureEventHubsProperties();
        eventHubsProperties.getProfile().setCloudType(AZURE_US_GOVERNMENT);
        eventHubsProperties.setDomainName("parent-domain");

        AzureEventHubsProperties.Processor processorProperties = eventHubsProperties.getProcessor();
        processorProperties.getProfile().setCloudType(AZURE_CHINA);
        processorProperties.setDomainName("child-domain");

        AzureEventHubsProperties.Processor.BlobCheckpointStore checkpointStore = processorProperties.getCheckpointStore();
        checkpointStore.getProfile().setCloudType(AZURE);

        AzureEventHubsProperties.Processor processor = eventHubsProperties.buildProcessorProperties();

        assertEquals(AZURE, processor.getCheckpointStore().getProfile().getCloudType());
        assertEquals(AzureEnvironment.AZURE.getActiveDirectoryEndpoint(),
            processor.getCheckpointStore().getProfile().getEnvironment().getActiveDirectoryEndpoint());
    }
}
