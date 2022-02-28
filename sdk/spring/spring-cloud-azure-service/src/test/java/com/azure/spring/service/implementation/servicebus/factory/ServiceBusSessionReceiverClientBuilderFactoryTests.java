// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.implementation.servicebus.factory;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.spring.core.properties.authentication.NamedKeyProperties;
import com.azure.spring.service.implementation.servicebus.properties.ServiceBusReceiverClientTestProperties;
import com.azure.spring.service.servicebus.properties.ServiceBusEntityType;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

class ServiceBusSessionReceiverClientBuilderFactoryTests
    extends AbstractServiceBusSubClientBuilderFactoryTests<
        ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder,
    ServiceBusReceiverClientTestProperties,
        ServiceBusSessionReceiverClientBuilderFactory> {

    @Override
    protected ServiceBusReceiverClientTestProperties createMinimalServiceProperties() {
        ServiceBusReceiverClientTestProperties properties = new ServiceBusReceiverClientTestProperties();
        properties.setEntityName("test");
        properties.setEntityType(ServiceBusEntityType.TOPIC);
        properties.setSubscriptionName("test-subscription");
        return properties;
    }

    @Override
    protected ServiceBusSessionReceiverClientBuilderFactory getMinimalClientBuilderFactory() {
        ServiceBusReceiverClientTestProperties properties = createMinimalServiceProperties();
        return getClientBuilderFactory(properties);
    }

    @Override
    protected ServiceBusSessionReceiverClientBuilderFactory getSasCredentialConfiguredClientBuilderFactory() {
        ServiceBusReceiverClientTestProperties properties = createMinimalServiceProperties();
        properties.setSasToken("test-token");
        properties.setNamespace("test-namespace");
        return getClientBuilderFactory(properties);
    }

    @Override
    protected ServiceBusSessionReceiverClientBuilderFactory getTokenCredentialConfiguredClientBuilderFactory() {
        ServiceBusReceiverClientTestProperties properties = createMinimalServiceProperties();
        properties.setNamespace("test-namespace");
        properties.getCredential().setClientId("test-client");
        properties.getCredential().setClientSecret("test-secret");
        properties.getProfile().setTenantId("test-tenant");
        return getClientBuilderFactory(properties);
    }

    @Override
    protected ServiceBusSessionReceiverClientBuilderFactory getNamedKeyCredentialConfiguredClientBuilderFactory() {
        ServiceBusReceiverClientTestProperties properties = createMinimalServiceProperties();
        properties.setNamespace("test-namespace");
        NamedKeyProperties namedKey = new NamedKeyProperties();
        namedKey.setKey("test-key");
        namedKey.setName("test-name");
        properties.setNamedKey(namedKey);
        return getClientBuilderFactory(properties);
    }

    private ServiceBusSessionReceiverClientBuilderFactoryExt getClientBuilderFactory(ServiceBusReceiverClientTestProperties properties) {
        ServiceBusClientBuilder clientBuilder = mock(ServiceBusClientBuilder.class);
        ServiceBusSessionReceiverClientBuilderFactoryExt factory =
            spy(new ServiceBusSessionReceiverClientBuilderFactoryExt(clientBuilder, properties));
        doReturn(false).when(factory).isShareServiceBusClientBuilder();
        return factory;
    }

    static class ServiceBusSessionReceiverClientBuilderFactoryExt extends ServiceBusSessionReceiverClientBuilderFactory {
        ServiceBusSessionReceiverClientBuilderFactoryExt(ServiceBusClientBuilder clientBuilder,
                                                          ServiceBusReceiverClientTestProperties properties) {
            super(clientBuilder, properties);
        }

        @Override
        public ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder createBuilderInstance() {
            return mock(ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder.class);
        }
    }
}
