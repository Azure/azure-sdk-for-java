// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.telemetry;

/**
 * This class contains constants used for telemetry events related to feature
 * evaluations. These constants are used to define the structure and content of
 * telemetry events, including event names, property names, and versioning
 * information.
 */
public final class TelemetryConstants {

    static final String EVENT_NAME = "FeatureEvaluation";

    static final String FEATURE_NAME = "FeatureName";

    static final String ENABLED = "Enabled";

    static final String VARIANT = "Variant";

    static final String REASON = "VariantAssignmentReason";

    static final String DEFAULT_WHEN_ENABLED = "DefaultWhenEnabled";

    static final String VERSION = "Version";

    static final String VARIANT_ASSIGNMENT_PERCENTAGE = "VariantAssignmentPercentage";

    static final String EVALUATION_EVENT_VERSION = "1.1.0";

    static final String APPLICATION_INSIGHTS_CUSTOM_EVENT_KEY = "microsoft.custom_event.name";

    /** Private constructor to prevent instantiation. */
    private TelemetryConstants() {
    }
}
