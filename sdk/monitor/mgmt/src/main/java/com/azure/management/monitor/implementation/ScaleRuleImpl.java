/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.monitor.implementation;

import com.azure.management.monitor.ComparisonOperationType;
import com.azure.management.monitor.MetricStatisticType;
import com.azure.management.monitor.MetricTrigger;
import com.azure.management.monitor.ScaleAction;
import com.azure.management.monitor.ScaleDirection;
import com.azure.management.monitor.ScaleRule;
import com.azure.management.monitor.ScaleType;
import com.azure.management.monitor.TimeAggregationType;
import com.azure.management.monitor.models.ScaleRuleInner;
import com.azure.management.resources.fluentcore.model.implementation.WrapperImpl;

import java.time.Duration;

/**
 * Implementation for ScaleRule.
 */
class ScaleRuleImpl
        extends WrapperImpl<ScaleRuleInner>
        implements
        ScaleRule,
            ScaleRule.Definition,
            ScaleRule.ParentUpdateDefinition,
            ScaleRule.UpdateDefinition,
            ScaleRule.Update {

    private final AutoscaleProfileImpl parent;

    ScaleRuleImpl(ScaleRuleInner innerObject, AutoscaleProfileImpl parent) {
        super(innerObject);
        this.parent = parent;
        if (this.inner().metricTrigger() == null) {
            this.inner().withMetricTrigger(new MetricTrigger());
        }
        if (this.inner().scaleAction() == null) {
            this.inner().withScaleAction(new ScaleAction());
        }
    }

    @Override
    public String metricSource() {
        if (this.inner().metricTrigger() != null) {
            return this.inner().metricTrigger().metricResourceUri();
        }
        return null;
    }

    @Override
    public String metricName() {
        if (this.inner().metricTrigger() != null) {
            return this.inner().metricTrigger().metricName();
        }
        return null;
    }

    @Override
    public Duration duration() {
        if (this.inner().metricTrigger() != null) {
            return this.inner().metricTrigger().timeWindow();
        }
        return null;
    }

    @Override
    public Duration frequency() {
        if (this.inner().metricTrigger() != null) {
            return this.inner().metricTrigger().timeGrain();
        }
        return null;
    }

    @Override
    public MetricStatisticType frequencyStatistic() {
        if (this.inner().metricTrigger() != null) {
            return this.inner().metricTrigger().statistic();
        }
        return null;
    }

    @Override
    public ComparisonOperationType condition() {
        if (this.inner().metricTrigger() != null) {
            return this.inner().metricTrigger().operator();
        }
        return null;
    }

    @Override
    public TimeAggregationType timeAggregation() {
        if (this.inner().metricTrigger() != null) {
            return this.inner().metricTrigger().timeAggregation();
        }
        return null;
    }

    @Override
    public double threshold() {
        if (this.inner().metricTrigger() != null) {
            return this.inner().metricTrigger().threshold();
        }
        return 0;
    }

    @Override
    public ScaleDirection scaleDirection() {
        if (this.inner().scaleAction() != null) {
            return this.inner().scaleAction().direction();
        }
        return null;
    }

    @Override
    public ScaleType scaleType() {
        if (this.inner().scaleAction() != null) {
            return this.inner().scaleAction().type();
        }
        return null;
    }

    @Override
    public int scaleInstanceCount() {
        if (this.inner().scaleAction() != null) {
            return Integer.parseInt(this.inner().scaleAction().value());
        }
        return 0;
    }

    @Override
    public Duration coolDown() {
        if (this.inner().scaleAction() != null) {
            return this.inner().scaleAction().cooldown();
        }
        return null;
    }

    @Override
    public AutoscaleProfileImpl parent() {
        // end of update
        return this.parent;
    }

    @Override
    public AutoscaleProfileImpl attach() {
        return this.parent.addNewScaleRule(this);
    }

    @Override
    public ScaleRuleImpl withMetricSource(String metricSourceResourceId) {
        this.inner().metricTrigger().withMetricResourceUri(metricSourceResourceId);
        return this;
    }

    @Override
    public ScaleRuleImpl withMetricName(String metricName) {
        this.inner().metricTrigger().withMetricName(metricName);
        return this;
    }

    @Override
    public ScaleRuleImpl withStatistic(Duration duration, Duration frequency, MetricStatisticType statisticType) {
        this.inner().metricTrigger().withStatistic(statisticType);
        this.inner().metricTrigger().withTimeWindow(duration);
        this.inner().metricTrigger().withTimeGrain(frequency);
        return this;
    }

    @Override
    public ScaleRuleImpl withStatistic() {
        return withStatistic(Duration.ofMinutes(10), Duration.ofMinutes(1), MetricStatisticType.AVERAGE);
    }

    @Override
    public ScaleRuleImpl withStatistic(Duration duration) {
        return withStatistic(duration, Duration.ofMinutes(1), MetricStatisticType.AVERAGE);
    }

    @Override
    public ScaleRuleImpl withStatistic(Duration duration, MetricStatisticType statisticType) {
        return withStatistic(duration, Duration.ofMinutes(1), statisticType);
    }

    @Override
    public ScaleRuleImpl withCondition(TimeAggregationType timeAggregation, ComparisonOperationType condition, double threshold) {
        this.inner().metricTrigger().withOperator(condition);
        this.inner().metricTrigger().withTimeAggregation(timeAggregation);
        this.inner().metricTrigger().withThreshold(threshold);
        return this;
    }

    @Override
    public ScaleRuleImpl withScaleAction(ScaleDirection direction, ScaleType type, int instanceCountChange, Duration cooldown) {
        this.inner().scaleAction().withDirection(direction);
        this.inner().scaleAction().withType(type);
        this.inner().scaleAction().withValue(Integer.toString(instanceCountChange));
        this.inner().scaleAction().withCooldown(cooldown);
        return this;
    }
}

