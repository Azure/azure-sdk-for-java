// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.implementation.servicebus.factory;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.spring.core.properties.authentication.NamedKeyProperties;
import com.azure.spring.service.implementation.servicebus.properties.ServiceBusSenderClientTestProperties;
import com.azure.spring.service.servicebus.properties.ServiceBusEntityType;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

class ServiceBusSenderClientBuilderFactoryTests extends AbstractServiceBusSubClientBuilderFactoryTests<ServiceBusClientBuilder.ServiceBusSenderClientBuilder,
    ServiceBusSenderClientTestProperties, ServiceBusSenderClientBuilderFactory> {

    @Override
    protected ServiceBusSenderClientTestProperties createMinimalServiceProperties() {
        ServiceBusSenderClientTestProperties properties = new ServiceBusSenderClientTestProperties();
        properties.setEntityName("test");
        properties.setEntityType(ServiceBusEntityType.TOPIC);
        properties.setSubscriptionName("test-subscription");
        return properties;
    }

    @Override
    protected ServiceBusSenderClientBuilderFactory getMinimalClientBuilderFactory() {
        ServiceBusSenderClientTestProperties properties = createMinimalServiceProperties();
        return getClientBuilderFactory(properties);
    }

    @Override
    protected ServiceBusSenderClientBuilderFactory getSasCredentialConfiguredClientBuilderFactory() {
        ServiceBusSenderClientTestProperties properties = createMinimalServiceProperties();
        properties.setSasToken("test-token");
        properties.setNamespace("test-namespace");
        return getClientBuilderFactory(properties);
    }

    @Override
    protected ServiceBusSenderClientBuilderFactory getTokenCredentialConfiguredClientBuilderFactory() {
        ServiceBusSenderClientTestProperties properties = createMinimalServiceProperties();
        properties.setNamespace("test-namespace");
        properties.getCredential().setClientId("test-client");
        properties.getCredential().setClientSecret("test-secret");
        properties.getProfile().setTenantId("test-tenant");
        return getClientBuilderFactory(properties);
    }

    @Override
    protected ServiceBusSenderClientBuilderFactory getNamedKeyCredentialConfiguredClientBuilderFactory() {
        ServiceBusSenderClientTestProperties properties = createMinimalServiceProperties();
        properties.setNamespace("test-namespace");
        NamedKeyProperties namedKey = new NamedKeyProperties();
        namedKey.setKey("test-key");
        namedKey.setName("test-name");
        properties.setNamedKey(namedKey);
        return getClientBuilderFactory(properties);
    }

    private ServiceBusReceiverClientBuilderFactoryExt getClientBuilderFactory(ServiceBusSenderClientTestProperties properties) {
        ServiceBusClientBuilder clientBuilder = mock(ServiceBusClientBuilder.class);
        ServiceBusReceiverClientBuilderFactoryExt factory =
            spy(new ServiceBusReceiverClientBuilderFactoryExt(clientBuilder, properties));
        doReturn(false).when(factory).isShareServiceBusClientBuilder();
        return factory;
    }

    static class ServiceBusReceiverClientBuilderFactoryExt extends ServiceBusSenderClientBuilderFactory {
        ServiceBusReceiverClientBuilderFactoryExt(ServiceBusClientBuilder serviceBusClientBuilder,
                                                  ServiceBusSenderClientTestProperties properties) {
            super(serviceBusClientBuilder, properties);
        }

        @Override
        public ServiceBusClientBuilder.ServiceBusSenderClientBuilder createBuilderInstance() {
            return mock(ServiceBusClientBuilder.ServiceBusSenderClientBuilder.class);
        }
    }
}
