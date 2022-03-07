// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhubs;

import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.spring.cloud.autoconfigure.TestBuilderCustomizer;
import com.azure.spring.cloud.service.implementation.eventhubs.factory.EventHubClientBuilderFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static com.azure.spring.cloud.autoconfigure.eventhubs.EventHubsTestUtils.CONNECTION_STRING_FORMAT;
import static org.assertj.core.api.Assertions.assertThat;

class AzureEventHubsClientBuilderConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureEventHubsClientBuilderConfiguration.class));

    @Test
    void noConnectionInfoProvidedShouldNotConfigure() {
        contextRunner.run(context -> assertThat(context).doesNotHaveBean(AzureEventHubsClientBuilderConfiguration.class));
    }

    @Test
    @SuppressWarnings("rawtypes")
    void connectionStringProvidedShouldConfigure() {
        contextRunner
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.connection-string=" + String.format(CONNECTION_STRING_FORMAT, "test-namespace"),
                "spring.cloud.azure.eventhubs.event-hub-name=test-event-hub"
            )
            .withUserConfiguration(AzureEventHubsPropertiesTestConfiguration.class)
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
                "spring.cloud.azure.eventhubs.connection-string=" + String.format(CONNECTION_STRING_FORMAT, "test-namespace"),
                "spring.cloud.azure.eventhubs.event-hub-name=test-event-hub"
            )
            .withUserConfiguration(AzureEventHubsPropertiesTestConfiguration.class)
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
                "spring.cloud.azure.eventhubs.connection-string=" + String.format(CONNECTION_STRING_FORMAT, "test-namespace"),
                "spring.cloud.azure.eventhubs.event-hub-name=test-event-hub"
            )
            .withUserConfiguration(AzureEventHubsPropertiesTestConfiguration.class)
            .withBean("customizer1", EventHubBuilderCustomizer.class, () -> customizer)
            .withBean("customizer2", EventHubBuilderCustomizer.class, () -> customizer)
            .withBean("customizer3", OtherBuilderCustomizer.class, () -> otherBuilderCustomizer)
            .run(context -> {
                assertThat(customizer.getCustomizedTimes()).isEqualTo(2);
                assertThat(otherBuilderCustomizer.getCustomizedTimes()).isEqualTo(0);
            });
    }

    @Test
    void userDefinedEventHubsClientBuilderProvidedShouldNotConfigureTheAuto() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.connection-string=" + String.format(CONNECTION_STRING_FORMAT, "test-namespace"),
                "spring.cloud.azure.eventhubs.event-hub-name=test-event-hub"
            )
            .withUserConfiguration(AzureEventHubsPropertiesTestConfiguration.class)
            .withBean("user-defined-builder", EventHubClientBuilder.class, EventHubClientBuilder::new)
            .run(context -> {
                assertThat(context).hasSingleBean(EventHubClientBuilder.class);
                assertThat(context).hasBean("user-defined-builder");
            });
    }

    private static class EventHubBuilderCustomizer extends TestBuilderCustomizer<EventHubClientBuilder> {

    }

    private static class OtherBuilderCustomizer extends TestBuilderCustomizer<ConfigurationClientBuilder> {

    }

}
