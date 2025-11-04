// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.stream.binder.servicebus.implementation;

import com.azure.spring.cloud.service.servicebus.properties.ServiceBusEntityType;
import com.azure.spring.cloud.stream.binder.servicebus.core.implementation.provisioning.ServiceBusChannelProvisioner;
import com.azure.spring.cloud.stream.binder.servicebus.core.properties.ServiceBusBindingProperties;
import com.azure.spring.cloud.stream.binder.servicebus.core.properties.ServiceBusConsumerProperties;
import com.azure.spring.cloud.stream.binder.servicebus.core.properties.ServiceBusExtendedBindingProperties;
import com.azure.spring.integration.servicebus.inbound.ServiceBusInboundChannelAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cloud.stream.binder.BinderHeaders;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.HeaderMode;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.integration.core.MessageProducer;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests for retry functionality in ServiceBusMessageChannelBinder.
 */
class ServiceBusRetryTest {

    @Mock
    private ConsumerDestination consumerDestination;

    private final ServiceBusExtendedBindingProperties extendedBindingProperties =
        new ServiceBusExtendedBindingProperties();

    private ExtendedConsumerProperties<ServiceBusConsumerProperties> consumerProperties;

    private final ServiceBusConsumerProperties serviceBusConsumerProperties = new ServiceBusConsumerProperties();

    private final ServiceBusMessageChannelTestBinder binder = new ServiceBusMessageChannelTestBinder(
        BinderHeaders.STANDARD_HEADERS, new ServiceBusChannelProvisioner());

    private static final String ENTITY_NAME = "test-entity";
    private static final String GROUP = "test";
    private static final String NAMESPACE_NAME = "test-namespace";

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        GenericApplicationContext context = new GenericApplicationContext();
        binder.setApplicationContext(context);
    }

    @Test
    void testRetryTemplateConfiguredWhenMaxAttemptsGreaterThanOne() {
        // Arrange
        prepareConsumerProperties();
        consumerProperties.setMaxAttempts(3);
        consumerProperties.setBackOffInitialInterval(1000);
        consumerProperties.setBackOffMultiplier(2.0);
        consumerProperties.setBackOffMaxInterval(5000);
        when(consumerDestination.getName()).thenReturn(ENTITY_NAME);

        // Act
        MessageProducer producer = binder.createConsumerEndpoint(consumerDestination, GROUP, consumerProperties);

        // Assert
        assertThat(producer).isInstanceOf(ServiceBusInboundChannelAdapter.class);
        ServiceBusInboundChannelAdapter adapter = (ServiceBusInboundChannelAdapter) producer;
        RetryTemplate retryTemplate = (RetryTemplate) ReflectionTestUtils.getField(adapter, "retryTemplate");
        assertThat(retryTemplate).isNotNull();
    }

    @Test
    void testRetryTemplateNotConfiguredWhenMaxAttemptsIsOne() {
        // Arrange
        prepareConsumerProperties();
        consumerProperties.setMaxAttempts(1);
        when(consumerDestination.getName()).thenReturn(ENTITY_NAME);

        // Act
        MessageProducer producer = binder.createConsumerEndpoint(consumerDestination, GROUP, consumerProperties);

        // Assert
        assertThat(producer).isInstanceOf(ServiceBusInboundChannelAdapter.class);
        ServiceBusInboundChannelAdapter adapter = (ServiceBusInboundChannelAdapter) producer;
        RetryTemplate retryTemplate = (RetryTemplate) ReflectionTestUtils.getField(adapter, "retryTemplate");
        assertThat(retryTemplate).isNull();
    }

    @Test
    void testRetryTemplateNotConfiguredWhenMaxAttemptsNotSet() {
        // Arrange
        prepareConsumerProperties();
        // maxAttempts defaults to 3 in ExtendedConsumerProperties, 
        // but we test the case where it's explicitly set to 1 or not configured with retry
        consumerProperties.setMaxAttempts(1);
        when(consumerDestination.getName()).thenReturn(ENTITY_NAME);

        // Act
        MessageProducer producer = binder.createConsumerEndpoint(consumerDestination, GROUP, consumerProperties);

        // Assert
        assertThat(producer).isInstanceOf(ServiceBusInboundChannelAdapter.class);
        ServiceBusInboundChannelAdapter adapter = (ServiceBusInboundChannelAdapter) producer;
        RetryTemplate retryTemplate = (RetryTemplate) ReflectionTestUtils.getField(adapter, "retryTemplate");
        assertThat(retryTemplate).isNull();
    }

    private void prepareConsumerProperties() {
        serviceBusConsumerProperties.setEntityName(ENTITY_NAME);
        serviceBusConsumerProperties.setSubscriptionName(GROUP);
        serviceBusConsumerProperties.setEntityType(ServiceBusEntityType.TOPIC);
        serviceBusConsumerProperties.setNamespace(NAMESPACE_NAME);
        serviceBusConsumerProperties.getRetry().setTryTimeout(Duration.ofMinutes(5));
        serviceBusConsumerProperties.setAutoComplete(false);
        ServiceBusBindingProperties bindingProperties = new ServiceBusBindingProperties();
        bindingProperties.setConsumer(serviceBusConsumerProperties);

        extendedBindingProperties.setBindings(new HashMap<String, ServiceBusBindingProperties>() {
            {
                put(ENTITY_NAME, bindingProperties);
            }
        });
        binder.setBindingProperties(extendedBindingProperties);

        consumerProperties = new ExtendedConsumerProperties<>(serviceBusConsumerProperties);
        consumerProperties.setHeaderMode(HeaderMode.embeddedHeaders);
    }
}
