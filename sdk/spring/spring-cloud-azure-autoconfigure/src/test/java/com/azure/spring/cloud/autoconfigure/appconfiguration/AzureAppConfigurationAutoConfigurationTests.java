// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.appconfiguration;

import com.azure.data.appconfiguration.ConfigurationAsyncClient;
import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.spring.cloud.autoconfigure.TestBuilderCustomizer;
import com.azure.spring.cloud.autoconfigure.implementation.appconfiguration.AzureAppConfigurationProperties;
import com.azure.spring.cloud.autoconfigure.keyvault.secrets.AzureKeyVaultSecretAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.service.implementation.appconfiguration.ConfigurationClientBuilderFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 */
class AzureAppConfigurationAutoConfigurationTests {

    private static final String ENDPOINT = "https://%s.azconfig.io";

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureAppConfigurationAutoConfiguration.class));

    @Test
    void withoutConfigurationClientBuilderShouldNotConfigure() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.appconfiguration.endpoint=" + String.format(ENDPOINT, "my-appconfig"))
            .withClassLoader(new FilteredClassLoader(ConfigurationClientBuilder.class))
            .run(context -> assertThat(context).doesNotHaveBean(AzureAppConfigurationAutoConfiguration.class));
    }

    @Test
    void disableAppConfigurationSecretShouldNotConfigure() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.appconfiguration.enabled=false",
                "spring.cloud.azure.appconfiguration.endpoint=" + String.format(ENDPOINT, "my-appconfig")
            )
            .run(context -> assertThat(context).doesNotHaveBean(AzureAppConfigurationAutoConfiguration.class));
    }

    @Test
    void withoutEndpointShouldNotConfigure() {
        this.contextRunner
            .run(context -> assertThat(context).doesNotHaveBean(AzureKeyVaultSecretAutoConfiguration.class));
    }

    @Test
    void withEndpointShouldConfigure() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.appconfiguration.endpoint=" + String.format(ENDPOINT, "my-appconfig"))
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureAppConfigurationAutoConfiguration.class);
                assertThat(context).hasSingleBean(AzureAppConfigurationProperties.class);
                assertThat(context).hasSingleBean(ConfigurationClient.class);
                assertThat(context).hasSingleBean(ConfigurationAsyncClient.class);
                assertThat(context).hasSingleBean(ConfigurationClientBuilder.class);
                assertThat(context).hasSingleBean(ConfigurationClientBuilderFactory.class);
            });
    }

    @Test
    void customizerShouldBeCalled() {
        AppConfigurationBuilderCustomizer customizer = new AppConfigurationBuilderCustomizer();
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.appconfiguration.endpoint=" + String.format(ENDPOINT, "my-appconfig"))
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .withBean("customizer1", AppConfigurationBuilderCustomizer.class, () -> customizer)
            .withBean("customizer2", AppConfigurationBuilderCustomizer.class, () -> customizer)
            .run(context -> assertThat(customizer.getCustomizedTimes()).isEqualTo(2));
    }

    @Test
    void otherCustomizerShouldNotBeCalled() {
        AppConfigurationBuilderCustomizer customizer = new AppConfigurationBuilderCustomizer();
        OtherBuilderCustomizer otherBuilderCustomizer = new OtherBuilderCustomizer();
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.appconfiguration.endpoint=" + String.format(ENDPOINT, "my-appconfig"))
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .withBean("customizer1", AppConfigurationBuilderCustomizer.class, () -> customizer)
            .withBean("customizer2", AppConfigurationBuilderCustomizer.class, () -> customizer)
            .withBean("customizer3", OtherBuilderCustomizer.class, () -> otherBuilderCustomizer)
            .run(context -> {
                assertThat(customizer.getCustomizedTimes()).isEqualTo(2);
                assertThat(otherBuilderCustomizer.getCustomizedTimes()).isEqualTo(0);
            });
    }

    private static class AppConfigurationBuilderCustomizer extends TestBuilderCustomizer<ConfigurationClientBuilder> {

    }

    private static class OtherBuilderCustomizer extends TestBuilderCustomizer<EventHubClientBuilder> {

    }
}
