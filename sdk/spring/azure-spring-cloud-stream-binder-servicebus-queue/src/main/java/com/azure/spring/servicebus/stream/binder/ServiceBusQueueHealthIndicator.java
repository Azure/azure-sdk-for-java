// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.servicebus.stream.binder;

import com.azure.spring.integration.servicebus.health.Instrumentation;
import com.azure.spring.integration.servicebus.health.InstrumentationManager;
import com.azure.spring.servicebus.core.queue.ServiceBusQueueOperation;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;

/**
 * Implementation of a {@link AbstractHealthIndicator} returning status information for
 * service bus queue.
 */
public class ServiceBusQueueHealthIndicator extends AbstractHealthIndicator {

    private final InstrumentationManager instrumentationManager;

    public ServiceBusQueueHealthIndicator(ServiceBusQueueOperation serviceBusQueueOperation) {
        super("Service bus health check failed");
        this.instrumentationManager = serviceBusQueueOperation.getInstrumentationManager();
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
        if (instrumentationManager.getHealthInstrumentations().stream()
                                  .allMatch(Instrumentation::isOutOfService)) {
            builder.outOfService();
            return;
        }
        builder.down();
        instrumentationManager.getHealthInstrumentations().stream()
                              .filter(instrumentation -> !instrumentation.isStarted())
                              .forEach(instrumentation -> builder
                                  .withDetail(instrumentation.getName() + ":" + instrumentation.getType().getTypeName(),
                                      instrumentation.getStartException()));
    }
}
