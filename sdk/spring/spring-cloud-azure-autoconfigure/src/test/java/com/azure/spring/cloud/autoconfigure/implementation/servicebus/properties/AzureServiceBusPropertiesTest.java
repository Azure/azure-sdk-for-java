// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.servicebus.properties;

import com.azure.core.management.AzureEnvironment;
import org.junit.jupiter.api.Test;

import static com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider.CloudType.AZURE;
import static com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider.CloudType.AZURE_CHINA;
import static com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider.CloudType.AZURE_US_GOVERNMENT;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AzureServiceBusPropertiesTest {

    @Test
    void childrenWillInheritParent() {
        AzureServiceBusProperties serviceBusProperties = new AzureServiceBusProperties();

        AzureServiceBusProperties.Producer producer = serviceBusProperties.buildProducerProperties();
        AzureServiceBusProperties.Consumer consumer = serviceBusProperties.buildConsumerProperties();
        AzureServiceBusProperties.Processor processor = serviceBusProperties.buildProcessorProperties();

        assertEquals(AZURE, producer.getProfile().getCloudType());
        assertEquals(AzureEnvironment.AZURE.getActiveDirectoryEndpoint(),
            producer.getProfile().getEnvironment().getActiveDirectoryEndpoint());

        assertEquals(AZURE, consumer.getProfile().getCloudType());
        assertEquals(AzureEnvironment.AZURE.getActiveDirectoryEndpoint(),
            consumer.getProfile().getEnvironment().getActiveDirectoryEndpoint());

        assertEquals(AZURE, processor.getProfile().getCloudType());
        assertEquals(AzureEnvironment.AZURE.getActiveDirectoryEndpoint(),
            processor.getProfile().getEnvironment().getActiveDirectoryEndpoint());
    }

    @Test
    void buildProducerPropertiesUseParent() {
        AzureServiceBusProperties serviceBusProperties = new AzureServiceBusProperties();
        serviceBusProperties.getProfile().setCloudType(AZURE_US_GOVERNMENT);
        serviceBusProperties.setDomainName("parent-domain");

        AzureServiceBusProperties.Producer producer = serviceBusProperties.buildProducerProperties();
        assertEquals(AZURE_US_GOVERNMENT, producer.getProfile().getCloudType());
        assertEquals(AzureEnvironment.AZURE_US_GOVERNMENT.getActiveDirectoryEndpoint(),
            producer.getProfile().getEnvironment().getActiveDirectoryEndpoint());
        assertEquals("parent-domain", producer.getDomainName());
    }

    @Test
    void buildProducerPropertiesUseChild() {
        AzureServiceBusProperties serviceBusProperties = new AzureServiceBusProperties();
        serviceBusProperties.getProfile().setCloudType(AZURE_US_GOVERNMENT);
        serviceBusProperties.setDomainName("parent-domain");

        AzureServiceBusProperties.Producer producerProperties = serviceBusProperties.getProducer();
        producerProperties.setDomainName("child-domain");
        producerProperties.getProfile().setCloudType(AZURE_CHINA);

        AzureServiceBusProperties.Producer producer = serviceBusProperties.buildProducerProperties();
        assertEquals(AZURE_CHINA, producer.getProfile().getCloudType());
        assertEquals(AzureEnvironment.AZURE_CHINA.getActiveDirectoryEndpoint(),
            producer.getProfile().getEnvironment().getActiveDirectoryEndpoint());
        assertEquals("child-domain", producer.getDomainName());
    }

    @Test
    void buildConsumerPropertiesUseParent() {
        AzureServiceBusProperties serviceBusProperties = new AzureServiceBusProperties();
        serviceBusProperties.getProfile().setCloudType(AZURE_US_GOVERNMENT);
        serviceBusProperties.setDomainName("parent-domain");

        AzureServiceBusProperties.Consumer consumer = serviceBusProperties.buildConsumerProperties();
        assertEquals(AZURE_US_GOVERNMENT, consumer.getProfile().getCloudType());
        assertEquals(AzureEnvironment.AZURE_US_GOVERNMENT.getActiveDirectoryEndpoint(),
            consumer.getProfile().getEnvironment().getActiveDirectoryEndpoint());
        assertEquals("parent-domain", consumer.getDomainName());
    }

    @Test
    void buildConsumerPropertiesUseChild() {
        AzureServiceBusProperties serviceBusProperties = new AzureServiceBusProperties();
        serviceBusProperties.getProfile().setCloudType(AZURE_US_GOVERNMENT);
        serviceBusProperties.setDomainName("parent-domain");

        AzureServiceBusProperties.Consumer consumerProperties = serviceBusProperties.getConsumer();
        consumerProperties.setDomainName("child-domain");
        consumerProperties.getProfile().setCloudType(AZURE_CHINA);

        AzureServiceBusProperties.Consumer consumer = serviceBusProperties.buildConsumerProperties();
        assertEquals(AZURE_CHINA, consumer.getProfile().getCloudType());
        assertEquals(AzureEnvironment.AZURE_CHINA.getActiveDirectoryEndpoint(),
            consumer.getProfile().getEnvironment().getActiveDirectoryEndpoint());
        assertEquals("child-domain", consumer.getDomainName());
    }

    @Test
    void buildProcessorProperties() {
        AzureServiceBusProperties serviceBusProperties = new AzureServiceBusProperties();
        serviceBusProperties.getProfile().setCloudType(AZURE_US_GOVERNMENT);
        serviceBusProperties.setDomainName("parent-domain");

        AzureServiceBusProperties.Processor propertiesProcessor = serviceBusProperties.getProcessor();
        propertiesProcessor.setDomainName("child-domain");
        propertiesProcessor.getProfile().setCloudType(AZURE_CHINA);

        AzureServiceBusProperties.Processor processor = serviceBusProperties.buildProcessorProperties();
        assertEquals(AZURE_CHINA, processor.getProfile().getCloudType());
        assertEquals(AzureEnvironment.AZURE_CHINA.getActiveDirectoryEndpoint(),
            processor.getProfile().getEnvironment().getActiveDirectoryEndpoint());
        assertEquals("child-domain", processor.getDomainName());
    }
}
