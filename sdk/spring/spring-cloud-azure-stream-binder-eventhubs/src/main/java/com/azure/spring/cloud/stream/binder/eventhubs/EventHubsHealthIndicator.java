// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.eventhubs;

import com.azure.spring.integration.core.instrumentation.Instrumentation;
import com.azure.spring.integration.core.instrumentation.InstrumentationManager;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;

/**
 * Health indicator for Event Hubs.
 */
public class EventHubsHealthIndicator extends AbstractHealthIndicator {

    private final InstrumentationManager instrumentationManager;

    /**
     * Construct a {@link EventHubsHealthIndicator} with the specified {@link EventHubsMessageChannelBinder}.
     *
     * @param binder the binder
     */
    public EventHubsHealthIndicator(EventHubsMessageChannelBinder binder) {
        super("Event hubs health check failed");
        this.instrumentationManager = binder.getInstrumentationManager();
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        if (instrumentationManager == null || instrumentationManager.getAllHealthInstrumentation().isEmpty()) {
            builder.unknown();
            return;
        }
        if (instrumentationManager.getAllHealthInstrumentation().stream()
                                  .allMatch(instr -> Instrumentation.Status.UP.equals(instr.getStatus()))) {
            builder.up();
            return;
        }
        builder.down();
        instrumentationManager.getAllHealthInstrumentation().stream()
                              .filter(instr -> Instrumentation.Status.DOWN.equals(instr.getStatus()))
                              .forEach(instr -> builder.withDetail(instr.getId(), instr.getException()));
    }
}
