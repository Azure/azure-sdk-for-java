// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.context;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.spring.cloud.autoconfigure.properties.AzureGlobalProperties;
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

import java.util.List;

import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME;

/**
 * Auto-configuration for Azure Spring default token credential.
 */
@Configuration(proxyBeanMethods = false)
public class AzureDefaultTokenCredentialAutoConfiguration {

    private final AzureGlobalProperties azureGlobalProperties;

    public AzureDefaultTokenCredentialAutoConfiguration(AzureGlobalProperties azureGlobalProperties) {
        this.azureGlobalProperties = azureGlobalProperties;
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
        ObjectProvider<List<AzureServiceClientBuilderCustomizer<DefaultAzureCredentialBuilder>>> customizers,
        ObjectProvider<ThreadPoolTaskExecutor> threadPoolTaskExecutors) {
        DefaultAzureCredentialBuilderFactory factory = new DefaultAzureCredentialBuilderFactory(azureGlobalProperties);

        threadPoolTaskExecutors.ifAvailable(tpe -> factory.setExecutorService(tpe.getThreadPoolExecutor()));
        customizers.ifAvailable(cs -> cs.forEach(factory::addBuilderCustomizer));

        return factory;
    }

    @Bean
    public AzureServiceClientBuilderFactoryPostProcessor builderFactoryBeanPostProcessor() {
        return new AzureServiceClientBuilderFactoryPostProcessor();
    }

    static class AzureServiceClientBuilderFactoryPostProcessor implements BeanPostProcessor, BeanFactoryAware {

        private BeanFactory beanFactory;

        @SuppressWarnings("rawtypes")
        @Override
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
}
