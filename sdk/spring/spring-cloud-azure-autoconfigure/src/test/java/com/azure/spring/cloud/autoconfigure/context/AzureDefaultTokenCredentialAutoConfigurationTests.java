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
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 */
class AzureDefaultTokenCredentialAutoConfigurationTests {

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

    @Test
    void httpClientOptionsShouldConfigure() {
        AzureGlobalProperties azureGlobalProperties = new AzureGlobalProperties();
        azureGlobalProperties.getClient().getHttp().setConnectTimeout(Duration.ofSeconds(2));
        contextRunner
            .withBean(AzureGlobalProperties.class, () -> azureGlobalProperties)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureDefaultTokenCredentialAutoConfiguration.class);

                AzureDefaultTokenCredentialAutoConfiguration configuration = context.getBean(AzureDefaultTokenCredentialAutoConfiguration.class);
                AzureDefaultTokenCredentialAutoConfiguration.IdentityClientProperties identityClientProperties = (AzureDefaultTokenCredentialAutoConfiguration.IdentityClientProperties) ReflectionTestUtils.getField(configuration, "identityClientProperties");
                assertThat(identityClientProperties).isNotNull();
                assertThat(identityClientProperties.getClient().getConnectTimeout()).isEqualTo(Duration.ofSeconds(2));
            });
    }

    @Test
    void httpRetryOptionsShouldConfigure() {
        AzureGlobalProperties azureGlobalProperties = new AzureGlobalProperties();
        azureGlobalProperties.getRetry().setMaxAttempts(2);
        azureGlobalProperties.getRetry().getHttp().setRetryAfterHeader("test-header");
        contextRunner
            .withBean(AzureGlobalProperties.class, () -> azureGlobalProperties)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureDefaultTokenCredentialAutoConfiguration.class);

                AzureDefaultTokenCredentialAutoConfiguration configuration = context.getBean(AzureDefaultTokenCredentialAutoConfiguration.class);
                AzureDefaultTokenCredentialAutoConfiguration.IdentityClientProperties identityClientProperties = (AzureDefaultTokenCredentialAutoConfiguration.IdentityClientProperties) ReflectionTestUtils.getField(configuration, "identityClientProperties");
                assertThat(identityClientProperties).isNotNull();
                assertThat(identityClientProperties.getRetry().getMaxAttempts()).isEqualTo(2);
                assertThat(identityClientProperties.getRetry().getRetryAfterHeader()).isEqualTo("test-header");
            });
    }

    @Test
    void httpProxyOptionsShouldConfigure() {
        AzureGlobalProperties azureGlobalProperties = new AzureGlobalProperties();

        azureGlobalProperties.getProxy().setHostname("test-host");
        azureGlobalProperties.getProxy().getHttp().setNonProxyHosts("test-non-proxy-host");
        contextRunner
            .withBean(AzureGlobalProperties.class, () -> azureGlobalProperties)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureDefaultTokenCredentialAutoConfiguration.class);

                AzureDefaultTokenCredentialAutoConfiguration configuration = context.getBean(AzureDefaultTokenCredentialAutoConfiguration.class);
                AzureDefaultTokenCredentialAutoConfiguration.IdentityClientProperties identityClientProperties = (AzureDefaultTokenCredentialAutoConfiguration.IdentityClientProperties) ReflectionTestUtils.getField(configuration, "identityClientProperties");
                assertThat(identityClientProperties).isNotNull();
                assertThat(identityClientProperties.getProxy().getHostname()).isEqualTo("test-host");
                assertThat(identityClientProperties.getProxy().getNonProxyHosts()).isEqualTo("test-non-proxy-host");
            });
    }

    private static class DefaultTokenCredentialBuilderCustomizer extends TestBuilderCustomizer<DefaultAzureCredentialBuilder> {

    }

    private static class OtherBuilderCustomizer extends TestBuilderCustomizer<EventHubClientBuilder> {

    }

}
