// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/**
 * The logical operator to apply across anomaly detection conditions.
 */
public final class DetectionConditionsOperator
    extends ExpandableStringEnum<DetectionConditionsOperator> {
    /**
     * The logical operator AND, indicate that all conditions should be satisfied
     * to detect a data point as anomaly.
     */
    public static final DetectionConditionsOperator AND = fromString("AND");
    /**
     * The logical operator OR, indicate that at least one conditions should be
     * satisfied to detect a data point as anomaly.
     */
    public static final DetectionConditionsOperator OR = fromString("OR");

    /**
     * Create {@link DetectionConditionsOperator} from a string value.
     *
     * @param name The string value.
     * @return The {@link DetectionConditionsOperator}.
     */
    public static DetectionConditionsOperator fromString(String name) {
        return fromString(name, DetectionConditionsOperator.class);
    }

    /**
     * Get all values of {@link DetectionConditionsOperator}.
     *
     * @return All valid values.
     */
    public static Collection<DetectionConditionsOperator> values() {
        return values(DetectionConditionsOperator.class);
    }
}
