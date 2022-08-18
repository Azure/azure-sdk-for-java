// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.eventhubs.properties;

import com.azure.core.management.AzureEnvironment;
import com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider;
import org.junit.jupiter.api.Test;

import static com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider.CloudType.AZURE;
import static com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider.CloudType.AZURE_CHINA;
import static com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider.CloudType.AZURE_US_GOVERNMENT;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AzureEventHubsPropertiesTest {

    @Test
    void childrenWillInheritParent() {
        AzureEventHubsProperties eventHubsProperties = new AzureEventHubsProperties();

        AzureEventHubsProperties.Producer producer = eventHubsProperties.buildProducerProperties();
        AzureEventHubsProperties.Consumer consumer = eventHubsProperties.buildConsumerProperties();
        AzureEventHubsProperties.Processor processor = eventHubsProperties.buildProcessorProperties();

        assertEquals(AZURE, producer.getProfile().getCloudType());
        assertEquals(AzureEnvironment.AZURE.getActiveDirectoryEndpoint(),
            producer.getProfile().getEnvironment().getActiveDirectoryEndpoint());

        assertEquals(AZURE, consumer.getProfile().getCloudType());
        assertEquals(AzureEnvironment.AZURE.getActiveDirectoryEndpoint(),
            consumer.getProfile().getEnvironment().getActiveDirectoryEndpoint());

        assertEquals(AZURE, processor.getProfile().getCloudType());
        assertEquals(AzureEnvironment.AZURE.getActiveDirectoryEndpoint(),
            processor.getProfile().getEnvironment().getActiveDirectoryEndpoint());

        assertEquals(AZURE, processor.getCheckpointStore().getProfile().getCloudType());
        assertEquals(AzureEnvironment.AZURE.getActiveDirectoryEndpoint(),
            processor.getCheckpointStore().getProfile().getEnvironment().getActiveDirectoryEndpoint());
    }

    @Test
    void buildProducerPropertiesUseParent() {
        AzureEventHubsProperties eventHubsProperties = new AzureEventHubsProperties();
        eventHubsProperties.getProfile().setCloudType(AZURE_US_GOVERNMENT);
        eventHubsProperties.setDomainName("parent-domain");

        AzureEventHubsProperties.Producer producer = eventHubsProperties.buildProducerProperties();
        assertEquals(AZURE_US_GOVERNMENT, producer.getProfile().getCloudType());
        assertEquals(AzureEnvironment.AZURE_US_GOVERNMENT.getActiveDirectoryEndpoint(),
            producer.getProfile().getEnvironment().getActiveDirectoryEndpoint());
        assertEquals("parent-domain", producer.getDomainName());
    }

    @Test
    void buildProducerPropertiesUseChild() {
        AzureEventHubsProperties eventHubsProperties = new AzureEventHubsProperties();
        eventHubsProperties.getProfile().setCloudType(AZURE_US_GOVERNMENT);
        eventHubsProperties.setDomainName("parent-domain");

        AzureEventHubsProperties.Producer producerProperties = eventHubsProperties.getProducer();
        producerProperties.setDomainName("child-domain");
        producerProperties.getProfile().setCloudType(AZURE_CHINA);

        AzureEventHubsProperties.Producer producer = eventHubsProperties.buildProducerProperties();
        assertEquals(AZURE_CHINA, producer.getProfile().getCloudType());
        assertEquals(AzureEnvironment.AZURE_CHINA.getActiveDirectoryEndpoint(),
            producer.getProfile().getEnvironment().getActiveDirectoryEndpoint());
        assertEquals("child-domain", producer.getDomainName());
    }

    @Test
    void buildConsumerPropertiesUseParent() {
        AzureEventHubsProperties eventHubsProperties = new AzureEventHubsProperties();
        eventHubsProperties.getProfile().setCloudType(AZURE_US_GOVERNMENT);
        eventHubsProperties.setDomainName("parent-domain");

        AzureEventHubsProperties.Consumer consumer = eventHubsProperties.buildConsumerProperties();
        assertEquals(AZURE_US_GOVERNMENT, consumer.getProfile().getCloudType());
        assertEquals(AzureEnvironment.AZURE_US_GOVERNMENT.getActiveDirectoryEndpoint(),
            consumer.getProfile().getEnvironment().getActiveDirectoryEndpoint());
        assertEquals("parent-domain", consumer.getDomainName());
    }

    @Test
    void buildConsumerPropertiesUseChild() {
        AzureEventHubsProperties eventHubsProperties = new AzureEventHubsProperties();
        eventHubsProperties.getProfile().setCloudType(AZURE_US_GOVERNMENT);
        eventHubsProperties.setDomainName("parent-domain");

        AzureEventHubsProperties.Consumer consumerProperties = eventHubsProperties.getConsumer();
        consumerProperties.setDomainName("child-domain");
        consumerProperties.getProfile().setCloudType(AZURE_CHINA);

        AzureEventHubsProperties.Consumer consumer = eventHubsProperties.buildConsumerProperties();
        assertEquals(AzureProfileOptionsProvider.CloudType.AZURE_CHINA, consumer.getProfile().getCloudType());
        assertEquals(AzureEnvironment.AZURE_CHINA.getActiveDirectoryEndpoint(),
            consumer.getProfile().getEnvironment().getActiveDirectoryEndpoint());
        assertEquals("child-domain", consumer.getDomainName());
    }

    @Test
    void buildProcessorPropertiesUseParent() {
        AzureEventHubsProperties eventHubsProperties = new AzureEventHubsProperties();
        eventHubsProperties.getProfile().setCloudType(AZURE_US_GOVERNMENT);
        eventHubsProperties.setDomainName("parent-domain");

        AzureEventHubsProperties.Processor processor = eventHubsProperties.buildProcessorProperties();
        assertEquals(AZURE_US_GOVERNMENT, processor.getProfile().getCloudType());
        assertEquals(AzureEnvironment.AZURE_US_GOVERNMENT.getActiveDirectoryEndpoint(),
            processor.getProfile().getEnvironment().getActiveDirectoryEndpoint());
        assertEquals("parent-domain", processor.getDomainName());

        assertEquals(AZURE_US_GOVERNMENT, processor.getCheckpointStore().getProfile().getCloudType());
        assertEquals(AzureEnvironment.AZURE_US_GOVERNMENT.getActiveDirectoryEndpoint(),
            processor.getCheckpointStore().getProfile().getEnvironment().getActiveDirectoryEndpoint());
    }

    @Test
    void buildProcessorPropertiesUseChild() {
        AzureEventHubsProperties eventHubsProperties = new AzureEventHubsProperties();
        eventHubsProperties.getProfile().setCloudType(AZURE_US_GOVERNMENT);
        eventHubsProperties.setDomainName("parent-domain");

        AzureEventHubsProperties.Processor processorProperties = eventHubsProperties.getProcessor();
        processorProperties.getProfile().setCloudType(AZURE_CHINA);
        processorProperties.setDomainName("child-domain");

        AzureEventHubsProperties.Processor processor = eventHubsProperties.buildProcessorProperties();
        assertEquals(AZURE_CHINA, processor.getProfile().getCloudType());
        assertEquals(AzureEnvironment.AZURE_CHINA.getActiveDirectoryEndpoint(),
            processor.getProfile().getEnvironment().getActiveDirectoryEndpoint());
        assertEquals("child-domain", processor.getDomainName());
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
