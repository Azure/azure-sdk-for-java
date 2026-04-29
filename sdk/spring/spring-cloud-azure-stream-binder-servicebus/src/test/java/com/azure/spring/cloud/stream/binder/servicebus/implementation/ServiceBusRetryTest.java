// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.stream.binder.servicebus.implementation;

import com.azure.spring.cloud.service.servicebus.properties.ServiceBusEntityType;
import com.azure.spring.cloud.stream.binder.servicebus.core.implementation.provisioning.ServiceBusChannelProvisioner;
import com.azure.spring.cloud.stream.binder.servicebus.core.properties.ServiceBusBindingProperties;
import com.azure.spring.cloud.stream.binder.servicebus.core.properties.ServiceBusConsumerProperties;
import com.azure.spring.cloud.stream.binder.servicebus.core.properties.ServiceBusExtendedBindingProperties;
import com.azure.spring.integration.servicebus.inbound.ServiceBusInboundChannelAdapter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.stream.binder.BinderHeaders;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.HeaderMode;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.integration.core.MessageProducer;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Tests for retry functionality in ServiceBusMessageChannelBinder.
 */
@ExtendWith(MockitoExtension.class)
class ServiceBusRetryTest {

    @Mock
    private ConsumerDestination consumerDestination;

    private final ServiceBusExtendedBindingProperties extendedBindingProperties =
        new ServiceBusExtendedBindingProperties();

    private ExtendedConsumerProperties<ServiceBusConsumerProperties> consumerProperties;

    private final ServiceBusConsumerProperties serviceBusConsumerProperties = new ServiceBusConsumerProperties();

    private ServiceBusMessageChannelTestBinder binder;

    private GenericApplicationContext applicationContext;

    private static final String ENTITY_NAME = "test-entity";
    private static final String GROUP = "test";
    private static final String NAMESPACE_NAME = "test-namespace";

    @BeforeEach
    void init() {
        binder = new ServiceBusMessageChannelTestBinder(
            BinderHeaders.STANDARD_HEADERS, new ServiceBusChannelProvisioner());
        applicationContext = new GenericApplicationContext();
        applicationContext.refresh();
        binder.setApplicationContext(applicationContext);
    }

    @AfterEach
    void tearDown() {
        if (applicationContext != null) {
            applicationContext.close();
        }
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
        RetryTemplate retryTemplate = adapter.getRetryTemplate();
        assertThat(retryTemplate).isNotNull();

        // Verify maxAttempts=3 by executing the template and counting actual attempts
        AtomicInteger callCount = new AtomicInteger(0);
        assertThatThrownBy(() -> retryTemplate.execute(ctx -> {
            callCount.incrementAndGet();
            throw new RuntimeException("test");
        })).isInstanceOf(RuntimeException.class);
        assertThat(callCount.get()).isEqualTo(3);

        // Verify backoff policy configuration via the binder's factory method (no reflection needed)
        ExponentialBackOffPolicy backOffPolicy = binder.createExponentialBackOffPolicy(consumerProperties);
        assertThat(backOffPolicy.getInitialInterval()).isEqualTo(1000L);
        assertThat(backOffPolicy.getMultiplier()).isEqualTo(2.0);
        assertThat(backOffPolicy.getMaxInterval()).isEqualTo(5000L);
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
        assertThat(((ServiceBusInboundChannelAdapter) producer).getRetryTemplate()).isNull();
    }

    @Test
    void testRetryTemplateConfiguredWithDefaultSettings() {
        // Arrange
        prepareConsumerProperties();
        // Spring Cloud Stream default maxAttempts is 3 (> 1), so a RetryTemplate should be created.
        when(consumerDestination.getName()).thenReturn(ENTITY_NAME);

        // Act
        MessageProducer producer = binder.createConsumerEndpoint(consumerDestination, GROUP, consumerProperties);

        // Assert
        assertThat(producer).isInstanceOf(ServiceBusInboundChannelAdapter.class);
        ServiceBusInboundChannelAdapter adapter = (ServiceBusInboundChannelAdapter) producer;
        RetryTemplate retryTemplate = adapter.getRetryTemplate();
        assertThat(retryTemplate).isNotNull();

        // Verify maxAttempts matches Spring Cloud Stream's default via observable behavior
        int expectedMaxAttempts = new ExtendedConsumerProperties<>(new ServiceBusConsumerProperties()).getMaxAttempts();
        AtomicInteger callCount = new AtomicInteger(0);
        assertThatThrownBy(() -> retryTemplate.execute(ctx -> {
            callCount.incrementAndGet();
            throw new RuntimeException("test");
        })).isInstanceOf(RuntimeException.class);
        assertThat(callCount.get()).isEqualTo(expectedMaxAttempts);
    }

    @Test
    void testCustomRetryTemplateIsUsed() {
        // Arrange
        prepareConsumerProperties();
        consumerProperties.setMaxAttempts(3);
        when(consumerDestination.getName()).thenReturn(ENTITY_NAME);

        // Create a custom RetryTemplate
        RetryTemplate customRetryTemplate = new RetryTemplate();
        binder.setRetryTemplate(customRetryTemplate);

        // Act
        MessageProducer producer = binder.createConsumerEndpoint(consumerDestination, GROUP, consumerProperties);

        // Assert
        assertThat(producer).isInstanceOf(ServiceBusInboundChannelAdapter.class);
        ServiceBusInboundChannelAdapter adapter = (ServiceBusInboundChannelAdapter) producer;
        assertThat(adapter.getRetryTemplate()).isNotNull();
        assertThat(adapter.getRetryTemplate()).isSameAs(customRetryTemplate);
    }

    @Test
    void testCustomRetryTemplateNotAppliedWhenMaxAttemptsIsOne() {
        // Arrange: maxAttempts=1 disables retry even when a custom RetryTemplate bean is injected
        prepareConsumerProperties();
        consumerProperties.setMaxAttempts(1);
        when(consumerDestination.getName()).thenReturn(ENTITY_NAME);

        RetryTemplate customRetryTemplate = new RetryTemplate();
        binder.setRetryTemplate(customRetryTemplate);

        // Act
        MessageProducer producer = binder.createConsumerEndpoint(consumerDestination, GROUP, consumerProperties);

        // Assert
        assertThat(producer).isInstanceOf(ServiceBusInboundChannelAdapter.class);
        assertThat(((ServiceBusInboundChannelAdapter) producer).getRetryTemplate()).isNull();
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

        Map<String, ServiceBusBindingProperties> bindings = new HashMap<>();
        bindings.put(ENTITY_NAME, bindingProperties);
        extendedBindingProperties.setBindings(bindings);
        binder.setBindingProperties(extendedBindingProperties);

        consumerProperties = new ExtendedConsumerProperties<>(serviceBusConsumerProperties);
        consumerProperties.setHeaderMode(HeaderMode.embeddedHeaders);
    }
}
