// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.stream.binder.servicebus.core.properties;

import com.azure.spring.cloud.service.implementation.core.PropertiesValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.util.Collections;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(OutputCaptureExtension.class)
public class ServiceBusExtendedBindingPropertiesTest {

    private ServiceBusExtendedBindingProperties extendedBindingProperties;
    private ServiceBusBindingProperties bindingProperties;

    @BeforeEach
    void beforeEach() {
        bindingProperties = new ServiceBusBindingProperties();
        Map<String, ServiceBusBindingProperties> binding =
            Collections.singletonMap("test", bindingProperties);
        extendedBindingProperties = new ServiceBusExtendedBindingProperties();
        extendedBindingProperties.setBindings(binding);
    }

    @Test
    void testConsumerNamespaceIllegal(CapturedOutput output) throws Exception {
        ServiceBusConsumerProperties consumerProperties = new ServiceBusConsumerProperties();
        consumerProperties.setNamespace("a");
        bindingProperties.setConsumer(consumerProperties);

        extendedBindingProperties.afterPropertiesSet();
        assertThat(output).contains(PropertiesValidator.LENGTH_ERROR);
    }

    @Test
    void testProducerNamespaceIllegal(CapturedOutput output) throws Exception {
        ServiceBusProducerProperties producerProperties = new ServiceBusProducerProperties();
        producerProperties.setNamespace("test_test");
        bindingProperties.setProducer(producerProperties);

        extendedBindingProperties.afterPropertiesSet();
        assertThat(output).contains(PropertiesValidator.ILLEGAL_SYMBOL_ERROR);
    }
}
