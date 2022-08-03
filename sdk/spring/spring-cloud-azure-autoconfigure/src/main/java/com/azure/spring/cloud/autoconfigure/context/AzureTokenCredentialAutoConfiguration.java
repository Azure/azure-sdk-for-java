// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.context;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ClientCertificateCredentialBuilder;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import com.azure.identity.UsernamePasswordCredentialBuilder;
import com.azure.spring.cloud.autoconfigure.AzureServiceConfigurationBase;
import com.azure.spring.cloud.autoconfigure.implementation.properties.core.AbstractAzureHttpConfigurationProperties;
import com.azure.spring.cloud.core.customizer.AzureServiceClientBuilderCustomizer;
import com.azure.spring.cloud.core.implementation.credential.resolver.AzureTokenCredentialResolver;
import com.azure.spring.cloud.core.implementation.factory.AbstractAzureServiceClientBuilderFactory;
import com.azure.spring.cloud.core.implementation.factory.credential.AbstractAzureCredentialBuilderFactory;
import com.azure.spring.cloud.core.implementation.factory.credential.ClientCertificateCredentialBuilderFactory;
import com.azure.spring.cloud.core.implementation.factory.credential.ClientSecretCredentialBuilderFactory;
import com.azure.spring.cloud.core.implementation.factory.credential.DefaultAzureCredentialBuilderFactory;
import com.azure.spring.cloud.core.implementation.factory.credential.ManagedIdentityCredentialBuilderFactory;
import com.azure.spring.cloud.core.implementation.factory.credential.UsernamePasswordCredentialBuilderFactory;
import com.azure.spring.cloud.core.provider.authentication.TokenCredentialOptionsProvider;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.task.TaskExecutorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.StringUtils;

import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.DEFAULT_CREDENTIAL_TASK_EXECUTOR_BEAN_NAME;
import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.DEFAULT_CREDENTIAL_THREAD_NAME_PREFIX;
import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Spring Cloud Azure default {@link TokenCredential}.
 *
 * @since 4.0.0
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(TaskExecutionAutoConfiguration.class)
public class AzureTokenCredentialAutoConfiguration extends AzureServiceConfigurationBase {

    private final IdentityClientProperties identityClientProperties;

    AzureTokenCredentialAutoConfiguration(AzureGlobalProperties azureGlobalProperties) {
        super(azureGlobalProperties);
        this.identityClientProperties = loadProperties(azureGlobalProperties, new IdentityClientProperties());
    }

    @ConditionalOnMissingBean(name = DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME)
    @Bean(name = DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME)
    @Order
    TokenCredential tokenCredential(DefaultAzureCredentialBuilderFactory factory,
                                    AzureTokenCredentialResolver resolver) {
        TokenCredential globalTokenCredential = resolver.resolve(this.identityClientProperties);
        if (globalTokenCredential != null) {
            return globalTokenCredential;
        } else {
            return factory.build().build();
        }
    }

    @Bean
    @ConditionalOnMissingBean
    DefaultAzureCredentialBuilderFactory azureCredentialBuilderFactory(
        ObjectProvider<AzureServiceClientBuilderCustomizer<DefaultAzureCredentialBuilder>> customizers,
        @Qualifier(DEFAULT_CREDENTIAL_TASK_EXECUTOR_BEAN_NAME) ThreadPoolTaskExecutor threadPoolTaskExecutor) {
        DefaultAzureCredentialBuilderFactory factory = new DefaultAzureCredentialBuilderFactory(identityClientProperties);
        factory.setExecutorService(threadPoolTaskExecutor.getThreadPoolExecutor());
        customizers.orderedStream().forEach(factory::addBuilderCustomizer);
        return factory;
    }

    @Bean
    @ConditionalOnMissingBean
    AzureTokenCredentialResolver azureTokenCredentialResolver(
        ClientSecretCredentialBuilderFactory clientSecretCredentialBuilderFactory,
        ClientCertificateCredentialBuilderFactory clientCertificateCredentialBuilderFactory,
        UsernamePasswordCredentialBuilderFactory usernamePasswordCredentialBuilderFactory,
        ManagedIdentityCredentialBuilderFactory managedIdentityCredentialBuilderFactory) {

        return new AzureTokenCredentialResolver(azureProperties -> {

            if (azureProperties.getCredential() == null) {
                return null;
            }

            final TokenCredentialOptionsProvider.TokenCredentialOptions properties = azureProperties.getCredential();
            final String tenantId = azureProperties.getProfile().getTenantId();
            final String clientId = properties.getClientId();
            final boolean isClientIdSet = StringUtils.hasText(clientId);
            final String authorityHost = azureProperties.getProfile().getEnvironment().getActiveDirectoryEndpoint();

            if (StringUtils.hasText(tenantId)) {

                if (isClientIdSet && StringUtils.hasText(properties.getClientSecret())) {
                    return clientSecretCredentialBuilderFactory.build()
                                                               .authorityHost(authorityHost)
                                                               .clientId(clientId)
                                                               .clientSecret(properties.getClientSecret())
                                                               .tenantId(tenantId)
                                                               .build();
                }

                String clientCertificatePath = properties.getClientCertificatePath();
                if (StringUtils.hasText(clientCertificatePath)) {
                    ClientCertificateCredentialBuilder builder = clientCertificateCredentialBuilderFactory
                        .build()
                        .authorityHost(authorityHost)
                        .tenantId(tenantId)
                        .clientId(clientId);

                    if (StringUtils.hasText(properties.getClientCertificatePassword())) {
                        builder.pfxCertificate(clientCertificatePath, properties.getClientCertificatePassword());
                    } else {
                        builder.pemCertificate(clientCertificatePath);
                    }

                    return builder.build();
                }
            }

            if (isClientIdSet && StringUtils.hasText(properties.getUsername())
                && StringUtils.hasText(properties.getPassword())) {
                return usernamePasswordCredentialBuilderFactory.build()
                                                               .authorityHost(authorityHost)
                                                               .username(properties.getUsername())
                                                               .password(properties.getPassword())
                                                               .clientId(clientId)
                                                               .tenantId(tenantId)
                                                               .build();
            }

            if (properties.isManagedIdentityEnabled()) {
                ManagedIdentityCredentialBuilder builder = managedIdentityCredentialBuilderFactory.build();
                if (isClientIdSet) {
                    builder.clientId(clientId);
                }
                return builder.build();
            }
            return null;
        });
    }

    @Bean
    @ConditionalOnMissingBean
    ClientSecretCredentialBuilderFactory clientSecretCredentialBuilderFactory(
        @Qualifier(DEFAULT_CREDENTIAL_TASK_EXECUTOR_BEAN_NAME) ThreadPoolTaskExecutor threadPoolTaskExecutor,
        ObjectProvider<AzureServiceClientBuilderCustomizer<ClientSecretCredentialBuilder>> customizers) {
        ClientSecretCredentialBuilderFactory factory = new ClientSecretCredentialBuilderFactory(identityClientProperties);
        factory.setExecutorService(threadPoolTaskExecutor.getThreadPoolExecutor());
        customizers.orderedStream().forEach(factory::addBuilderCustomizer);
        return factory;
    }

    @Bean
    @ConditionalOnMissingBean
    ClientCertificateCredentialBuilderFactory clientCertificateCredentialBuilderFactory(
        @Qualifier(DEFAULT_CREDENTIAL_TASK_EXECUTOR_BEAN_NAME) ThreadPoolTaskExecutor threadPoolTaskExecutor,
        ObjectProvider<AzureServiceClientBuilderCustomizer<ClientCertificateCredentialBuilder>> customizers) {
        ClientCertificateCredentialBuilderFactory factory = new ClientCertificateCredentialBuilderFactory(identityClientProperties);
        factory.setExecutorService(threadPoolTaskExecutor.getThreadPoolExecutor());
        customizers.orderedStream().forEach(factory::addBuilderCustomizer);
        return factory;
    }

    @Bean
    @ConditionalOnMissingBean
    ManagedIdentityCredentialBuilderFactory managedIdentityCredentialBuilderFactory(
        ObjectProvider<AzureServiceClientBuilderCustomizer<ManagedIdentityCredentialBuilder>> customizers) {

        ManagedIdentityCredentialBuilderFactory factory = new ManagedIdentityCredentialBuilderFactory(identityClientProperties);

        customizers.orderedStream().forEach(factory::addBuilderCustomizer);

        return factory;
    }

    @Bean
    @ConditionalOnMissingBean
    UsernamePasswordCredentialBuilderFactory usernamePasswordCredentialBuilderFactory(
        ObjectProvider<AzureServiceClientBuilderCustomizer<UsernamePasswordCredentialBuilder>> customizers) {

        UsernamePasswordCredentialBuilderFactory factory = new UsernamePasswordCredentialBuilderFactory(identityClientProperties);

        customizers.orderedStream().forEach(factory::addBuilderCustomizer);

        return factory;
    }

    /**
     * The BeanPostProcessor to apply the default token credential to all service client builder factories.
     * @return the BPP.
     */
    @Bean
    static AzureServiceClientBuilderFactoryPostProcessor builderFactoryBeanPostProcessor() {
        return new AzureServiceClientBuilderFactoryPostProcessor();
    }

    @Bean(name = DEFAULT_CREDENTIAL_TASK_EXECUTOR_BEAN_NAME)
    @ConditionalOnMissingBean(name = DEFAULT_CREDENTIAL_TASK_EXECUTOR_BEAN_NAME)
    ThreadPoolTaskExecutor credentialTaskExecutor() {
        return new TaskExecutorBuilder()
            .corePoolSize(8)
            .allowCoreThreadTimeOut(true)
            .threadNamePrefix(DEFAULT_CREDENTIAL_THREAD_NAME_PREFIX)
            .build();
    }

    /**
     * Apply the default token credential to service client builder factory.
     */
    static class AzureServiceClientBuilderFactoryPostProcessor implements BeanPostProcessor, BeanFactoryAware {

        private BeanFactory beanFactory;

        @Override
        @SuppressWarnings({ "rawtypes", "unchecked" })
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            if (bean instanceof AbstractAzureCredentialBuilderFactory) {
                return bean;
            }

            if (bean instanceof AbstractAzureServiceClientBuilderFactory
                && beanFactory.containsBean(DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME)) {
                AbstractAzureServiceClientBuilderFactory factory = (AbstractAzureServiceClientBuilderFactory) bean;
                factory.setDefaultTokenCredential(
                    (TokenCredential) beanFactory.getBean(DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME));
                factory.setTokenCredentialResolver(beanFactory.getBean(AzureTokenCredentialResolver.class));
            }
            return bean;
        }

        @Override
        public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
            this.beanFactory = beanFactory;
        }
    }

    static class IdentityClientProperties extends AbstractAzureHttpConfigurationProperties {

    }
}
