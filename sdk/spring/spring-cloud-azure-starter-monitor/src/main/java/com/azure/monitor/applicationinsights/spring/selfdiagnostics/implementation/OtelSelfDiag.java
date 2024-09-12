// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.applicationinsights.spring.selfdiagnostics.implementation;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.spring.autoconfigure.OpenTelemetryAutoConfiguration;
import org.slf4j.Logger;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;

class OtelSelfDiag implements CommandLineRunner {

    private final ApplicationContext applicationContext;
    private final Logger selfDiagnosticsLogger;

    OtelSelfDiag(ApplicationContext applicationContext, Logger selfDiagnosticsLogger) {
        this.applicationContext = applicationContext;
        this.selfDiagnosticsLogger = selfDiagnosticsLogger;
    }

    @Override
    public void run(String... args) {
        try {
            executeOtelSelfDiagnostics();
        } catch (Exception e) {
            selfDiagnosticsLogger.warn("An unexpected issue has happened during OpenTelemetry self-diagnostics.", e);
        }
    }

    private void executeOtelSelfDiagnostics() {
        if (!selfDiagnosticsLogger.isDebugEnabled()) {
            return;
        }
        if (applicationContext instanceof BeanDefinitionRegistry) {
            BeanDefinitionRegistry beanDefinitionRegistry = (BeanDefinitionRegistry) applicationContext;
            checkBeanComesFromOtelJavaInstrumentationConfig(beanDefinitionRegistry, OpenTelemetry.class);
        }
        if (isOpenTelemetryNoop()) {
            selfDiagnosticsLogger.debug("NOOP OpenTelemetry");
        }
    }

    private void checkBeanComesFromOtelJavaInstrumentationConfig(BeanDefinitionRegistry beanDefinitionRegistry, Class<?> clazz) {
        String[] beanNames = applicationContext.getBeanNamesForType(clazz);
        String beanName = beanNames[0];
        BeanDefinition beanDefinition = beanDefinitionRegistry.getBeanDefinition(beanName);
        String factoryBeanName = beanDefinition.getFactoryBeanName();
        if (factoryBeanName != null) {
            boolean isOtelFactoryBean = factoryBeanName.startsWith(OpenTelemetryAutoConfiguration.class.getName());
            if (!isOtelFactoryBean) {
                selfDiagnosticsLogger.debug("We do not recommend to define a bean of type " + clazz + ". ");
            }
        }
    }

    private boolean isOpenTelemetryNoop() {
        OpenTelemetry openTelemetry = applicationContext.getBean(OpenTelemetry.class);
        return openTelemetry.equals(OpenTelemetry.noop());
    }
}
