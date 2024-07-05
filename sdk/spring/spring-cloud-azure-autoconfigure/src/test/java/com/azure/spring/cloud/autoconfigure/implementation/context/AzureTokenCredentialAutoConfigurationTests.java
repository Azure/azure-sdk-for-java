// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.context;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.ClientCertificateCredential;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.identity.ManagedIdentityCredential;
import com.azure.identity.UsernamePasswordCredential;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.spring.cloud.autoconfigure.implementation.TestBuilderCustomizer;
import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.cosmos.properties.AzureCosmosProperties;
import com.azure.spring.cloud.core.credential.AzureCredentialResolver;
import com.azure.spring.cloud.core.implementation.credential.resolver.AzureTokenCredentialResolver;
import com.azure.spring.cloud.core.implementation.factory.credential.ClientCertificateCredentialBuilderFactory;
import com.azure.spring.cloud.core.implementation.factory.credential.ClientSecretCredentialBuilderFactory;
import com.azure.spring.cloud.core.implementation.factory.credential.DefaultAzureCredentialBuilderFactory;
import com.azure.spring.cloud.core.implementation.factory.credential.ManagedIdentityCredentialBuilderFactory;
import com.azure.spring.cloud.core.implementation.factory.credential.UsernamePasswordCredentialBuilderFactory;
import com.azure.spring.cloud.service.implementation.cosmos.CosmosClientBuilderFactory;
import com.azure.spring.cloud.service.implementation.cosmos.CosmosClientProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;

import static com.azure.spring.cloud.autoconfigure.implementation.context.AzureContextUtils.DEFAULT_CREDENTIAL_TASK_EXECUTOR_BEAN_NAME;
import static com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider.CloudType.AZURE_US_GOVERNMENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
                assertTrue(credential instanceof DefaultAzureCredential);

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
                assertEquals(ClientSecretCredential.class, resolver.resolve(properties).getClass());
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
                assertEquals(ClientCertificateCredential.class, resolver.resolve(properties).getClass());
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
                assertEquals(ManagedIdentityCredential.class, resolver.resolve(properties).getClass());
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
                assertEquals(ManagedIdentityCredential.class, resolver.resolve(properties).getClass());
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
                assertEquals(UsernamePasswordCredential.class, resolver.resolve(properties).getClass());
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

    @Test
    void defaultCredentialThreadPoolShouldConfigureWhenMultiExecutorBeans() {
        contextRunner
            .withConfiguration(AutoConfigurations.of(MultiExecutorConfiguration.class))
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .run(context -> {
                assertThat(context).hasBean(DEFAULT_CREDENTIAL_TASK_EXECUTOR_BEAN_NAME);
                assertThat(context).hasSingleBean(DefaultAzureCredentialBuilderFactory.class);
                assertThat(context).hasSingleBean(ClientSecretCredentialBuilderFactory.class);
                assertThat(context).hasSingleBean(ClientCertificateCredentialBuilderFactory.class);
            });
    }

    @Test
    void defaultCredentialThreadPoolShouldNotConfigureWhenOverride() {
        contextRunner
            .withConfiguration(AutoConfigurations.of(CredentialThreadPoolConfiguration.class))
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .run(context -> {
                ThreadPoolTaskExecutor credentialTaskExecutor = (ThreadPoolTaskExecutor) context.getBean(DEFAULT_CREDENTIAL_TASK_EXECUTOR_BEAN_NAME);
                assertTrue(credentialTaskExecutor instanceof ThreadPoolTaskExecutorExtend);
                assertThat(context).hasSingleBean(DefaultAzureCredentialBuilderFactory.class);
                assertThat(context).hasSingleBean(ClientSecretCredentialBuilderFactory.class);
                assertThat(context).hasSingleBean(ClientCertificateCredentialBuilderFactory.class);
            });
    }

    @Test
    void defaultAzureCredentialSetToBuilderFactories() {
        CosmosClientBuilderFactoryExt builderFactory = new CosmosClientBuilderFactoryExt(new AzureCosmosProperties());
        contextRunner
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .withBean(CosmosClientBuilderFactoryExt.class, () -> builderFactory)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureTokenCredentialAutoConfiguration.AzureServiceClientBuilderFactoryPostProcessor.class);
                assertThat(context).hasSingleBean(TokenCredential.class);

                TokenCredential tokenCredential = context.getBean(TokenCredential.class);
                assertTrue(tokenCredential instanceof DefaultAzureCredential);
                assertEquals(builderFactory.getDefaultTokenCredential(), tokenCredential);
            });
    }

    @Test
    void defaultAzureCredentialResolverSetToBuilderFactories() {
        CosmosClientBuilderFactoryExt builderFactory = new CosmosClientBuilderFactoryExt(new AzureCosmosProperties());
        contextRunner
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .withBean(CosmosClientBuilderFactoryExt.class, () -> builderFactory)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureTokenCredentialAutoConfiguration.AzureServiceClientBuilderFactoryPostProcessor.class);
                assertThat(context).hasSingleBean(AzureTokenCredentialResolver.class);

                AzureTokenCredentialResolver tokenCredentialResolver = context.getBean(AzureTokenCredentialResolver.class);
                assertEquals(builderFactory.getAzureTokenCredentialResolver(), tokenCredentialResolver);
            });
    }

    @Test
    void globalPropertiesShouldBeHonored() {
        AzureGlobalProperties properties = new AzureGlobalProperties();
        properties.getProfile().setCloudType(AZURE_US_GOVERNMENT);
        contextRunner
            .withBean(AzureGlobalProperties.class, () -> properties)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureTokenCredentialAutoConfiguration.class);
                AzureTokenCredentialAutoConfiguration autoConfiguration = context.getBean(AzureTokenCredentialAutoConfiguration.class);
                AzureTokenCredentialAutoConfiguration.IdentityClientProperties identityClientProperties = autoConfiguration.getIdentityClientProperties();

                assertEquals(AZURE_US_GOVERNMENT, identityClientProperties.getProfile().getCloudType());
                assertEquals(AzureEnvironment.AZURE_US_GOVERNMENT.getActiveDirectoryEndpoint(),
                    identityClientProperties.getProfile().getEnvironment().getActiveDirectoryEndpoint());
            });

    }

    @Configuration
    static class MultiExecutorConfiguration {

        @Bean
        public ThreadPoolTaskExecutor executor1() {
            ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
            executor.setMaxPoolSize(1);
            executor.setCorePoolSize(1);
            executor.initialize();
            return executor;
        }

        @Primary
        @Bean
        public ThreadPoolTaskExecutor executor2() {
            ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
            executor.setMaxPoolSize(1);
            executor.setCorePoolSize(1);
            executor.initialize();
            return executor;
        }
    }

    @Configuration
    @AutoConfigureBefore(AzureTokenCredentialAutoConfiguration.class)
    @AutoConfigureAfter(TaskExecutionAutoConfiguration.class)
    static class CredentialThreadPoolConfiguration {

        @Bean(name = DEFAULT_CREDENTIAL_TASK_EXECUTOR_BEAN_NAME)
        public ThreadPoolTaskExecutor credentialTaskExecutor() {
            ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutorExtend();
            executor.setMaxPoolSize(1);
            executor.setCorePoolSize(1);
            executor.initialize();
            return executor;
        }

    }

    static class ThreadPoolTaskExecutorExtend extends ThreadPoolTaskExecutor {

    }

    private static class DefaultTokenCredentialBuilderCustomizer extends TestBuilderCustomizer<DefaultAzureCredentialBuilder> {

    }

    private static class OtherBuilderCustomizer extends TestBuilderCustomizer<EventHubClientBuilder> {

    }

    private static class CosmosClientBuilderFactoryExt extends CosmosClientBuilderFactory {

        private TokenCredential defaultTokenCredential;
        private AzureCredentialResolver<TokenCredential> azureTokenCredentialResolver;

        CosmosClientBuilderFactoryExt(CosmosClientProperties cosmosClientProperties) {
            super(cosmosClientProperties);
        }

        @Override
        public void setDefaultTokenCredential(TokenCredential defaultTokenCredential) {
            this.defaultTokenCredential = defaultTokenCredential;
        }

        @Override
        public void setTokenCredentialResolver(AzureCredentialResolver<TokenCredential> tokenCredentialResolver) {
            this.azureTokenCredentialResolver = tokenCredentialResolver;
        }

        TokenCredential getDefaultTokenCredential() {
            return defaultTokenCredential;
        }

        AzureCredentialResolver<TokenCredential> getAzureTokenCredentialResolver() {
            return azureTokenCredentialResolver;
        }
    }

}
