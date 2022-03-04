// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.spring.cloud.autoconfigure.implementation.servicebus.properties.AzureServiceBusProperties;
import org.junit.jupiter.api.Test;

import static com.azure.spring.cloud.service.implementation.core.PropertiesValidator.ILLEGAL_SYMBOL_ERROR;
import static com.azure.spring.cloud.service.implementation.core.PropertiesValidator.LENGTH_ERROR;
import static com.azure.spring.cloud.service.implementation.core.PropertiesValidator.START_SYMBOL_ERROR;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AzureServiceBusPropertiesTest {

    @Test
    public void testNamespaceIllegal() {
        AzureServiceBusProperties azureServiceBusProperties = new AzureServiceBusProperties();
        azureServiceBusProperties.setNamespace("a");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            azureServiceBusProperties::afterPropertiesSet);
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(LENGTH_ERROR));
    }

    @Test
    public void testProducerNamespaceIllegal() {
        AzureServiceBusProperties azureServiceBusProperties = new AzureServiceBusProperties();
        azureServiceBusProperties.getProducer().setNamespace(new String(new char[51]).replace("\0", "a"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            azureServiceBusProperties::afterPropertiesSet);
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(LENGTH_ERROR));
    }

    @Test
    public void testConsumerNamespaceIllegal() {
        AzureServiceBusProperties azureServiceBusProperties = new AzureServiceBusProperties();
        azureServiceBusProperties.getConsumer().setNamespace("test+test");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            azureServiceBusProperties::afterPropertiesSet);
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(ILLEGAL_SYMBOL_ERROR));
    }

    @Test
    public void testProcessorNamespaceIllegal() {
        AzureServiceBusProperties azureServiceBusProperties = new AzureServiceBusProperties();
        azureServiceBusProperties.getConsumer().setNamespace("1testtest");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            azureServiceBusProperties::afterPropertiesSet);
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(START_SYMBOL_ERROR));
    }

    @Test
    public void testNullNamespaceShouldPass() {
        AzureServiceBusProperties azureServiceBusProperties = new AzureServiceBusProperties();

        assertDoesNotThrow(azureServiceBusProperties::afterPropertiesSet);
    }

}
