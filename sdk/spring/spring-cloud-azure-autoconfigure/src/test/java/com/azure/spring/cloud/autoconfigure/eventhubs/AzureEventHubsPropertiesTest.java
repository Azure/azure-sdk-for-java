// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.eventhubs;

import com.azure.spring.cloud.autoconfigure.implementation.eventhubs.properties.AzureEventHubsProperties;
import com.azure.spring.cloud.service.implementation.core.PropertiesValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(OutputCaptureExtension.class)
public class AzureEventHubsPropertiesTest {
    @Test
    public void testNamespaceIllegal(CapturedOutput output) throws Exception {
        AzureEventHubsProperties azureEventHubsProperties = new AzureEventHubsProperties();
        azureEventHubsProperties.setNamespace("a");

        azureEventHubsProperties.afterPropertiesSet();
        assertThat(output).contains(PropertiesValidator.LENGTH_ERROR);
    }

    @Test
    public void testProducerNamespaceIllegal(CapturedOutput output) throws Exception {
        AzureEventHubsProperties azureEventHubsProperties = new AzureEventHubsProperties();
        azureEventHubsProperties.getProducer().setNamespace(new String(new char[51]).replace("\0", "a"));

        azureEventHubsProperties.afterPropertiesSet();
        assertThat(output).contains(PropertiesValidator.LENGTH_ERROR);
    }

    @Test
    public void testConsumerNamespaceIllegal(CapturedOutput output) throws Exception {
        AzureEventHubsProperties azureEventHubsProperties = new AzureEventHubsProperties();
        azureEventHubsProperties.getConsumer().setNamespace("test+test");

        azureEventHubsProperties.afterPropertiesSet();
        assertThat(output).contains(PropertiesValidator.ILLEGAL_SYMBOL_ERROR);
    }

    @Test
    public void testProcessorNamespaceIllegal(CapturedOutput output) throws Exception {
        AzureEventHubsProperties azureEventHubsProperties = new AzureEventHubsProperties();
        azureEventHubsProperties.getConsumer().setNamespace("1testtest");

        azureEventHubsProperties.afterPropertiesSet();
        assertThat(output).contains(PropertiesValidator.START_SYMBOL_ERROR);
    }

    @Test
    public void testNullNamespaceShouldPass(CapturedOutput output) throws Exception {
        AzureEventHubsProperties azureEventHubsProperties = new AzureEventHubsProperties();

        assertThat(output).doesNotContain("The Namespace");
    }
}
