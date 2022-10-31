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
import com.azure.spring.cloud.core.credential.AzureCredentialResolver;
import com.azure.spring.cloud.core.customizer.AzureServiceClientBuilderCustomizer;
import com.azure.spring.cloud.core.implementation.credential.resolver.AzureTokenCredentialResolver;
import com.azure.spring.cloud.core.implementation.credential.resolver.ClientCertificateCredentialResolver;
import com.azure.spring.cloud.core.implementation.credential.resolver.ClientSecretCredentialResolver;
import com.azure.spring.cloud.core.implementation.credential.resolver.DefaultAzureCredentialResolver;
import com.azure.spring.cloud.core.implementation.credential.resolver.DefaultTokenCredentialResolver;
import com.azure.spring.cloud.core.implementation.credential.resolver.ManagedIdentityCredentialResolver;
import com.azure.spring.cloud.core.implementation.credential.resolver.UsernamePasswordCredentialResolver;
import com.azure.spring.cloud.core.implementation.factory.AbstractAzureServiceClientBuilderFactory;
import com.azure.spring.cloud.core.implementation.factory.credential.AbstractAzureCredentialBuilderFactory;
import com.azure.spring.cloud.core.implementation.factory.credential.ClientCertificateCredentialBuilderFactory;
import com.azure.spring.cloud.core.implementation.factory.credential.ClientSecretCredentialBuilderFactory;
import com.azure.spring.cloud.core.implementation.factory.credential.DefaultAzureCredentialBuilderFactory;
import com.azure.spring.cloud.core.implementation.factory.credential.ManagedIdentityCredentialBuilderFactory;
import com.azure.spring.cloud.core.implementation.factory.credential.UsernamePasswordCredentialBuilderFactory;
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

import java.util.ArrayList;
import java.util.List;

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
    TokenCredential tokenCredential(ClientSecretCredentialBuilderFactory clientSecretCredentialBuilderFactory,
                                    ClientCertificateCredentialBuilderFactory clientCertificateCredentialBuilderFactory,
                                    UsernamePasswordCredentialBuilderFactory usernamePasswordCredentialBuilderFactory,
                                    ManagedIdentityCredentialBuilderFactory managedIdentityCredentialBuilderFactory,
                                    DefaultAzureCredentialBuilderFactory defaultAzureCredentialBuilderFactory) {

        List<AzureCredentialResolver<TokenCredential>> resolverList = new ArrayList<>();
        resolverList.add(new ClientSecretCredentialResolver(clientSecretCredentialBuilderFactory));
        resolverList.add(new ClientCertificateCredentialResolver(clientCertificateCredentialBuilderFactory));
        resolverList.add(new UsernamePasswordCredentialResolver(usernamePasswordCredentialBuilderFactory));
        resolverList.add(new ManagedIdentityCredentialResolver(managedIdentityCredentialBuilderFactory));
        resolverList.add(new DefaultAzureCredentialResolver(defaultAzureCredentialBuilderFactory));

        AzureTokenCredentialResolver resolver = new AzureTokenCredentialResolver(resolverList);
        return resolver.resolve(this.identityClientProperties);
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
        @Qualifier(DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME) TokenCredential defaultTokenCredential,
        ClientSecretCredentialBuilderFactory clientSecretCredentialBuilderFactory,
        ClientCertificateCredentialBuilderFactory clientCertificateCredentialBuilderFactory,
        UsernamePasswordCredentialBuilderFactory usernamePasswordCredentialBuilderFactory,
        ManagedIdentityCredentialBuilderFactory managedIdentityCredentialBuilderFactory,
        DefaultAzureCredentialBuilderFactory defaultAzureCredentialBuilderFactory) {

        List<AzureCredentialResolver<TokenCredential>> resolverList = new ArrayList<>();
        resolverList.add(new ClientSecretCredentialResolver(clientSecretCredentialBuilderFactory));
        resolverList.add(new ClientCertificateCredentialResolver(clientCertificateCredentialBuilderFactory));
        resolverList.add(new UsernamePasswordCredentialResolver(usernamePasswordCredentialBuilderFactory));
        resolverList.add(new ManagedIdentityCredentialResolver(managedIdentityCredentialBuilderFactory));
        resolverList.add(new DefaultTokenCredentialResolver(defaultTokenCredential, this.identityClientProperties));
        resolverList.add(new DefaultAzureCredentialResolver(defaultAzureCredentialBuilderFactory));

        AzureTokenCredentialResolver azureTokenCredentialResolver = new AzureTokenCredentialResolver(resolverList);

        return azureTokenCredentialResolver;
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

    IdentityClientProperties getIdentityClientProperties() {
        return identityClientProperties;
    }
}
