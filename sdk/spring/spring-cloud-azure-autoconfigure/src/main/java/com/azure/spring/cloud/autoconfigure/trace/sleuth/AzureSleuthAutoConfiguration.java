// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.trace.sleuth;

import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.spring.cloud.core.implementation.trace.AzureHttpClientBuilderFactoryBeanPostProcessor;
import com.azure.spring.cloud.trace.sleuth.SleuthHttpPolicy;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.cloud.sleuth.propagation.Propagator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

/**
 * Auto-configuration for an Azure SDK Sleuth {@link Tracer}.
 */
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({ SleuthHttpPolicy.class, Tracer.class })
@ConditionalOnProperty(value = "spring.sleuth.enabled", matchIfMissing = true)
public class AzureSleuthAutoConfiguration {

    public static final String DEFAULT_SLEUTH_HTTP_POLICY_BEAN_NAME = "AzureSleuthHttpPolicy";

    /**
     * Autoconfigure the {@link HttpPipelinePolicy} for sleuth usage.
     * @param tracer the sleuth {@link Tracer}.
     * @param propagator the sleuth {@link Propagator}
     * @return the http pipeline policy
     */
    @Bean(name = DEFAULT_SLEUTH_HTTP_POLICY_BEAN_NAME)
    @ConditionalOnMissingBean(name = DEFAULT_SLEUTH_HTTP_POLICY_BEAN_NAME)
    public HttpPipelinePolicy azureSleuthHttpPolicy(Tracer tracer, Propagator propagator) {
        return new SleuthHttpPolicy(tracer, propagator);
    }

    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @Bean
    @ConditionalOnMissingBean
    AzureHttpClientBuilderFactoryBeanPostProcessor httpClientBuilderFactoryBeanPostProcessor() {
        return new AzureHttpClientBuilderFactoryBeanPostProcessor(DEFAULT_SLEUTH_HTTP_POLICY_BEAN_NAME);
    }
}
