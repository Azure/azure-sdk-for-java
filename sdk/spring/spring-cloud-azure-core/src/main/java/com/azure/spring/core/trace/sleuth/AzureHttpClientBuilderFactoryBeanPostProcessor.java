// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.trace.sleuth;

import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.spring.core.factory.AbstractAzureHttpClientBuilderFactory;
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
public class AzureHttpClientBuilderFactoryBeanPostProcessor
        implements BeanPostProcessor, Ordered, BeanFactoryAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureHttpClientBuilderFactoryBeanPostProcessor.class);
    public static final String DEFAULT_SLEUTH_HTTP_POLICY_BEAN_NAME = "AzureSleuthHttpPolicy";

    private BeanFactory beanFactory;

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    @SuppressWarnings({ "rawtypes"})
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (!(bean instanceof AbstractAzureHttpClientBuilderFactory)) {
            return bean;
        }

        if (beanFactory.containsBean(DEFAULT_SLEUTH_HTTP_POLICY_BEAN_NAME)) {
            HttpPipelinePolicy policy = (HttpPipelinePolicy) beanFactory.getBean(DEFAULT_SLEUTH_HTTP_POLICY_BEAN_NAME);
            AbstractAzureHttpClientBuilderFactory builderFactory = (AbstractAzureHttpClientBuilderFactory) bean;
            builderFactory.addHttpPipelinePolicy(policy);
            LOGGER.debug("Added the Sleuth http pipeline policy to {} builder.", bean.getClass());
        }
        return bean;
    }
}
