// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.servicebus.factory;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.spring.cloud.core.properties.authentication.NamedKeyProperties;
import com.azure.spring.cloud.service.implementation.servicebus.properties.ServiceBusProcessorClientTestProperties;
import com.azure.spring.cloud.service.listener.MessageListener;
import com.azure.spring.cloud.service.servicebus.consumer.ServiceBusErrorHandler;
import com.azure.spring.cloud.service.servicebus.consumer.ServiceBusRecordMessageListener;
import com.azure.spring.cloud.service.servicebus.properties.ServiceBusEntityType;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

class ServiceBusSessionProcessorClientBuilderFactoryTests extends AbstractServiceBusSubClientBuilderFactoryTests<ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder,
    ServiceBusProcessorClientTestProperties, ServiceBusSessionProcessorClientBuilderFactory> {

    @Override
    protected ServiceBusProcessorClientTestProperties createMinimalServiceProperties() {
        ServiceBusProcessorClientTestProperties properties = new ServiceBusProcessorClientTestProperties();
        properties.setEntityName("test");
        properties.setEntityType(ServiceBusEntityType.TOPIC);
        properties.setSubscriptionName("test-subscription");
        return properties;
    }

    @Override
    protected ServiceBusSessionProcessorClientBuilderFactory getMinimalClientBuilderFactory() {
        ServiceBusProcessorClientTestProperties properties = createMinimalServiceProperties();
        return getClientBuilderFactory(properties);
    }

    @Override
    protected ServiceBusSessionProcessorClientBuilderFactory getSasCredentialConfiguredClientBuilderFactory() {
        ServiceBusProcessorClientTestProperties properties = createMinimalServiceProperties();
        properties.setSasToken("test-token");
        properties.setNamespace("test-namespace");
        return getClientBuilderFactory(properties);
    }

    @Override
    protected ServiceBusSessionProcessorClientBuilderFactory getTokenCredentialConfiguredClientBuilderFactory() {
        ServiceBusProcessorClientTestProperties properties = createMinimalServiceProperties();
        properties.setNamespace("test-namespace");
        properties.getCredential().setClientId("test-client");
        properties.getCredential().setClientSecret("test-secret");
        properties.getProfile().setTenantId("test-tenant");
        return getClientBuilderFactory(properties);
    }

    @Override
    protected ServiceBusSessionProcessorClientBuilderFactory getNamedKeyCredentialConfiguredClientBuilderFactory() {
        ServiceBusProcessorClientTestProperties properties = createMinimalServiceProperties();
        properties.setNamespace("test-namespace");
        NamedKeyProperties namedKey = new NamedKeyProperties();
        namedKey.setKey("test-key");
        namedKey.setName("test-name");
        properties.setNamedKey(namedKey);
        return getClientBuilderFactory(properties);
    }

    private ServiceBusSessionProcessorClientBuilderFactoryExt getClientBuilderFactory(ServiceBusProcessorClientTestProperties properties) {
        ServiceBusClientBuilder clientBuilder = mock(ServiceBusClientBuilder.class);
        MessageListener<ServiceBusReceivedMessageContext> listener = (ServiceBusRecordMessageListener) messageContext -> { };
        ServiceBusErrorHandler errorHandler = errorContext -> { };
        ServiceBusSessionProcessorClientBuilderFactoryExt factory =
            spy(new ServiceBusSessionProcessorClientBuilderFactoryExt(clientBuilder, properties, listener, errorHandler));
        doReturn(false).when(factory).isShareServiceBusClientBuilder();
        return factory;
    }

    static class ServiceBusSessionProcessorClientBuilderFactoryExt extends ServiceBusSessionProcessorClientBuilderFactory {
        ServiceBusSessionProcessorClientBuilderFactoryExt(ServiceBusClientBuilder clientBuilder,
                                                          ServiceBusProcessorClientTestProperties properties,
                                                          MessageListener<?> messageListener,
                                                          ServiceBusErrorHandler errorHandler) {
            super(clientBuilder, properties, messageListener, errorHandler);
        }

        @Override
        public ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder createBuilderInstance() {
            return mock(ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder.class);
        }
    }
}
