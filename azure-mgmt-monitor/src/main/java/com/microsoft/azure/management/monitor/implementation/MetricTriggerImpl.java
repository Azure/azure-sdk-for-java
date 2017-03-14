/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.monitor.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.monitor.ComparisonOperationType;
import com.microsoft.azure.management.monitor.MetricStatisticType;
import com.microsoft.azure.management.monitor.MetricTrigger;
import com.microsoft.azure.management.monitor.ScaleRule;
import com.microsoft.azure.management.monitor.TimeAggregationType;
import org.joda.time.Period;

/**
 * Implementation for CdnProfile.
 */
@LangDefinition
class MetricTriggerImpl
        implements
            MetricTrigger,
            MetricTrigger.StandaloneDefinition,
            MetricTrigger.StandaloneUpdate,
            MetricTrigger.Definition,
            MetricTrigger.ParentUpdateDefinition,
            MetricTrigger.UpdateDefinition,
            MetricTrigger.Update {

    MetricTriggerImpl(String name) {
    }

    @Override
    public MetricTriggerImpl withMetricName(String metricResourceUri) {
        return null;
    }

    @Override
    public MetricTriggerImpl withMetricResourceUri(String metricResourceUri) {
        return null;
    }

    @Override
    public MetricTriggerImpl withTimeGrain(Period timeGrain) {
        return null;
    }

    @Override
    public MetricTriggerImpl withStatistic(MetricStatisticType statistic) {
        return null;
    }

    @Override
    public MetricTriggerImpl withTimeWindow(Period timeWindow) {
        return null;
    }

    @Override
    public MetricTriggerImpl withTimeAggregation(TimeAggregationType timeAggregation) {
        return null;
    }

    @Override
    public MetricTriggerImpl withOperator(ComparisonOperationType operator) {
        return null;
    }

    @Override
    public MetricTriggerImpl withThreshold(double threshold) {
        return null;
    }

    @Override
    public ScaleRuleImpl parent() {
        return null;
    }

    @Override
    public ScaleRuleImpl attach() {
        return null;
    }

    @Override
    public MetricTriggerImpl create() {
        return this;
    }

    @Override
    public MetricTriggerImpl update() {
        return this;
    }

    @Override
    public MetricTriggerImpl apply() {
        return this;
    }
}
