// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.applicationinsights.spring;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.runtimemetrics.java8.BufferPools;
import io.opentelemetry.instrumentation.runtimemetrics.java8.Classes;
import io.opentelemetry.instrumentation.runtimemetrics.java8.Cpu;
import io.opentelemetry.instrumentation.runtimemetrics.java8.GarbageCollector;
import io.opentelemetry.instrumentation.runtimemetrics.java8.MemoryPools;
import io.opentelemetry.instrumentation.runtimemetrics.java8.Threads;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;

// See https://github.com/Azure/azure-sdk-for-java/issues/35725
public class JvmMetricsPostProcessor implements BeanPostProcessor, Ordered {

    private final AzureTelemetryActivation azureTelemetryActivation;

    public JvmMetricsPostProcessor(AzureTelemetryActivation azureTelemetryActivation) {
        this.azureTelemetryActivation = azureTelemetryActivation;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (azureTelemetryActivation.isTrue() && bean instanceof OpenTelemetry) {
            OpenTelemetry openTelemetry = (OpenTelemetry) bean;
            BufferPools.registerObservers(openTelemetry);
            Classes.registerObservers(openTelemetry);
            Cpu.registerObservers(openTelemetry);
            MemoryPools.registerObservers(openTelemetry);
            Threads.registerObservers(openTelemetry);
            GarbageCollector.registerObservers(openTelemetry);
        }
        return bean;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 1;
    }

}
