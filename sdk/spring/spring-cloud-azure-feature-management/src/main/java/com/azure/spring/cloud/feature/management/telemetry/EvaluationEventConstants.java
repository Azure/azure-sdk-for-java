// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.telemetry;

/**
 * This class contains constants used for telemetry events related to feature
 * evaluations. These constants are used to define the structure and content of
 * telemetry events, including event names, property names, and versioning
 * information.
 */
public final class EvaluationEventConstants {

    /** The name of the feature evaluation event. */
    public static final String EVENT_NAME = "FeatureEvaluation";

    /** The name of the feature being evaluated. */
    public static final String FEATURE_NAME = "FeatureName";

    /** The key name of the feature's enabled state. */
    public static final String ENABLED = "Enabled";

    /** The key name of the feature's variant. */
    public static final String VARIANT = "Variant";

    /** The key name of the reason for the variant assignment. */
    public static final String REASON = "VariantAssignmentReason";

    /** The key name of the default variant when the feature is enabled. */
    public static final String DEFAULT_WHEN_ENABLED = "DefaultWhenEnabled";

    /** The key name of the feature's version. */
    public static final String VERSION = "Version";

    /** The key name of the variant assignment percentage. */
    public static final String VARIANT_ASSIGNMENT_PERCENTAGE = "VariantAssignmentPercentage";

    /** The value of the evaluation event version. */
    public static final String EVALUATION_EVENT_VERSION = "1.1.0";

    /** The key name of the application insights custom event. */
    public static final String APPLICATION_INSIGHTS_CUSTOM_EVENT_KEY = "microsoft.custom_event.name";

    /** Private constructor to prevent instantiation. */
    private EvaluationEventConstants() {
    }
}
