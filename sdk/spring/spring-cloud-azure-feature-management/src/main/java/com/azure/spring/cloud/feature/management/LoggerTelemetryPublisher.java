// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.azure.spring.cloud.feature.management.models.EvaluationEvent;
import com.azure.spring.cloud.feature.management.models.Variant;
import com.azure.spring.cloud.feature.management.models.VariantAssignmentReason;

/**
 * Telemetry publisher that logs feature evaluation events using SLF4J. This
 * class implements the TelemetryPublisher interface and is responsible for
 * publishing telemetry events related to feature evaluations. It uses SLF4J for
 * logging the events, which allows for easy integration with various logging
 * frameworks. It uses MDC (Mapped Diagnostic Context) to add contextual
 * information to the logs, such as feature name, enabled status, variant name,
 * and reason for the evaluation.
 */
public class LoggerTelemetryPublisher implements TelemetryPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggerTelemetryPublisher.class);

    private static final String EVENT_NAME = "FeatureEvaluation";

    private static final String FEATURE_NAME = "FeatureName";

    private static final String ENABLED = "Enabled";

    private static final String VARIANT = "Variant";

    private static final String REASON = "VariantAssignmentReason";

    private static final String DEFAULT_WHEN_ENABLED = "DefaultWhenEnabled";

    private static final String VERSION = "Version";

    private static final String VARIANT_ASSIGNMENT_PERCENTAGE = "VariantAssignmentPercentage";

    private static final String EVALUATION_EVENT_VERSION = "1.1.0";

    /*
     * Publishes telemetry events related to feature evaluations. It logs the
     * evaluation event using SLF4J, adding contextual information to the logs using
     * MDC.
     * 
     * @param evaluationEvent The evaluation event to be published.
     */
    public void publishTelemetry(EvaluationEvent evaluationEvent) {
        if (evaluationEvent == null || evaluationEvent.getFeature() == null) {
            return;
        }

        Map<String, String> eventProperties = new HashMap<>();

        eventProperties.put(REASON, evaluationEvent.getReason().getType());

        eventProperties.put(FEATURE_NAME, evaluationEvent.getFeature().getId());
        eventProperties.put(ENABLED, String.valueOf(evaluationEvent.isEnabled()));
        eventProperties.put(VERSION, EVALUATION_EVENT_VERSION);

        Variant variant = evaluationEvent.getVariant();
        if (variant != null) {
            eventProperties.put(VARIANT, variant.getName());
        }

        if (evaluationEvent.getReason() == VariantAssignmentReason.DEFAULT_WHEN_ENABLED) {
            eventProperties.put(VARIANT_ASSIGNMENT_PERCENTAGE, "100");
        } else if (evaluationEvent.getReason() == VariantAssignmentReason.PERCENTILE) {
            Double allocationPercentage = evaluationEvent.getFeature().getAllocation().getPercentile().stream()
                    .filter(percentile -> percentile.getVariant().equals(variant != null ? variant.getName() : ""))
                    .map(allocation -> allocation.getTo() - allocation.getFrom()).reduce((a, b) -> a + b).get();

            eventProperties.put(VARIANT_ASSIGNMENT_PERCENTAGE, String.valueOf(allocationPercentage));
        }

        if (evaluationEvent.getFeature().getAllocation() != null
                && evaluationEvent.getFeature().getAllocation().getDefaultWhenEnabled() != null) {
            eventProperties.put(DEFAULT_WHEN_ENABLED,
                    evaluationEvent.getFeature().getAllocation().getDefaultWhenEnabled());
        }

        // Set the key-value pair in the MDC context for logging
        for (Map.Entry<String, String> entry : eventProperties.entrySet()) {
            MDC.put(entry.getKey(), entry.getValue());
        }
        MDC.put("microsoft.custom_event.name", EVENT_NAME);
        LOGGER.info(EVENT_NAME);

        // Remove the key-value pairs from the MDC context after logging.
        MDC.remove("microsoft.custom_event.name");
        for (String key : eventProperties.keySet()) {
            MDC.remove(key);
        }
    }

}