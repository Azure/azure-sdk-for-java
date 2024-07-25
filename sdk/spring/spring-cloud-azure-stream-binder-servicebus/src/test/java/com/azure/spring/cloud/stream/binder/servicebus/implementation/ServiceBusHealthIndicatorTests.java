// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.servicebus.implementation;

import com.azure.spring.cloud.service.servicebus.properties.ServiceBusEntityType;
import com.azure.spring.cloud.stream.binder.servicebus.core.properties.ServiceBusBindingProperties;
import com.azure.spring.cloud.stream.binder.servicebus.core.properties.ServiceBusConsumerProperties;
import com.azure.spring.cloud.stream.binder.servicebus.core.properties.ServiceBusExtendedBindingProperties;
import com.azure.spring.cloud.stream.binder.servicebus.core.properties.ServiceBusProducerProperties;
import com.azure.spring.cloud.stream.binder.servicebus.core.implementation.provisioning.ServiceBusChannelProvisioner;
import com.azure.spring.integration.core.handler.DefaultMessageHandler;
import com.azure.spring.integration.servicebus.inbound.ServiceBusInboundChannelAdapter;
import com.azure.spring.messaging.AzureHeaders;
import com.azure.spring.messaging.servicebus.core.ServiceBusTemplate;
import com.azure.spring.messaging.servicebus.core.DefaultServiceBusNamespaceProducerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.cloud.stream.binder.BinderHeaders;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.binder.HeaderMode;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class ServiceBusHealthIndicatorTests {

    @Mock
    private ConfigurableListableBeanFactory beanFactory;

    @Mock
    private ConsumerDestination consumerDestination;

    @Mock
    private ProducerDestination producerDestination;

    private final ServiceBusExtendedBindingProperties extendedBindingProperties =
        new ServiceBusExtendedBindingProperties();

    private ExtendedProducerProperties<ServiceBusProducerProperties> producerProperties;

    private ExtendedConsumerProperties<ServiceBusConsumerProperties> consumerProperties;

    private final ServiceBusProducerProperties serviceBusProducerProperties = new ServiceBusProducerProperties();

    private final ServiceBusConsumerProperties serviceBusConsumerProperties = new ServiceBusConsumerProperties();
    @Mock
    private MessageChannel errorChannel;

    private final ServiceBusMessageChannelTestBinder binder = new ServiceBusMessageChannelTestBinder(
        BinderHeaders.STANDARD_HEADERS, new ServiceBusChannelProvisioner());

    private ServiceBusHealthIndicator serviceBusHealthIndicator;
    private static final String ENTITY_NAME = "test-entity";
    private static final String GROUP = "test";
    private static final String NAMESPACE_NAME = "test-namespace";


    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        GenericApplicationContext context = new GenericApplicationContext();
        binder.setApplicationContext(context);
        serviceBusHealthIndicator = new ServiceBusHealthIndicator(binder);
    }

    @Test
    void testNoInstrumentationInUse() {
        final Health health = serviceBusHealthIndicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.UNKNOWN);
    }

    @Test
    void testServiceBusProducerHealthIndicatorIsUp() {
        prepareProducerProperties();
        when(producerDestination.getName()).thenReturn(ENTITY_NAME);
        binder.createProducerMessageHandler(producerDestination, producerProperties, errorChannel);
        ServiceBusTemplate serviceBusTemplate = (ServiceBusTemplate) ReflectionTestUtils.getField(binder,
            "serviceBusTemplate");
        DefaultServiceBusNamespaceProducerFactory producerFactory =
            (DefaultServiceBusNamespaceProducerFactory) ReflectionTestUtils.getField(serviceBusTemplate, "producerFactory");
        producerFactory.createProducer(ENTITY_NAME);
        final Health health = serviceBusHealthIndicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.UP);
    }

    @Test
    void testServiceBusProducerHealthIndicatorIsDown() {
        prepareProducerProperties();
        when(producerDestination.getName()).thenReturn(ENTITY_NAME);
        DefaultMessageHandler producerMessageHandler =
            (DefaultMessageHandler) binder.createProducerMessageHandler(producerDestination, producerProperties,
                errorChannel);
        producerMessageHandler.setBeanFactory(beanFactory);
        ReflectionTestUtils.invokeMethod(producerMessageHandler,
            DefaultMessageHandler.class,
            "onInit");
        producerMessageHandler.handleMessage(MessageBuilder.withPayload("test")
                                                           .setHeader(AzureHeaders.PARTITION_KEY, "fake-key")
                                                           .build());
        binder.addProducerDownInstrumentation();
        final Health health = serviceBusHealthIndicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    }

    @Test
    void testServiceBusProcessorHealthIndicatorIsUp() {
        prepareConsumerProperties();
        when(consumerDestination.getName()).thenReturn(ENTITY_NAME);
        ServiceBusInboundChannelAdapter consumerEndpoint = (ServiceBusInboundChannelAdapter) binder.createConsumerEndpoint(consumerDestination, null, consumerProperties);

        consumerEndpoint.afterPropertiesSet();
        consumerEndpoint.start();

        final Health health = serviceBusHealthIndicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.UP);
    }

    @Test
    void testServiceBusProcessorHealthIndicatorIsDown() {
        prepareConsumerProperties();
        when(consumerDestination.getName()).thenReturn(ENTITY_NAME);
        ServiceBusInboundChannelAdapter consumerEndpoint = (ServiceBusInboundChannelAdapter) binder.createConsumerEndpoint(consumerDestination, null, consumerProperties);

        consumerEndpoint.afterPropertiesSet();
        consumerEndpoint.start();

        binder.addProcessorDownInstrumentation();

        final Health health = serviceBusHealthIndicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    }

    private void prepareProducerProperties() {
        serviceBusProducerProperties.setEntityName(ENTITY_NAME);
        serviceBusProducerProperties.setEntityType(ServiceBusEntityType.TOPIC);
        serviceBusProducerProperties.setNamespace(NAMESPACE_NAME);
        serviceBusProducerProperties.setSync(false);
        ServiceBusBindingProperties bindingProperties = new ServiceBusBindingProperties();
        bindingProperties.setProducer(serviceBusProducerProperties);

        extendedBindingProperties.setBindings(new HashMap<String, ServiceBusBindingProperties>() {
            {
                put(ENTITY_NAME, bindingProperties);
            }
        });
        binder.setBindingProperties(extendedBindingProperties);

        producerProperties = new ExtendedProducerProperties<>(serviceBusProducerProperties);
        producerProperties.setHeaderMode(HeaderMode.embeddedHeaders);
    }

    private void prepareConsumerProperties() {
        serviceBusConsumerProperties.setEntityName(ENTITY_NAME);
        serviceBusConsumerProperties.setEntityType(ServiceBusEntityType.QUEUE);
        serviceBusConsumerProperties.setNamespace(NAMESPACE_NAME);
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
