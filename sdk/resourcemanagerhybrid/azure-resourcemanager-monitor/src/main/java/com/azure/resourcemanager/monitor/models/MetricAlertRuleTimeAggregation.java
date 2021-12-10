// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.monitor.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
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
     * Creates or finds a MetricAlertRuleTimeAggregation from its string representation.
     *
     * @param name a name to look for
     * @return the corresponding MetricAlertRuleTimeAggregation
     */
    @JsonCreator
    public static MetricAlertRuleTimeAggregation fromString(String name) {
        return fromString(name, MetricAlertRuleTimeAggregation.class);
    }

    /** @return known MetricAlertRuleTimeAggregation values */
    public static Collection<MetricAlertRuleTimeAggregation> values() {
        return values(MetricAlertRuleTimeAggregation.class);
    }
}
