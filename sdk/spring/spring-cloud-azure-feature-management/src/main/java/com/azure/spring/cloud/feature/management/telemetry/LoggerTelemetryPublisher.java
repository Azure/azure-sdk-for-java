// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.telemetry;

import static com.azure.spring.cloud.feature.management.telemetry.EvaluationEventConstants.APPLICATION_INSIGHTS_CUSTOM_EVENT_KEY;
import static com.azure.spring.cloud.feature.management.telemetry.EvaluationEventConstants.DEFAULT_WHEN_ENABLED;
import static com.azure.spring.cloud.feature.management.telemetry.EvaluationEventConstants.ENABLED;
import static com.azure.spring.cloud.feature.management.telemetry.EvaluationEventConstants.EVALUATION_EVENT_VERSION;
import static com.azure.spring.cloud.feature.management.telemetry.EvaluationEventConstants.EVENT_NAME;
import static com.azure.spring.cloud.feature.management.telemetry.EvaluationEventConstants.FEATURE_NAME;
import static com.azure.spring.cloud.feature.management.telemetry.EvaluationEventConstants.REASON;
import static com.azure.spring.cloud.feature.management.telemetry.EvaluationEventConstants.VARIANT;
import static com.azure.spring.cloud.feature.management.telemetry.EvaluationEventConstants.VARIANT_ASSIGNMENT_PERCENTAGE;
import static com.azure.spring.cloud.feature.management.telemetry.EvaluationEventConstants.VERSION;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.azure.spring.cloud.feature.management.models.EvaluationEvent;
import com.azure.spring.cloud.feature.management.models.FeatureDefinition;
import com.azure.spring.cloud.feature.management.models.PercentileAllocation;
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

    /**
     * Constructor for LoggerTelemetryPublisher. 
     */
    public LoggerTelemetryPublisher() {
        
    }

    /**
     * Publishes telemetry events related to feature evaluations. It logs the
     * evaluation event using SLF4J, adding contextual information to the logs using
     * MDC.
     * 
     * @param evaluationEvent The evaluation event to be published.
     */
    public void publish(EvaluationEvent evaluationEvent) {
        if (evaluationEvent == null || evaluationEvent.getFeature() == null) {
            return;
        }

        FeatureDefinition feature = evaluationEvent.getFeature();

        Variant variant = evaluationEvent.getVariant();

        Map<String, String> eventProperties = new HashMap<>(Map.of(
                FEATURE_NAME, feature.getId(),
                ENABLED, String.valueOf(evaluationEvent.isEnabled()),
                REASON, evaluationEvent.getReason().getValue(),
                VERSION, EVALUATION_EVENT_VERSION));

        if (variant != null) {
            eventProperties.put(VARIANT, variant.getName());
        }
        
        if (evaluationEvent.getReason() == VariantAssignmentReason.DEFAULT_WHEN_ENABLED) {
            // Calculate the amount of unallocated variant percentage. This is therefore the amount allocated to the default when enabled variant.
            eventProperties.put(VARIANT_ASSIGNMENT_PERCENTAGE, "100");
            if (feature.getAllocation() != null && feature.getAllocation().getPercentile() != null) {
                double allocationPercentage = 0.0;
                for (PercentileAllocation allocation : feature.getAllocation().getPercentile()) {
                    if (allocation.getTo() != null && allocation.getFrom() != null) {
                        allocationPercentage += allocation.getTo() - allocation.getFrom();
                    }
                }

                eventProperties.put(VARIANT_ASSIGNMENT_PERCENTAGE, String.valueOf(100 - allocationPercentage));
            }
        } else if (evaluationEvent.getReason() == VariantAssignmentReason.PERCENTILE) {
            if (feature.getAllocation() != null && feature.getAllocation().getPercentile() != null) {
                eventProperties.put(VARIANT_ASSIGNMENT_PERCENTAGE, String.valueOf(feature.getAllocation().getPercentile().stream()
                    // Filter out null values and calculate the sum of the allocation percentages
                    // for the specific variant.
                    .filter(percentile -> percentile.getVariant() != null && variant != null
                        && percentile.getVariant().equals(variant.getName()))
                    .filter(allocation -> allocation.getTo() != null && allocation.getFrom() != null)
                    // Calculate the percentage of the variant allocation.
                    .mapToDouble(allocation -> allocation.getTo() - allocation.getFrom())
                    .sum()));
            }
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
        MDC.put(APPLICATION_INSIGHTS_CUSTOM_EVENT_KEY, EVENT_NAME);
        LOGGER.info(EVENT_NAME);

        // Remove the key-value pairs from the MDC context after logging.
        MDC.remove(APPLICATION_INSIGHTS_CUSTOM_EVENT_KEY);
        for (String key : eventProperties.keySet()) {
            MDC.remove(key);
        }
    }

}
