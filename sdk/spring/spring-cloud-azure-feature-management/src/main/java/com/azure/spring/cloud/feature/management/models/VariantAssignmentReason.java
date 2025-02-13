// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.feature.management.models;

/**
 * The reason why a given boolean/variant was returned when calling isEnabled/getVariant.
 */
public enum VariantAssignmentReason {

    /** None */
    NONE("None"),
    /** Default when Disabled */
    DEFAULT_WHEN_DISABLED("DefaultWhenDisabled"),
    /** Default when Enabled */
    DEFAULT_WHEN_ENABLED("DefaultWhenEnabled"),
    /** User */
    USER("User"),
    /** Group */
    GROUP("Group"),
    /** Percentile */
    PERCENTILE("Percentile");

    private final String type;

    VariantAssignmentReason(final String type) {
        this.type = type;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

}
