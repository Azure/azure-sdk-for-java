// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.applicationinsights.spring;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;

// Necessary for SQL dependencies and metrics
/**
 *
 */
public class OtelGlobalRegistrationPostProcessor implements BeanPostProcessor, Ordered {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (AzureTelemetry.isEnabled() && bean instanceof OpenTelemetry) {
            OpenTelemetry openTelemetry = (OpenTelemetry) bean;
            GlobalOpenTelemetry.set(openTelemetry);
        }
        return bean;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 1;
    }

}
