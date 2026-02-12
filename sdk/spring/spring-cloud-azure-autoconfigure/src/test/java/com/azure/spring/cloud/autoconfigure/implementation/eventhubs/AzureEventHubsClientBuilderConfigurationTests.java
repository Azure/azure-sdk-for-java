// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.eventhubs;

import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.spring.cloud.autoconfigure.implementation.TestBuilderCustomizer;
import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.eventhubs.properties.AzureEventHubsConnectionDetails;
import com.azure.spring.cloud.core.provider.connectionstring.StaticConnectionStringProvider;
import com.azure.spring.cloud.service.implementation.eventhubs.factory.EventHubClientBuilderFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static com.azure.spring.cloud.autoconfigure.implementation.eventhubs.EventHubsTestUtils.CONNECTION_STRING_FORMAT;
import static org.assertj.core.api.Assertions.assertThat;

class AzureEventHubsClientBuilderConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureEventHubsAutoConfiguration.class));

    @Test
    void noConnectionInfoProvidedShouldNotConfigure() {
        contextRunner.run(context -> assertThat(context).doesNotHaveBean(AzureEventHubsClientBuilderConfiguration.class));
    }

    @Test
    void connectionStringProvidedShouldConfigure() {
        contextRunner
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.connection-string=" + String.format(CONNECTION_STRING_FORMAT, "test-namespace")
            )
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureEventHubsClientBuilderConfiguration.class);
                assertThat(context).hasSingleBean(EventHubClientBuilderFactory.class);
                assertThat(context).hasSingleBean(EventHubClientBuilder.class);
            });
    }

    @Test
    void namespaceProvidedShouldConfigure() {
        contextRunner
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.namespace=test-namespace"
            )
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureEventHubsClientBuilderConfiguration.class);
                assertThat(context).hasSingleBean(EventHubClientBuilderFactory.class);
                assertThat(context).hasSingleBean(EventHubClientBuilder.class);
            });
    }

    @Test
    void customizerShouldBeCalled() {
        EventHubBuilderCustomizer customizer = new EventHubBuilderCustomizer();
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.connection-string=" + String.format(CONNECTION_STRING_FORMAT, "test-namespace")
            )
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .withBean("customizer1", EventHubBuilderCustomizer.class, () -> customizer)
            .withBean("customizer2", EventHubBuilderCustomizer.class, () -> customizer)
            .run(context -> assertThat(customizer.getCustomizedTimes()).isEqualTo(2));
    }

    @Test
    void otherCustomizerShouldNotBeCalled() {
        EventHubBuilderCustomizer customizer = new EventHubBuilderCustomizer();
        OtherBuilderCustomizer otherBuilderCustomizer = new OtherBuilderCustomizer();
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.connection-string=" + String.format(CONNECTION_STRING_FORMAT, "test-namespace")
            )
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .withBean("customizer1", EventHubBuilderCustomizer.class, () -> customizer)
            .withBean("customizer2", EventHubBuilderCustomizer.class, () -> customizer)
            .withBean("customizer3", OtherBuilderCustomizer.class, () -> otherBuilderCustomizer)
            .run(context -> {
                assertThat(customizer.getCustomizedTimes()).isEqualTo(2);
                assertThat(otherBuilderCustomizer.getCustomizedTimes()).isEqualTo(0);
            });
    }

    @Test
    void userDefinedEventHubsClientBuilderProvidedShouldNotAutoconfigure() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.connection-string=" + String.format(CONNECTION_STRING_FORMAT, "test-namespace")
            )
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .withBean("user-defined-builder", EventHubClientBuilder.class, EventHubClientBuilder::new)
            .run(context -> {
                assertThat(context).hasSingleBean(EventHubClientBuilder.class);
                assertThat(context).hasBean("user-defined-builder");
            });
    }

    @Test
    void connectionStringPropertyRegistersStaticProvider() {
        String connectionString = String.format(CONNECTION_STRING_FORMAT, "test-namespace");
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.connection-string=" + connectionString
            )
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .run(context -> {
                assertThat(context).hasSingleBean(StaticConnectionStringProvider.class);
                assertThat(context.getBean(StaticConnectionStringProvider.class).getConnectionString())
                    .isEqualTo(connectionString);
            });
    }

    @Test
    void connectionDetailsRegistersStaticProvider() {
        String connectionString = String.format(CONNECTION_STRING_FORMAT, "details-namespace");
        this.contextRunner
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .withBean(AzureEventHubsConnectionDetails.class, () -> new TestConnectionDetails(connectionString))
            .run(context -> {
                assertThat(context).hasSingleBean(StaticConnectionStringProvider.class);
                assertThat(context.getBean(StaticConnectionStringProvider.class).getConnectionString())
                    .isEqualTo(connectionString);
            });
    }

    @Test
    void namespaceOnlyDoesNotRegisterStaticProvider() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.namespace=test-namespace"
            )
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .run(context -> assertThat(context).doesNotHaveBean(StaticConnectionStringProvider.class));
    }

    private static class EventHubBuilderCustomizer extends TestBuilderCustomizer<EventHubClientBuilder> {

    }

    private static class OtherBuilderCustomizer extends TestBuilderCustomizer<ConfigurationClientBuilder> {

    }

    private static final class TestConnectionDetails implements AzureEventHubsConnectionDetails {
        private final String connectionString;

        TestConnectionDetails(String connectionString) {
            this.connectionString = connectionString;
        }

        @Override
        public String getConnectionString() {
            return this.connectionString;
        }
    }

}
