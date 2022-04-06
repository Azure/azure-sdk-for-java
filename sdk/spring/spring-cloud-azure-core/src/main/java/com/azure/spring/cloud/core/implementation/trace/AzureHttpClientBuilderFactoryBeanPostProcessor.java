// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.trace;

import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.spring.cloud.core.implementation.factory.AbstractAzureHttpClientBuilderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;

/**
 * Apply the http pipeline policy to service client builder based on HTTP protocol.
 */
public class AzureHttpClientBuilderFactoryBeanPostProcessor implements BeanPostProcessor, Ordered, BeanFactoryAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureHttpClientBuilderFactoryBeanPostProcessor.class);

    private final String httpPolicyBeanName;
    private BeanFactory beanFactory;

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    /**
     * Create an {@link AzureHttpClientBuilderFactoryBeanPostProcessor} instance with a http policy bean name.
     * @param httpPolicyBeanName the bean name for the http policy.
     */
    public AzureHttpClientBuilderFactoryBeanPostProcessor(String httpPolicyBeanName) {
        this.httpPolicyBeanName = httpPolicyBeanName;
    }

    @Override
    @SuppressWarnings({ "rawtypes"})
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (!(bean instanceof AbstractAzureHttpClientBuilderFactory)) {
            return bean;
        }

        if (beanFactory.containsBean(this.httpPolicyBeanName)) {
            HttpPipelinePolicy policy = (HttpPipelinePolicy) beanFactory.getBean(this.httpPolicyBeanName);
            AbstractAzureHttpClientBuilderFactory builderFactory = (AbstractAzureHttpClientBuilderFactory) bean;
            builderFactory.addHttpPipelinePolicy(policy);
            LOGGER.debug("Added the Sleuth http pipeline policy to {} builder.", bean.getClass());
        }
        return bean;
    }
}
