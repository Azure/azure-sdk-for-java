// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/**
 * The logical operator to apply across multiple {@link MetricAnomalyAlertConfiguration}.
 */
public final class MetricAnomalyAlertConfigurationsOperator
    extends ExpandableStringEnum<MetricAnomalyAlertConfigurationsOperator> {
    /**
     * Indicate that anomaly should be included in an alert when time series with the same
     * dimension combination exist in every {@link MetricAnomalyAlertConfiguration} and
     * the result based on anomaly detection is true.
     */
    public static final MetricAnomalyAlertConfigurationsOperator AND = fromString("AND");
    /**
     * Indicate that any anomaly should trigger an alert.
     */
    public static final MetricAnomalyAlertConfigurationsOperator OR = fromString("OR");
    /**
     * This operator can be applied only if exactly two {@link MetricAnomalyAlertConfiguration}
     * are specified. When the detection result on the series of one configuration is True then
     * anomaly will be included in an alert only if the detection result on the series of another
     * configuration is False, both series should have the same dimension combination.
     */
    public static final MetricAnomalyAlertConfigurationsOperator XOR = fromString("XOR");

    /**
     * Creates MetricAnomalyAlertConfigurationsOperator from the given string name.
     *
     * @param name The name.
     * @return MetricAnomalyAlertConfigurationsOperator.
     */
    public static MetricAnomalyAlertConfigurationsOperator fromString(String name) {
        return fromString(name, MetricAnomalyAlertConfigurationsOperator.class);
    }

    /**
     * Gets the collections of all MetricAnomalyAlertConfigurationsOperator values.
     *
     * @return The collections of all MetricAnomalyAlertConfigurationsOperator values.
     */
    public static Collection<MetricAnomalyAlertConfigurationsOperator> values() {
        return values(MetricAnomalyAlertConfigurationsOperator.class);
    }
}
