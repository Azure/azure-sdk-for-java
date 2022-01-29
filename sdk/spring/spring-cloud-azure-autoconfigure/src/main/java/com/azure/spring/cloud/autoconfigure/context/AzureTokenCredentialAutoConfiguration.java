// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.context;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ClientCertificateCredentialBuilder;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import com.azure.spring.cloud.autoconfigure.AzureServiceConfigurationBase;
import com.azure.spring.cloud.autoconfigure.properties.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.properties.core.AbstractAzureHttpConfigurationProperties;
import com.azure.spring.core.aware.authentication.TokenCredentialAware;
import com.azure.spring.core.customizer.AzureServiceClientBuilderCustomizer;
import com.azure.spring.core.factory.AbstractAzureServiceClientBuilderFactory;
import com.azure.spring.core.factory.credential.AbstractAzureCredentialBuilderFactory;
import com.azure.spring.core.factory.credential.ClientCertificateCredentialBuilderFactory;
import com.azure.spring.core.factory.credential.ClientSecretCredentialBuilderFactory;
import com.azure.spring.core.factory.credential.DefaultAzureCredentialBuilderFactory;
import com.azure.spring.core.factory.credential.ManagedIdentityCredentialBuilderFactory;
import com.azure.spring.core.implementation.credential.resolver.AzureTokenCredentialResolver;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.StringUtils;

import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME;

/**
 * Auto-configuration for Azure Spring default token credential.
 */
@Configuration(proxyBeanMethods = false)
public class AzureTokenCredentialAutoConfiguration extends AzureServiceConfigurationBase {

    private final IdentityClientProperties identityClientProperties;

    public AzureTokenCredentialAutoConfiguration(AzureGlobalProperties azureGlobalProperties) {
        super(azureGlobalProperties);
        this.identityClientProperties = loadProperties(azureGlobalProperties, new IdentityClientProperties());
    }

    @ConditionalOnMissingBean(name = DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME)
    @Bean(name = DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME)
    @Order
    public TokenCredential tokenCredential(DefaultAzureCredentialBuilderFactory factory) {
        return factory.build().build();
    }

    @Bean
    @ConditionalOnMissingBean
    public DefaultAzureCredentialBuilderFactory azureCredentialBuilderFactory(
        ObjectProvider<AzureServiceClientBuilderCustomizer<DefaultAzureCredentialBuilder>> customizers,
        ObjectProvider<ThreadPoolTaskExecutor> threadPoolTaskExecutors) {
        DefaultAzureCredentialBuilderFactory factory = new DefaultAzureCredentialBuilderFactory(identityClientProperties);

        threadPoolTaskExecutors.ifAvailable(tpe -> factory.setExecutorService(tpe.getThreadPoolExecutor()));
        customizers.orderedStream().forEach(factory::addBuilderCustomizer);

        return factory;
    }

    @Bean
    @ConditionalOnMissingBean
    AzureTokenCredentialResolver azureTokenCredentialResolver(
        ClientSecretCredentialBuilderFactory clientSecretCredentialBuilderFactory,
        ClientCertificateCredentialBuilderFactory clientCertificateCredentialBuilderFactory,
        ManagedIdentityCredentialBuilderFactory managedIdentityCredentialBuilderFactory) {

        return new AzureTokenCredentialResolver(properties -> {

            if (properties.getCredential() == null) {
                return null;
            }

            TokenCredential result = null;

            final TokenCredentialAware.TokenCredential credentialProperties = properties.getCredential();
            final String tenantId = properties.getProfile().getTenantId();
            if (StringUtils.hasText(tenantId)
                && StringUtils.hasText(credentialProperties.getClientId())
                && StringUtils.hasText(credentialProperties.getClientSecret())) {
                result = clientSecretCredentialBuilderFactory.build()
                    .clientId(credentialProperties.getClientId())
                    .clientSecret(credentialProperties.getClientSecret())
                    .tenantId(tenantId)
                    .build();
            }

            if (StringUtils.hasText(tenantId)
                && StringUtils.hasText(credentialProperties.getClientCertificatePath())) {
                ClientCertificateCredentialBuilder builder = clientCertificateCredentialBuilderFactory
                        .build().tenantId(tenantId)
                        .clientId(credentialProperties.getClientId());

                if (StringUtils.hasText(credentialProperties.getClientCertificatePassword())) {
                    builder.pfxCertificate(credentialProperties.getClientCertificatePath(),
                        credentialProperties.getClientCertificatePassword());
                } else {
                    builder.pemCertificate(credentialProperties.getClientCertificatePath());
                }

                result = builder.build();
            }

            if (credentialProperties.getManagedIdentityClientId() != null) {
                result = managedIdentityCredentialBuilderFactory
                    .build()
                    .clientId(credentialProperties.getManagedIdentityClientId())
                    .build();
            }
            return result;
        });
    }

    @Bean
    @ConditionalOnMissingBean
    public ClientSecretCredentialBuilderFactory clientSecretCredentialBuilderFactory(
        ObjectProvider<ThreadPoolTaskExecutor> threadPoolTaskExecutors,
        ObjectProvider<AzureServiceClientBuilderCustomizer<ClientSecretCredentialBuilder>> customizers) {

        ClientSecretCredentialBuilderFactory factory = new ClientSecretCredentialBuilderFactory(identityClientProperties);

        threadPoolTaskExecutors.ifAvailable(tpe -> factory.setExecutorService(tpe.getThreadPoolExecutor()));
        customizers.orderedStream().forEach(factory::addBuilderCustomizer);

        return factory;
    }

    @Bean
    @ConditionalOnMissingBean
    public ClientCertificateCredentialBuilderFactory clientCertificateCredentialBuilderFactory(
        ObjectProvider<ThreadPoolTaskExecutor> threadPoolTaskExecutors,
        ObjectProvider<AzureServiceClientBuilderCustomizer<ClientCertificateCredentialBuilder>> customizers) {

        ClientCertificateCredentialBuilderFactory factory = new ClientCertificateCredentialBuilderFactory(identityClientProperties);

        threadPoolTaskExecutors.ifAvailable(tpe -> factory.setExecutorService(tpe.getThreadPoolExecutor()));
        customizers.orderedStream().forEach(factory::addBuilderCustomizer);

        return factory;
    }

    @Bean
    @ConditionalOnMissingBean
    public ManagedIdentityCredentialBuilderFactory managedIdentityCredentialBuilderFactory(
        ObjectProvider<AzureServiceClientBuilderCustomizer<ManagedIdentityCredentialBuilder>> customizers) {

        ManagedIdentityCredentialBuilderFactory factory = new ManagedIdentityCredentialBuilderFactory(identityClientProperties);

        customizers.orderedStream().forEach(factory::addBuilderCustomizer);

        return factory;
    }

    @Bean
    public static AzureServiceClientBuilderFactoryPostProcessor builderFactoryBeanPostProcessor() {
        return new AzureServiceClientBuilderFactoryPostProcessor();
    }

    /**
     * Apply the default token credential to service client builder factory.
     */
    static class AzureServiceClientBuilderFactoryPostProcessor implements BeanPostProcessor, BeanFactoryAware {

        private BeanFactory beanFactory;

        @Override
        @SuppressWarnings("rawtypes")
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            if (bean instanceof AbstractAzureCredentialBuilderFactory) {
                return bean;
            }

            if (bean instanceof AbstractAzureServiceClientBuilderFactory
                && beanFactory.containsBean(DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME)) {
                ((AbstractAzureServiceClientBuilderFactory) bean).setDefaultTokenCredential(
                    (TokenCredential) beanFactory.getBean(DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME));
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
