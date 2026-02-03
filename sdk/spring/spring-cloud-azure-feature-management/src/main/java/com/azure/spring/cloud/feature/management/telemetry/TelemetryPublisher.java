// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.telemetry;

import com.azure.spring.cloud.feature.management.models.EvaluationEvent;

/**
 * TelemetryPublisher is an interface for publishing telemetry events.
 * Implementations of this interface can be used to send telemetry data to various
 * telemetry services or systems.
 */
@FunctionalInterface
public interface TelemetryPublisher {
    
    /**
     * Publishes an evaluation event to the telemetry system.
     *
     * @param evaluationEvent The evaluation event to be published.
     */
    void publish(EvaluationEvent evaluationEvent);

}
