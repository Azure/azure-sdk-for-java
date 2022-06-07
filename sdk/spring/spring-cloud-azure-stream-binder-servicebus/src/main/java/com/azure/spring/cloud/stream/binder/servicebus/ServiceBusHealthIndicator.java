// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.stream.binder.servicebus;

import com.azure.spring.integration.core.instrumentation.Instrumentation;
import com.azure.spring.integration.core.instrumentation.InstrumentationManager;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;

/**
 * Implementation of a {@link AbstractHealthIndicator} returning status information for
 * service bus queue.
 */
public class ServiceBusHealthIndicator extends AbstractHealthIndicator {

    private final InstrumentationManager instrumentationManager;

    /**
     * Construct a {@link ServiceBusHealthIndicator} with the specified {@link ServiceBusMessageChannelBinder}.
     *
     * @param binder the binder
     */
    public ServiceBusHealthIndicator(ServiceBusMessageChannelBinder binder) {
        super("Service bus health check failed");
        this.instrumentationManager = binder.getInstrumentationManager();
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        if (instrumentationManager == null || instrumentationManager.getAllHealthInstrumentation().isEmpty()) {
            builder.unknown();
            return;
        }
        if (instrumentationManager.getAllHealthInstrumentation().stream()
                                  .allMatch(instr -> Instrumentation.Status.UP == instr.getStatus())) {
            builder.up();
            return;
        }
        builder.down();
        instrumentationManager.getAllHealthInstrumentation().stream()
                              .filter(instr -> Instrumentation.Status.DOWN == instr.getStatus())
                              .forEach(instr -> builder.withDetail(instr.getId(), instr.getException()));
    }
}
