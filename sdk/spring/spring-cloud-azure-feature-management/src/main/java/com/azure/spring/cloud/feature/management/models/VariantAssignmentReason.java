// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.feature.management.models;

/**
 * The reason why a given boolean/variant was returned when calling isEnabled/getVariant. This enum represents the
 * different filtering mechanisms that determined the feature flag state.
 */
public enum VariantAssignmentReason {

    /**
     * Indicates no specific reason was assigned for the feature flag evaluation.
     */
    NONE("None"),

    /**
     * Indicates the feature flag was evaluated based on the default value when the flag is disabled.
     */
    DEFAULT_WHEN_DISABLED("DefaultWhenDisabled"),

    /**
     * Indicates the feature flag was evaluated based on the default value when the flag is enabled.
     */
    DEFAULT_WHEN_ENABLED("DefaultWhenEnabled"),

    /**
     * Indicates the feature flag was evaluated based on user targeting criteria.
     */
    USER("User"),

    /**
     * Indicates the feature flag was evaluated based on group targeting criteria.
     */
    GROUP("Group"),

    /**
     * Indicates the feature flag was evaluated based on percentile targeting criteria.
     */
    PERCENTILE("Percentile");

    private final String value;

    /**
     * Creates a new instance of the VariantAssignmentReason enum with the specified value.
     *
     * @param value The string representation of the variant assignment reason
     */
    VariantAssignmentReason(final String value) {
        this.value = value;
    }

    /**
     * Gets the string representation of this variant assignment reason.
     *
     * @return the string representation of this variant assignment reason
     */
    public String getValue() {
        return value;
    }

}
