// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.stream.binder.servicebus;

import com.azure.spring.integration.instrumentation.Instrumentation;
import com.azure.spring.integration.instrumentation.InstrumentationManager;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;

/**
 * Implementation of a {@link AbstractHealthIndicator} returning status information for
 * service bus queue.
 */
public final class ServiceBusHealthIndicator extends AbstractHealthIndicator {

    private final InstrumentationManager instrumentationManager;

    public ServiceBusHealthIndicator(ServiceBusMessageChannelBinder binder) {
        super("Service bus health check failed");
        this.instrumentationManager = binder.getInstrumentationManager();
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        if (instrumentationManager == null || instrumentationManager.getHealthInstrumentations().isEmpty()) {
            builder.unknown();
            return;
        }
        if (instrumentationManager.getHealthInstrumentations().stream()
                                  .allMatch(Instrumentation::isUp)) {
            builder.up();
            return;
        }
        builder.down();
        instrumentationManager.getHealthInstrumentations().stream()
                              .filter(instrumentation -> instrumentation.isDown())
                              .forEach(instrumentation -> builder
                                  .withDetail(instrumentation.getId(),
                                      instrumentation.getException()));
    }
}
