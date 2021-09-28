// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.trace.sleuth;

import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.spring.tracing.sleuth.SleuthHttpPolicy;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.cloud.sleuth.autoconfig.brave.BraveAutoConfiguration;
import org.springframework.cloud.sleuth.autoconfig.instrument.web.TraceWebAutoConfiguration;

import static com.azure.spring.cloud.autoconfigure.trace.sleuth.AzureSleuthAutoConfiguration.DEFAULT_SLEUTH_HTTP_POLICY_BEAN_NAME;
import static org.assertj.core.api.Assertions.assertThat;

public class AzureSleuthAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureSleuthAutoConfiguration.class));

    @Test
    public void configureWithoutAzureSleuthDependency() {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(SleuthHttpPolicy.class))
            .run(context -> assertThat(context).doesNotHaveBean(HttpPipelinePolicy.class));
    }

    @Test
    public void configureWithoutSpringSleuthDependency() {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(Tracer.class))
            .run(context -> assertThat(context).doesNotHaveBean(HttpPipelinePolicy.class));
    }

    @Test
    public void configureAzureSleuthBean() {
        this.contextRunner
            .withUserConfiguration(BraveAutoConfiguration.class, TraceWebAutoConfiguration.class)
            .run(context -> {
                assertThat(context).hasBean(DEFAULT_SLEUTH_HTTP_POLICY_BEAN_NAME);
                assertThat(context).hasSingleBean(AzureHttpClientBuilderFactoryBeanPostProcessor.class);
            });
    }
}
