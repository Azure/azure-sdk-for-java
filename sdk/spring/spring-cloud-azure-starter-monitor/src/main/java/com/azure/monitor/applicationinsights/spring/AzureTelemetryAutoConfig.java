// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.applicationinsights.spring;

import io.opentelemetry.instrumentation.spring.autoconfigure.OpenTelemetryAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Auto config for Azure Telemetry
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(OpenTelemetryAutoConfiguration.class)
@Import({AzureTelemetryConfig.class, AzureTelemetryActivationConfig.class})
public class AzureTelemetryAutoConfig {
}
