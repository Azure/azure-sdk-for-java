// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.spring.cloud.autoconfigure.TestBuilderCustomizer;
import com.azure.spring.cloud.core.provider.connectionstring.StaticConnectionStringProvider;
import com.azure.spring.cloud.core.service.AzureServiceType;
import com.azure.spring.cloud.service.implementation.servicebus.factory.ServiceBusClientBuilderFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static com.azure.spring.cloud.autoconfigure.servicebus.ServiceBusTestUtils.CONNECTION_STRING_FORMAT;
import static org.assertj.core.api.Assertions.assertThat;

class AzureServiceBusClientBuilderConfigurationTests {

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
                "spring.cloud.azure.servicebus.connection-string=" + String.format(CONNECTION_STRING_FORMAT, "test-namespace")
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

    @Test
    void customizerShouldBeCalled() {
        ServiceBusBuilderCustomizer customizer = new ServiceBusBuilderCustomizer();
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.servicebus.connection-string=" + String.format(CONNECTION_STRING_FORMAT, "test-namespace")
            )
            .withUserConfiguration(AzureServiceBusPropertiesTestConfiguration.class)
            .withBean("customizer1", ServiceBusBuilderCustomizer.class, () -> customizer)
            .withBean("customizer2", ServiceBusBuilderCustomizer.class, () -> customizer)
            .run(context -> assertThat(customizer.getCustomizedTimes()).isEqualTo(2));
    }

    @Test
    void otherCustomizerShouldNotBeCalled() {
        ServiceBusBuilderCustomizer customizer = new ServiceBusBuilderCustomizer();
        OtherBuilderCustomizer otherBuilderCustomizer = new OtherBuilderCustomizer();
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.servicebus.connection-string=" + String.format(CONNECTION_STRING_FORMAT, "test-namespace")
            )
            .withUserConfiguration(AzureServiceBusPropertiesTestConfiguration.class)
            .withBean("customizer1", ServiceBusBuilderCustomizer.class, () -> customizer)
            .withBean("customizer2", ServiceBusBuilderCustomizer.class, () -> customizer)
            .withBean("customizer3", OtherBuilderCustomizer.class, () -> otherBuilderCustomizer)
            .run(context -> {
                assertThat(customizer.getCustomizedTimes()).isEqualTo(2);
                assertThat(otherBuilderCustomizer.getCustomizedTimes()).isEqualTo(0);
            });
    }

    private static class ServiceBusBuilderCustomizer extends TestBuilderCustomizer<ServiceBusClientBuilder> {

    }

    private static class OtherBuilderCustomizer extends TestBuilderCustomizer<ConfigurationClientBuilder> {

    }

}
