// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/**
 * The logical operator to apply across multiple {@link MetricAlertConfiguration}.
 */
public final class MetricAlertConfigurationsOperator
    extends ExpandableStringEnum<MetricAlertConfigurationsOperator> {
    /**
     * Indicate that anomaly should be included in an alert when time series with the same
     * dimension combination exist in every {@link MetricAlertConfiguration} and
     * the result based on anomaly detection is true.
     */
    public static final MetricAlertConfigurationsOperator AND = fromString("AND");
    /**
     * Indicate that any anomaly should trigger an alert.
     */
    public static final MetricAlertConfigurationsOperator OR = fromString("OR");
    /**
     * This operator can be applied only if exactly two {@link MetricAlertConfiguration}
     * are specified. When the detection result on the series of one configuration is True then
     * anomaly will be included in an alert only if the detection result on the series of another
     * configuration is False, both series should have the same dimension combination.
     */
    public static final MetricAlertConfigurationsOperator XOR = fromString("XOR");

    /**
     * Creates MetricAnomalyAlertConfigurationsOperator from the given string name.
     *
     * @param name The name.
     * @return MetricAnomalyAlertConfigurationsOperator.
     */
    public static MetricAlertConfigurationsOperator fromString(String name) {
        return fromString(name, MetricAlertConfigurationsOperator.class);
    }

    /**
     * Gets the collections of all MetricAlertConfigurationsOperator values.
     *
     * @return The collections of all MetricAlertConfigurationsOperator values.
     */
    public static Collection<MetricAlertConfigurationsOperator> values() {
        return values(MetricAlertConfigurationsOperator.class);
    }
}
