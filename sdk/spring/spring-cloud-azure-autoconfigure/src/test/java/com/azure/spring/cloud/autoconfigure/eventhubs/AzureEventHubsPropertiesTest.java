// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.eventhubs;

import com.azure.spring.cloud.autoconfigure.implementation.eventhubs.properties.AzureEventHubsProperties;
import org.junit.jupiter.api.Test;

import static com.azure.spring.cloud.service.implementation.core.PropertiesValidator.ILLEGAL_SYMBOL_ERROR;
import static com.azure.spring.cloud.service.implementation.core.PropertiesValidator.LENGTH_ERROR;
import static com.azure.spring.cloud.service.implementation.core.PropertiesValidator.START_SYMBOL_ERROR;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AzureEventHubsPropertiesTest {
    @Test
    public void testNamespaceIllegal() {
        AzureEventHubsProperties azureEventHubsProperties = new AzureEventHubsProperties();
        azureEventHubsProperties.setNamespace("a");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            azureEventHubsProperties::afterPropertiesSet);
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(LENGTH_ERROR));
    }

    @Test
    public void testProducerNamespaceIllegal() {
        AzureEventHubsProperties azureEventHubsProperties = new AzureEventHubsProperties();
        azureEventHubsProperties.getProducer().setNamespace(new String(new char[51]).replace("\0", "a"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            azureEventHubsProperties::afterPropertiesSet);
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(LENGTH_ERROR));
    }

    @Test
    public void testConsumerNamespaceIllegal() {
        AzureEventHubsProperties azureEventHubsProperties = new AzureEventHubsProperties();
        azureEventHubsProperties.getConsumer().setNamespace("test+test");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            azureEventHubsProperties::afterPropertiesSet);
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(ILLEGAL_SYMBOL_ERROR));
    }

    @Test
    public void testProcessorNamespaceIllegal() {
        AzureEventHubsProperties azureEventHubsProperties = new AzureEventHubsProperties();
        azureEventHubsProperties.getConsumer().setNamespace("1testtest");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            azureEventHubsProperties::afterPropertiesSet);
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(START_SYMBOL_ERROR));
    }

    @Test
    public void testNullNamespaceShouldPass() {
        AzureEventHubsProperties azureEventHubsProperties = new AzureEventHubsProperties();

        assertDoesNotThrow(azureEventHubsProperties::afterPropertiesSet);
    }
}
