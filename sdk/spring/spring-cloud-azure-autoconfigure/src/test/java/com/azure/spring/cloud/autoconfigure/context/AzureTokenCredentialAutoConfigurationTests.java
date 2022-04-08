// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.context;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ClientCertificateCredential;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.identity.ManagedIdentityCredential;
import com.azure.identity.UsernamePasswordCredential;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.spring.cloud.autoconfigure.TestBuilderCustomizer;
import com.azure.spring.cloud.core.implementation.credential.resolver.AzureTokenCredentialResolver;
import com.azure.spring.cloud.core.implementation.factory.credential.ClientCertificateCredentialBuilderFactory;
import com.azure.spring.cloud.core.implementation.factory.credential.ClientSecretCredentialBuilderFactory;
import com.azure.spring.cloud.core.implementation.factory.credential.DefaultAzureCredentialBuilderFactory;
import com.azure.spring.cloud.core.implementation.factory.credential.ManagedIdentityCredentialBuilderFactory;
import com.azure.spring.cloud.core.implementation.factory.credential.UsernamePasswordCredentialBuilderFactory;
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
class AzureTokenCredentialAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureTokenCredentialAutoConfiguration.class));

    @Test
    void byDefaultShouldConfigure() {
        contextRunner
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .run(context -> {
                assertThat(context).hasSingleBean(DefaultAzureCredentialBuilderFactory.class);
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
                assertThat(context).hasSingleBean(AzureTokenCredentialAutoConfiguration.class);

                AzureTokenCredentialAutoConfiguration configuration = context.getBean(AzureTokenCredentialAutoConfiguration.class);
                AzureTokenCredentialAutoConfiguration.IdentityClientProperties identityClientProperties = (AzureTokenCredentialAutoConfiguration.IdentityClientProperties) ReflectionTestUtils.getField(configuration, "identityClientProperties");
                assertThat(identityClientProperties).isNotNull();
                assertThat(identityClientProperties.getClient().getConnectTimeout()).isEqualTo(Duration.ofSeconds(2));
            });
    }

    @Test
    void httpRetryOptionsShouldConfigure() {
        AzureGlobalProperties azureGlobalProperties = new AzureGlobalProperties();
        azureGlobalProperties.getRetry().getExponential().setMaxRetries(2);
        contextRunner
            .withBean(AzureGlobalProperties.class, () -> azureGlobalProperties)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureTokenCredentialAutoConfiguration.class);

                AzureTokenCredentialAutoConfiguration configuration = context.getBean(AzureTokenCredentialAutoConfiguration.class);
                AzureTokenCredentialAutoConfiguration.IdentityClientProperties identityClientProperties = (AzureTokenCredentialAutoConfiguration.IdentityClientProperties) ReflectionTestUtils.getField(configuration, "identityClientProperties");
                assertThat(identityClientProperties).isNotNull();
                assertThat(identityClientProperties.getRetry().getExponential().getMaxRetries()).isEqualTo(2);
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
                assertThat(context).hasSingleBean(AzureTokenCredentialAutoConfiguration.class);

                AzureTokenCredentialAutoConfiguration configuration = context.getBean(AzureTokenCredentialAutoConfiguration.class);
                AzureTokenCredentialAutoConfiguration.IdentityClientProperties identityClientProperties = (AzureTokenCredentialAutoConfiguration.IdentityClientProperties) ReflectionTestUtils.getField(configuration, "identityClientProperties");
                assertThat(identityClientProperties).isNotNull();
                assertThat(identityClientProperties.getProxy().getHostname()).isEqualTo("test-host");
                assertThat(identityClientProperties.getProxy().getNonProxyHosts()).isEqualTo("test-non-proxy-host");
            });
    }


    @Test
    void emptyPropertiesShouldNotResolve() {
        AzureGlobalProperties properties = new AzureGlobalProperties();

        contextRunner
            .withBean(AzureGlobalProperties.class, () -> properties)
                .run(context -> {
                    assertThat(context).hasSingleBean(AzureTokenCredentialResolver.class);
                    AzureTokenCredentialResolver resolver = context.getBean(AzureTokenCredentialResolver.class);
                    Assertions.assertNull(resolver.resolve(properties));
                });
    }

    @Test
    void shouldResolveClientSecretTokenCredential() {
        AzureGlobalProperties properties = new AzureGlobalProperties();
        properties.getCredential().setClientId("test-client-id");
        properties.getCredential().setClientSecret("test-client-secret");
        properties.getProfile().setTenantId("test-tenant-id");
        contextRunner
            .withBean(AzureGlobalProperties.class, () -> properties)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureTokenCredentialResolver.class);
                AzureTokenCredentialResolver resolver = context.getBean(AzureTokenCredentialResolver.class);
                Assertions.assertEquals(ClientSecretCredential.class, resolver.resolve(properties).getClass());
            });
    }

    @Test
    void shouldResolveClientCertificateTokenCredential() {
        AzureGlobalProperties properties = new AzureGlobalProperties();
        properties.getCredential().setClientId("test-client-id");
        properties.getCredential().setClientCertificatePath("test-client-cert-path");
        properties.getProfile().setTenantId("test-tenant-id");
        contextRunner
            .withBean(AzureGlobalProperties.class, () -> properties)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureTokenCredentialResolver.class);
                AzureTokenCredentialResolver resolver = context.getBean(AzureTokenCredentialResolver.class);
                Assertions.assertEquals(ClientCertificateCredential.class, resolver.resolve(properties).getClass());
            });
    }

    @Test
    void shouldResolveUserAssignedMITokenCredential() {
        AzureGlobalProperties properties = new AzureGlobalProperties();
        properties.getCredential().setClientId("test-mi-client-id");
        properties.getCredential().setManagedIdentityEnabled(true);
        contextRunner
            .withBean(AzureGlobalProperties.class, () -> properties)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureTokenCredentialResolver.class);
                AzureTokenCredentialResolver resolver = context.getBean(AzureTokenCredentialResolver.class);
                Assertions.assertEquals(ManagedIdentityCredential.class, resolver.resolve(properties).getClass());
            });
    }

    @Test
    void shouldResolveSystemAssignedMITokenCredential() {
        AzureGlobalProperties properties = new AzureGlobalProperties();
        properties.getCredential().setManagedIdentityEnabled(true);
        contextRunner
            .withBean(AzureGlobalProperties.class, () -> properties)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureTokenCredentialResolver.class);
                AzureTokenCredentialResolver resolver = context.getBean(AzureTokenCredentialResolver.class);
                Assertions.assertEquals(ManagedIdentityCredential.class, resolver.resolve(properties).getClass());
            });
    }

    @Test
    void shouldResolveUsernamePasswordTokenCredential() {
        AzureGlobalProperties properties = new AzureGlobalProperties();
        properties.getCredential().setUsername("test-username");
        properties.getCredential().setPassword("test-password");
        properties.getCredential().setClientId("test-client-id");
        contextRunner
            .withBean(AzureGlobalProperties.class, () -> properties)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureTokenCredentialResolver.class);
                AzureTokenCredentialResolver resolver = context.getBean(AzureTokenCredentialResolver.class);
                Assertions.assertEquals(UsernamePasswordCredential.class, resolver.resolve(properties).getClass());
            });
    }

    @Test
    void credentialBuilderFactoriesConfigured() {
        AzureGlobalProperties properties = new AzureGlobalProperties();
        contextRunner
            .withBean(AzureGlobalProperties.class, () -> properties)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureTokenCredentialResolver.class);
                assertThat(context).hasSingleBean(ClientSecretCredentialBuilderFactory.class);
                assertThat(context).hasSingleBean(ClientCertificateCredentialBuilderFactory.class);
                assertThat(context).hasSingleBean(UsernamePasswordCredentialBuilderFactory.class);
                assertThat(context).hasSingleBean(ManagedIdentityCredentialBuilderFactory.class);
            });
    }

    private static class DefaultTokenCredentialBuilderCustomizer extends TestBuilderCustomizer<DefaultAzureCredentialBuilder> {

    }

    private static class OtherBuilderCustomizer extends TestBuilderCustomizer<EventHubClientBuilder> {

    }

}
