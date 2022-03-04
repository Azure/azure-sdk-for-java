// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.spring.cloud.autoconfigure.implementation.servicebus.properties.AzureServiceBusProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static com.azure.spring.cloud.service.implementation.core.PropertiesValidator.ILLEGAL_SYMBOL_ERROR;
import static com.azure.spring.cloud.service.implementation.core.PropertiesValidator.LENGTH_ERROR;
import static com.azure.spring.cloud.service.implementation.core.PropertiesValidator.START_SYMBOL_ERROR;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(OutputCaptureExtension.class)
public class AzureServiceBusPropertiesTest {

    @Test
    public void testNamespaceIllegal(CapturedOutput output) throws Exception {
        AzureServiceBusProperties azureServiceBusProperties = new AzureServiceBusProperties();
        azureServiceBusProperties.setNamespace("a");

        azureServiceBusProperties.afterPropertiesSet();
        assertThat(output).contains(LENGTH_ERROR);
    }

    @Test
    public void testProducerNamespaceIllegal(CapturedOutput output) throws Exception {
        AzureServiceBusProperties azureServiceBusProperties = new AzureServiceBusProperties();
        azureServiceBusProperties.getProducer().setNamespace(new String(new char[51]).replace("\0", "a"));

        azureServiceBusProperties.afterPropertiesSet();
        assertThat(output).contains(LENGTH_ERROR);
    }

    @Test
    public void testConsumerNamespaceIllegal(CapturedOutput output) throws Exception {
        AzureServiceBusProperties azureServiceBusProperties = new AzureServiceBusProperties();
        azureServiceBusProperties.getConsumer().setNamespace("test+test");

        azureServiceBusProperties.afterPropertiesSet();
        assertThat(output).contains(ILLEGAL_SYMBOL_ERROR);
    }

    @Test
    public void testProcessorNamespaceIllegal(CapturedOutput output) throws Exception {
        AzureServiceBusProperties azureServiceBusProperties = new AzureServiceBusProperties();
        azureServiceBusProperties.getConsumer().setNamespace("1testtest");

        azureServiceBusProperties.afterPropertiesSet();
        assertThat(output).contains(START_SYMBOL_ERROR);
    }

}
