// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.servicebus.factory;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.spring.core.properties.authentication.NamedKeyProperties;
import com.azure.spring.service.servicebus.TestServiceBusReceiverClientProperties;
import com.azure.spring.service.servicebus.properties.ServiceBusEntityType;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

class ServiceBusReceiverClientBuilderFactoryTests
    extends AbstractServiceBusSubClientBuilderFactoryTests<ServiceBusClientBuilder.ServiceBusReceiverClientBuilder,
    TestServiceBusReceiverClientProperties, ServiceBusReceiverClientBuilderFactory> {

    @Override
    protected TestServiceBusReceiverClientProperties createMinimalServiceProperties() {
        TestServiceBusReceiverClientProperties properties = new TestServiceBusReceiverClientProperties();
        properties.setEntityName("test");
        properties.setEntityType(ServiceBusEntityType.TOPIC);
        properties.setSubscriptionName("test-subscription");
        return properties;
    }

    @Override
    protected ServiceBusReceiverClientBuilderFactory getMinimalClientBuilderFactory() {
        TestServiceBusReceiverClientProperties properties = createMinimalServiceProperties();
        return getClientBuilderFactory(properties);
    }

    @Override
    protected ServiceBusReceiverClientBuilderFactory getSasCredentialConfiguredClientBuilderFactory() {
        TestServiceBusReceiverClientProperties properties = createMinimalServiceProperties();
        properties.setSasToken("test-token");
        properties.setNamespace("test-namespace");
        return getClientBuilderFactory(properties);
    }

    @Override
    protected ServiceBusReceiverClientBuilderFactory getTokenCredentialConfiguredClientBuilderFactory() {
        TestServiceBusReceiverClientProperties properties = createMinimalServiceProperties();
        properties.setNamespace("test-namespace");
        properties.getCredential().setClientId("test-client");
        properties.getCredential().setClientSecret("test-secret");
        properties.getProfile().setTenantId("test-tenant");
        return getClientBuilderFactory(properties);
    }

    @Override
    protected ServiceBusReceiverClientBuilderFactory getNamedKeyCredentialConfiguredClientBuilderFactory() {
        TestServiceBusReceiverClientProperties properties = createMinimalServiceProperties();
        properties.setNamespace("test-namespace");
        NamedKeyProperties namedKey = new NamedKeyProperties();
        namedKey.setKey("test-key");
        namedKey.setName("test-name");
        properties.setNamedKey(namedKey);
        return getClientBuilderFactory(properties);
    }

    private ServiceBusReceiverClientBuilderFactoryExt getClientBuilderFactory(TestServiceBusReceiverClientProperties properties) {
        ServiceBusClientBuilder clientBuilder = mock(ServiceBusClientBuilder.class);
        ServiceBusReceiverClientBuilderFactoryExt factory =
            spy(new ServiceBusReceiverClientBuilderFactoryExt(clientBuilder, properties));
        doReturn(false).when(factory).isShareServiceBusClientBuilder();
        return factory;
    }

    static class ServiceBusReceiverClientBuilderFactoryExt extends ServiceBusReceiverClientBuilderFactory {
        ServiceBusReceiverClientBuilderFactoryExt(ServiceBusClientBuilder serviceBusClientBuilder,
                                                   TestServiceBusReceiverClientProperties properties) {
            super(serviceBusClientBuilder, properties);
        }

        @Override
        public ServiceBusClientBuilder.ServiceBusReceiverClientBuilder createBuilderInstance() {
            return mock(ServiceBusClientBuilder.ServiceBusReceiverClientBuilder.class);
        }
    }
}
