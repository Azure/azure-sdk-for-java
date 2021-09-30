// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.spring.core.StaticConnectionStringProvider;
import com.azure.spring.core.service.AzureServiceType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static com.azure.spring.cloud.autoconfigure.servicebus.ServiceBusTestUtils.CONNECTION_STRING;
import static org.assertj.core.api.Assertions.assertThat;

class AzureServiceBusClientBuilderConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureServiceBusClientBuilderConfiguration.class));

    @Test
    void noConnectionInfoProvidedShouldNotConfigure() {
        contextRunner.run(context -> assertThat(context).doesNotHaveBean(AzureServiceBusClientBuilderConfiguration.class));
    }

    @Test
    @SuppressWarnings("rawtypes")
    void connectionStringProvidedShouldConfigure() {
        contextRunner
            .withPropertyValues(
                "spring.cloud.azure.servicebus.connection-string=" + String.format(CONNECTION_STRING, "test-namespace")
            )
            .withUserConfiguration(AzureServiceBusPropertiesTestConfiguration.class)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureServiceBusClientBuilderConfiguration.class);
                assertThat(context).hasSingleBean(ServiceBusClientBuilderFactory.class);
                assertThat(context).hasSingleBean(ServiceBusClientBuilder.class);
                assertThat(context).hasSingleBean(StaticConnectionStringProvider.class);
                StaticConnectionStringProvider connectionStringProvider = context.getBean(StaticConnectionStringProvider.class);
                Assertions.assertEquals(AzureServiceType.SERVICE_BUS, connectionStringProvider.getServiceType());

            });
    }

}
