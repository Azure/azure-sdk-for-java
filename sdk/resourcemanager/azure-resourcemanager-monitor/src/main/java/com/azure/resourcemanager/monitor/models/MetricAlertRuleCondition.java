// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.monitor.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Collection;

/** Defines values for MetricAlertRuleCondition. */
public final class MetricAlertRuleCondition extends ExpandableStringEnum<MetricAlertRuleCondition> {
    /** Static value Equals for MetricAlertRuleCondition. */
    public static final MetricAlertRuleCondition EQUALS = fromString("Equals");

    /** Static value NotEquals for MetricAlertRuleCondition. */
    public static final MetricAlertRuleCondition NOT_EQUALS = fromString("NotEquals");

    /** Static value GreaterThan for MetricAlertRuleCondition. */
    public static final MetricAlertRuleCondition GREATER_THAN = fromString("GreaterThan");

    /** Static value GreaterThanOrEqual for MetricAlertRuleCondition. */
    public static final MetricAlertRuleCondition GREATER_THAN_OR_EQUAL = fromString("GreaterThanOrEqual");

    /** Static value LessThan for MetricAlertRuleCondition. */
    public static final MetricAlertRuleCondition LESS_THAN = fromString("LessThan");

    /** Static value LessThanOrEqual for MetricAlertRuleCondition. */
    public static final MetricAlertRuleCondition LESS_THAN_OR_EQUAL = fromString("LessThanOrEqual");

    /**
     * Creates or finds a MetricAlertRuleCondition from its string representation.
     *
     * @param name a name to look for
     * @return the corresponding MetricAlertRuleCondition
     */
    @JsonCreator
    public static MetricAlertRuleCondition fromString(String name) {
        return fromString(name, MetricAlertRuleCondition.class);
    }

    /** @return known MetricAlertRuleCondition values */
    public static Collection<MetricAlertRuleCondition> values() {
        return values(MetricAlertRuleCondition.class);
    }
}
