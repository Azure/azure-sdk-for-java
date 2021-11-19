// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.context;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.spring.cloud.autoconfigure.TestBuilderCustomizer;
import com.azure.spring.cloud.autoconfigure.properties.AzureGlobalProperties;
import com.azure.spring.core.factory.credential.AbstractAzureCredentialBuilderFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 */
class AzureDefaultTokenCredentialAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureDefaultTokenCredentialAutoConfiguration.class));

    @Test
    void byDefaultShouldConfigure() {
        contextRunner
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .run(context -> {
                assertThat(context).hasSingleBean(AbstractAzureCredentialBuilderFactory.class);
                assertThat(context).hasSingleBean(TokenCredential.class);

                final TokenCredential credential = context.getBean(TokenCredential.class);
                Assertions.assertTrue(credential instanceof DefaultAzureCredential);

            });
    }

    @Test
    void customizerShouldBeCalled() {
        DefaultTokenCredentialBuilderCustomizer customizer = new DefaultTokenCredentialBuilderCustomizer();
        this.contextRunner
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .withBean("customizer1", DefaultTokenCredentialBuilderCustomizer.class, () -> customizer)
            .withBean("customizer2", DefaultTokenCredentialBuilderCustomizer.class, () -> customizer)
            .run(context -> assertThat(customizer.getCustomizedTimes()).isEqualTo(2));
    }

    @Test
    void otherCustomizerShouldNotBeCalled() {
        DefaultTokenCredentialBuilderCustomizer customizer = new DefaultTokenCredentialBuilderCustomizer();
        OtherBuilderCustomizer otherBuilderCustomizer = new OtherBuilderCustomizer();
        this.contextRunner
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .withBean("customizer1", DefaultTokenCredentialBuilderCustomizer.class, () -> customizer)
            .withBean("customizer2", DefaultTokenCredentialBuilderCustomizer.class, () -> customizer)
            .withBean("customizer3", OtherBuilderCustomizer.class, () -> otherBuilderCustomizer)
            .run(context -> {
                assertThat(customizer.getCustomizedTimes()).isEqualTo(2);
                assertThat(otherBuilderCustomizer.getCustomizedTimes()).isEqualTo(0);
            });
    }

    private static class DefaultTokenCredentialBuilderCustomizer extends TestBuilderCustomizer<DefaultAzureCredentialBuilder> {

    }

    private static class OtherBuilderCustomizer extends TestBuilderCustomizer<EventHubClientBuilder> {

    }

}
