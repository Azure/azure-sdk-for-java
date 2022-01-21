// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.context;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.spring.cloud.autoconfigure.AzureServiceConfigurationBase;
import com.azure.spring.cloud.autoconfigure.properties.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.properties.core.AbstractAzureHttpConfigurationProperties;
import com.azure.spring.core.customizer.AzureServiceClientBuilderCustomizer;
import com.azure.spring.core.factory.AbstractAzureServiceClientBuilderFactory;
import com.azure.spring.core.factory.credential.AbstractAzureCredentialBuilderFactory;
import com.azure.spring.core.factory.credential.DefaultAzureCredentialBuilderFactory;
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

import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME;

/**
 * Auto-configuration for Azure Spring default token credential.
 */
@Configuration(proxyBeanMethods = false)
public class AzureDefaultTokenCredentialAutoConfiguration extends AzureServiceConfigurationBase {

    private final IdentityClientProperties identityClientProperties;

    public AzureDefaultTokenCredentialAutoConfiguration(AzureGlobalProperties azureGlobalProperties) {
        super(azureGlobalProperties);
        this.identityClientProperties = loadProperties(azureGlobalProperties, new IdentityClientProperties());
    }

    @ConditionalOnMissingBean(name = DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME)
    @Bean(name = DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME)
    @Order
    public TokenCredential tokenCredential(AbstractAzureCredentialBuilderFactory<DefaultAzureCredentialBuilder> factory) {
        return factory.build().build();
    }

    @Bean
    @ConditionalOnMissingBean
    public AbstractAzureCredentialBuilderFactory<DefaultAzureCredentialBuilder> azureCredentialBuilderFactory(
        ObjectProvider<AzureServiceClientBuilderCustomizer<DefaultAzureCredentialBuilder>> customizers,
        ObjectProvider<ThreadPoolTaskExecutor> threadPoolTaskExecutors) {
        DefaultAzureCredentialBuilderFactory factory = new DefaultAzureCredentialBuilderFactory(identityClientProperties);

        threadPoolTaskExecutors.ifAvailable(tpe -> factory.setExecutorService(tpe.getThreadPoolExecutor()));
        customizers.orderedStream().forEach(factory::addBuilderCustomizer);

        return factory;
    }

    @Bean
    public AzureServiceClientBuilderFactoryPostProcessor builderFactoryBeanPostProcessor() {
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
