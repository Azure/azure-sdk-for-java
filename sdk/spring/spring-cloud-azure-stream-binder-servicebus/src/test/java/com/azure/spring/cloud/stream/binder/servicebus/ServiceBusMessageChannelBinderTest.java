// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.stream.binder.servicebus;

import com.azure.spring.cloud.service.servicebus.properties.ServiceBusEntityType;
import com.azure.spring.cloud.stream.binder.servicebus.core.properties.ServiceBusBindingProperties;
import com.azure.spring.cloud.stream.binder.servicebus.core.properties.ServiceBusConsumerProperties;
import com.azure.spring.cloud.stream.binder.servicebus.core.properties.ServiceBusExtendedBindingProperties;
import com.azure.spring.cloud.stream.binder.servicebus.core.provisioning.ServiceBusChannelProvisioner;
import com.azure.spring.messaging.servicebus.core.properties.ServiceBusContainerProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cloud.stream.binder.BinderHeaders;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.HeaderMode;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class ServiceBusMessageChannelBinderTest {

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
    private static final String CREATE_CONTAINER_PROPERTIES_METHOD_NAME = "createContainerProperties";

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        GenericApplicationContext context = new GenericApplicationContext();
        binder.setApplicationContext(context);
    }

    @Test
    void testCreateContainerProperties() {
        prepareConsumerProperties();
        when(consumerDestination.getName()).thenReturn(ENTITY_NAME);
        ServiceBusContainerProperties containerProperties = ReflectionTestUtils.invokeMethod(
            binder,
            ServiceBusMessageChannelTestBinder.class,
            CREATE_CONTAINER_PROPERTIES_METHOD_NAME,
            consumerDestination, GROUP, consumerProperties);

        assertThat(containerProperties.getEntityName()).isEqualTo(consumerDestination.getName());
        assertThat(containerProperties.getSubscriptionName()).isEqualTo(GROUP);
        assertThat(containerProperties.getNamespace()).isEqualTo(serviceBusConsumerProperties.getNamespace());
        assertThat(containerProperties.getEntityType()).isEqualTo(serviceBusConsumerProperties.getEntityType());
        assertThat(containerProperties.getAutoComplete()).isEqualTo(serviceBusConsumerProperties.getAutoComplete());
        assertThat(containerProperties.getRetry().getTryTimeout()).isEqualTo(serviceBusConsumerProperties.getRetry().getTryTimeout());

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
