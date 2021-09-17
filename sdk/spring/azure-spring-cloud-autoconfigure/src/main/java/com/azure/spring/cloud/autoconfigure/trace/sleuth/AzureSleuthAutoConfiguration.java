// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.trace.sleuth;

import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.spring.tracing.sleuth.SleuthHttpPolicy;
import com.azure.spring.tracing.sleuth.SleuthTracer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration for an Azure SDK Sleuth {@link Tracer}.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({ SleuthTracer.class, Tracer.class })
@ConditionalOnProperty(value = "spring.sleuth.enabled", matchIfMissing = true)
public class AzureSleuthAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public HttpPipelinePolicy httpPipelinePolicy(Tracer tracer) {
        return new SleuthHttpPolicy(tracer);
    }
}
