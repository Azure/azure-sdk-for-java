// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.monitor.models;

import com.azure.core.util.ExpandableStringEnum;
import java.util.Collection;

/** Defines values for MetricAlertRuleTimeAggregation. */
public final class MetricAlertRuleTimeAggregation extends ExpandableStringEnum<MetricAlertRuleTimeAggregation> {
    /** Static value Count for MetricAlertRuleTimeAggregation. */
    public static final MetricAlertRuleTimeAggregation COUNT = fromString("Count");

    /** Static value Average for MetricAlertRuleTimeAggregation. */
    public static final MetricAlertRuleTimeAggregation AVERAGE = fromString("Average");

    /** Static value Minimum for MetricAlertRuleTimeAggregation. */
    public static final MetricAlertRuleTimeAggregation MINIMUM = fromString("Minimum");

    /** Static value Maximum for MetricAlertRuleTimeAggregation. */
    public static final MetricAlertRuleTimeAggregation MAXIMUM = fromString("Maximum");

    /** Static value Total for MetricAlertRuleTimeAggregation. */
    public static final MetricAlertRuleTimeAggregation TOTAL = fromString("Total");

    /**
     * Creates a new instance of MetricAlertRuleTimeAggregation value.
     *
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public MetricAlertRuleTimeAggregation() {
    }

    /**
     * Creates or finds a MetricAlertRuleTimeAggregation from its string representation.
     *
     * @param name a name to look for
     * @return the corresponding MetricAlertRuleTimeAggregation
     */
    public static MetricAlertRuleTimeAggregation fromString(String name) {
        return fromString(name, MetricAlertRuleTimeAggregation.class);
    }

    /**
     * Gets known MetricAlertRuleTimeAggregation values.
     *
     * @return known MetricAlertRuleTimeAggregation values
     */
    public static Collection<MetricAlertRuleTimeAggregation> values() {
        return values(MetricAlertRuleTimeAggregation.class);
    }
}
