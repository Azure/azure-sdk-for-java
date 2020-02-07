/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.autoconfigure.metrics;

import io.micrometer.azuremonitor.AzureMonitorConfig;
import io.micrometer.azuremonitor.AzureMonitorMeterRegistry;
import io.micrometer.core.instrument.Clock;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * Tests for autoconfiguration of {@link AzureMonitorMetricsExportAutoConfiguration}
 *
 * @author Dhaval Doshi
 */
public class AzureMonitorMetricsExportAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations
                    .of(AzureMonitorMetricsExportAutoConfiguration.class));

    @Test
    public void backsOffWithoutAClock() {
        this.contextRunner.run((context) -> assertThat(context)
                .doesNotHaveBean(AzureMonitorMeterRegistry.class));
    }

    @Test
    @Ignore("Somewhere in the class path there is xml from where the config is picked for AI")
    public void failsWithoutAnApiKey() {
        this.contextRunner.withUserConfiguration(BaseConfiguration.class)
                .run((context) -> assertThat(context).hasFailed());
    }

    @Test
    public void autoConfiguresConfigAndMeterRegistry() {
        this.contextRunner.withUserConfiguration(BaseConfiguration.class)
                .withPropertyValues(
                        "management.metrics.export.azuremonitor.instrumentation-key=fakekey")
                .run((context) -> assertThat(context)
                        .hasSingleBean(AzureMonitorMeterRegistry.class)
                        .hasSingleBean(AzureMonitorConfig.class));
    }

    @Test
    public void autoConfigurationCanBeDisabled() {
        this.contextRunner.withUserConfiguration(BaseConfiguration.class)
                .withPropertyValues(
                        "management.metrics.export.azuremonitor.enabled=false")
                .run((context) -> assertThat(context)
                        .doesNotHaveBean(AzureMonitorMeterRegistry.class)
                        .doesNotHaveBean(AzureMonitorConfig.class));
    }

    @Test
    public void allowsCustomConfigToBeUsed() {
        this.contextRunner.withUserConfiguration(CustomConfigConfiguration.class)
                .run((context) -> assertThat(context)
                        .hasSingleBean(AzureMonitorMeterRegistry.class)
                        .hasSingleBean(AzureMonitorConfig.class).hasBean("customConfig"));
    }

    @Test
    public void allowsCustomRegistryToBeUsed() {
        this.contextRunner.withUserConfiguration(CustomRegistryConfiguration.class)
                .withPropertyValues(
                        "management.metrics.export.azuremonitor.instrumentation-key=fakekey")
                .run((context) -> assertThat(context)
                        .hasSingleBean(AzureMonitorMeterRegistry.class)
                        .hasBean("customRegistry")
                        .hasSingleBean(AzureMonitorConfig.class));
    }

    @Test
    public void stopsMeterRegistryWhenContextIsClosed() {
        this.contextRunner.withUserConfiguration(BaseConfiguration.class)
                .withPropertyValues(
                        "management.metrics.export.azuremonitor.instrumentation-key=fakekey")
                .run((context) -> {
                    final AzureMonitorMeterRegistry registry = spyOnDisposableBean(
                            AzureMonitorMeterRegistry.class, context);
                    context.close();
                    verify(registry).stop();
                });
    }

    @SuppressWarnings("unchecked")
    private <T> T spyOnDisposableBean(Class<T> type,
                                      AssertableApplicationContext context) {
        final String[] names = context.getBeanNamesForType(type);
        assertThat(names).hasSize(1);
        final String registryBeanName = names[0];
        final Map<String, Object> disposableBeans = (Map<String, Object>) ReflectionTestUtils
                .getField(context.getAutowireCapableBeanFactory(), "disposableBeans");
        final Object registryAdapter = disposableBeans.get(registryBeanName);
        final T registry = (T) spy(ReflectionTestUtils.getField(registryAdapter, "bean"));
        ReflectionTestUtils.setField(registryAdapter, "bean", registry);
        return registry;
    }

    @Configuration
    static class BaseConfiguration {

        @Bean
        public Clock clock() {
            return Clock.SYSTEM;
        }

    }

    @Configuration
    @Import(BaseConfiguration.class)
    static class CustomConfigConfiguration {

        @Bean
        public AzureMonitorConfig customConfig() {
            return new AzureMonitorConfig() {

                @Override
                public String get(String k) {
                    if ("azuremonitor.instrumentation-key".equals(k)) {
                        return "12345";
                    }
                    return null;
                }

            };
        }

    }

    @Configuration
    @Import(BaseConfiguration.class)
    static class CustomRegistryConfiguration {

        @Bean
        public AzureMonitorMeterRegistry customRegistry(AzureMonitorConfig config,
                                                        Clock clock) {
            return AzureMonitorMeterRegistry.builder(config)
                    .clock(clock)
                    .build();
        }
    }
}
