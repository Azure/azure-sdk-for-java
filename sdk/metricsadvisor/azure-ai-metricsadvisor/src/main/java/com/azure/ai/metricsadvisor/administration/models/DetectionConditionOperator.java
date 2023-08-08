// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/**
 * The logical operator to apply across anomaly detection conditions.
 */
public final class DetectionConditionOperator
    extends ExpandableStringEnum<DetectionConditionOperator> {
    /**
     * The logical operator AND, indicate that all conditions should be satisfied
     * to detect a data point as anomaly.
     */
    public static final DetectionConditionOperator AND = fromString("AND");
    /**
     * The logical operator OR, indicate that at least one conditions should be
     * satisfied to detect a data point as anomaly.
     */
    public static final DetectionConditionOperator OR = fromString("OR");

    /**
     * Create {@link DetectionConditionOperator} from a string value.
     *
     * @param name The string value.
     * @return The {@link DetectionConditionOperator}.
     */
    public static DetectionConditionOperator fromString(String name) {
        return fromString(name, DetectionConditionOperator.class);
    }

    /**
     * Get all values of {@link DetectionConditionOperator}.
     *
     * @return All valid values.
     */
    public static Collection<DetectionConditionOperator> values() {
        return values(DetectionConditionOperator.class);
    }
}
