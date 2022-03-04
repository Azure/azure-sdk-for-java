// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.eventhubs.core.properties;

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
class EventHubsExtendedBindingPropertiesTest {

    private EventHubsExtendedBindingProperties extendedBindingProperties;
    private EventHubsBindingProperties bindingProperties;

    @BeforeEach
    void beforeEach() {
        bindingProperties = new EventHubsBindingProperties();
        Map<String, EventHubsBindingProperties> binding =
            Collections.singletonMap("test", bindingProperties);
        extendedBindingProperties = new EventHubsExtendedBindingProperties();
        extendedBindingProperties.setBindings(binding);
    }

    @Test
    void testConsumerNamespaceIllegal(CapturedOutput output) throws Exception {
        EventHubsConsumerProperties consumerProperties = new EventHubsConsumerProperties();
        consumerProperties.setNamespace("a");
        bindingProperties.setConsumer(consumerProperties);

        extendedBindingProperties.afterPropertiesSet();
        assertThat(output).contains(PropertiesValidator.LENGTH_ERROR);
    }

    @Test
    void testProducerNamespaceIllegal(CapturedOutput output) throws Exception {
        EventHubsProducerProperties producerProperties = new EventHubsProducerProperties();
        producerProperties.setNamespace("test_test");
        bindingProperties.setProducer(producerProperties);

        extendedBindingProperties.afterPropertiesSet();
        assertThat(output).contains(PropertiesValidator.ILLEGAL_SYMBOL_ERROR);
    }
}
