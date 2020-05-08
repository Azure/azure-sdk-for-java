// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.autoconfigure.metrics;

import com.microsoft.applicationinsights.TelemetryConfiguration;
import io.micrometer.azuremonitor.AzureMonitorConfig;
import io.micrometer.azuremonitor.AzureMonitorMeterRegistry;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.util.StringUtils;
import org.springframework.boot.actuate.autoconfigure.metrics.CompositeMeterRegistryAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.export.simple.SimpleMetricsExportAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-Configuration for exporting metrics to Azure Application Insights.
 *
 * @author Dhaval Doshi
 */
@Configuration
@AutoConfigureBefore({CompositeMeterRegistryAutoConfiguration.class,
        SimpleMetricsExportAutoConfiguration.class})
@AutoConfigureAfter(MetricsAutoConfiguration.class)
@ConditionalOnBean(Clock.class)
@ConditionalOnClass(AzureMonitorMeterRegistry.class)
@ConditionalOnResource(resources = "classpath:metrics.enable.config")
@ConditionalOnProperty(prefix = "management.metrics.export.azuremonitor",
        name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(AzureMonitorProperties.class)
public class AzureMonitorMetricsExportAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AzureMonitorConfig azureConfig(AzureMonitorProperties properties) {
        return new AzureMonitorPropertiesConfigAdapter(properties);
    }

    /**
     * This bean is already available when the
     * <a href="https://github.com/Microsoft/ApplicationInsights-Java/tree/master/azure-application-insights-spring-boot-starter">Azure Application Insights starter</a>
     * is present.
     *
     * @param config Azure monitor config
     * @return telemetry configuration
     */
    @Bean
    @ConditionalOnMissingBean
    public TelemetryConfiguration telemetryConfiguration(AzureMonitorConfig config) {
        // Gets the active instance of TelemetryConfiguration either created by starter or xml
        final TelemetryConfiguration telemetryConfiguration = TelemetryConfiguration.getActive();
        if (StringUtils.isEmpty(telemetryConfiguration.getInstrumentationKey())) {
            telemetryConfiguration.setInstrumentationKey(config.instrumentationKey());
        }
        return telemetryConfiguration;
    }

    @Bean
    @ConditionalOnMissingBean
    public AzureMonitorMeterRegistry azureMeterRegistry(AzureMonitorConfig config,
                                                        TelemetryConfiguration configuration, Clock clock) {
        return AzureMonitorMeterRegistry.builder(config)
                .clock(clock)
                .telemetryConfiguration(configuration)
                .build();
    }
}
