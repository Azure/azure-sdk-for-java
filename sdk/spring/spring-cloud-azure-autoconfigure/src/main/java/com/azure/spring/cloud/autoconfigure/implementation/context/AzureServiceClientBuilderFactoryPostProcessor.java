// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.context;

import com.azure.core.credential.TokenCredential;
import com.azure.spring.cloud.core.implementation.credential.resolver.AzureTokenCredentialResolver;
import com.azure.spring.cloud.core.implementation.factory.AbstractAzureServiceClientBuilderFactory;
import com.azure.spring.cloud.core.implementation.factory.credential.AbstractAzureCredentialBuilderFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanPostProcessor;

import static com.azure.spring.cloud.autoconfigure.implementation.context.AzureContextUtils.DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME;

/**
 * {@link BeanPostProcessor} for {@link AbstractAzureServiceClientBuilderFactory} to configure default credential and resolver.
 */
class AzureServiceClientBuilderFactoryPostProcessor implements BeanPostProcessor, BeanFactoryAware {

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
