// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.applicationinsights.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import java.util.Collections;
import java.util.Map;

class PropertiesPostProcessor implements EnvironmentPostProcessor, Ordered {
    private static final Logger LOG = LoggerFactory.getLogger(PropertiesPostProcessor.class);

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {

        if (starterHasToBeDisabled(environment)) {
            // To log once the logging system is initialized in the Spring Boot application
            application.addInitializers(applicationContext -> LOG.warn("The spring-cloud-azure-starter-monitor Spring starter is disabled because the Application Insights Java agent is enabled."
                + " You can remove this message by adding the otel.sdk.disabled=true property."));
        }

        Map<String, Object> newProperties = buildNewProperties(environment);
        PropertySource<?> propertySource = new MapPropertySource("newPropertiesForSpringMonitor", newProperties);

        MutablePropertySources propertySources = environment.getPropertySources();
        propertySources.addLast(propertySource);
    }

    private static Map<String, Object> buildNewProperties(ConfigurableEnvironment environment) {
        if (starterHasToBeDisabled(environment)) {
            return Collections.singletonMap("otel.sdk.disabled", true); // Disable the Spring Monitor starter and the OTel starter;
        }
        return Collections.singletonMap("otel.exporter.otlp.enabled", false); // Override the otel.exporter.otlp.enabled property
    }

    private static boolean starterHasToBeDisabled(ConfigurableEnvironment environment) {
        return !isNativeRuntimeExecution()
            && !isStarterDisabled(environment) && applicationInsightsAgentIsAttached();
    }

    private static boolean isNativeRuntimeExecution() {
        String imageCode = System.getProperty("org.graalvm.nativeimage.imagecode");
        return imageCode != null;
    }

    private static boolean isStarterDisabled(ConfigurableEnvironment environment) {
        String otelSdkDisabled = environment.getProperty("otel.sdk.disabled", "false");
        return "true".equalsIgnoreCase(otelSdkDisabled);
    }

    private static boolean applicationInsightsAgentIsAttached() {
        try {
            Class.forName("com.microsoft.applicationinsights.agent.Agent", false, null);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 1;
    }
}
