// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.servicebus.stream.binder;

import com.azure.spring.integration.servicebus.health.Instrumentation;
import com.azure.spring.integration.servicebus.health.InstrumentationManager;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;

public class ServiceBusTopicHealthIndicator extends AbstractHealthIndicator {
    private InstrumentationManager instrumentationManager;

    public ServiceBusTopicHealthIndicator(InstrumentationManager instrumentationManager) {
        this.instrumentationManager = instrumentationManager;
    }


    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        if (instrumentationManager.getHealthInstrumentations().stream()
                                  .allMatch(Instrumentation::isUp)) {
            builder.up();
            return;
        }
        if (instrumentationManager.getHealthInstrumentations().stream()
                                  .allMatch(Instrumentation::isOutOfService)) {
            builder.outOfService();
            return;
        }
        builder.down();
        instrumentationManager.getHealthInstrumentations().stream()
                              .filter(instrumentation -> !instrumentation.isStarted())
                              .forEach(instrumentation1 -> builder
                                  .withException(instrumentation1.getStartException()));
    }
}
