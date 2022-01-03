// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.monitor.implementation;

import com.azure.resourcemanager.monitor.models.ComparisonOperationType;
import com.azure.resourcemanager.monitor.models.MetricStatisticType;
import com.azure.resourcemanager.monitor.models.MetricTrigger;
import com.azure.resourcemanager.monitor.models.ScaleAction;
import com.azure.resourcemanager.monitor.models.ScaleDirection;
import com.azure.resourcemanager.monitor.models.ScaleRule;
import com.azure.resourcemanager.monitor.models.ScaleType;
import com.azure.resourcemanager.monitor.models.TimeAggregationType;
import com.azure.resourcemanager.monitor.fluent.models.ScaleRuleInner;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;
import java.time.Duration;

/** Implementation for ScaleRule. */
class ScaleRuleImpl extends WrapperImpl<ScaleRuleInner>
    implements ScaleRule,
        ScaleRule.Definition,
        ScaleRule.ParentUpdateDefinition,
        ScaleRule.UpdateDefinition,
        ScaleRule.Update {

    private final AutoscaleProfileImpl parent;

    ScaleRuleImpl(ScaleRuleInner innerObject, AutoscaleProfileImpl parent) {
        super(innerObject);
        this.parent = parent;
        if (this.innerModel().metricTrigger() == null) {
            this.innerModel().withMetricTrigger(new MetricTrigger());
        }
        if (this.innerModel().scaleAction() == null) {
            this.innerModel().withScaleAction(new ScaleAction());
        }
    }

    @Override
    public String metricSource() {
        if (this.innerModel().metricTrigger() != null) {
            return this.innerModel().metricTrigger().metricResourceUri();
        }
        return null;
    }

    @Override
    public String metricName() {
        if (this.innerModel().metricTrigger() != null) {
            return this.innerModel().metricTrigger().metricName();
        }
        return null;
    }

    @Override
    public Duration duration() {
        if (this.innerModel().metricTrigger() != null) {
            return this.innerModel().metricTrigger().timeWindow();
        }
        return null;
    }

    @Override
    public Duration frequency() {
        if (this.innerModel().metricTrigger() != null) {
            return this.innerModel().metricTrigger().timeGrain();
        }
        return null;
    }

    @Override
    public MetricStatisticType frequencyStatistic() {
        if (this.innerModel().metricTrigger() != null) {
            return this.innerModel().metricTrigger().statistic();
        }
        return null;
    }

    @Override
    public ComparisonOperationType condition() {
        if (this.innerModel().metricTrigger() != null) {
            return this.innerModel().metricTrigger().operator();
        }
        return null;
    }

    @Override
    public TimeAggregationType timeAggregation() {
        if (this.innerModel().metricTrigger() != null) {
            return this.innerModel().metricTrigger().timeAggregation();
        }
        return null;
    }

    @Override
    public double threshold() {
        if (this.innerModel().metricTrigger() != null) {
            return this.innerModel().metricTrigger().threshold();
        }
        return 0;
    }

    @Override
    public ScaleDirection scaleDirection() {
        if (this.innerModel().scaleAction() != null) {
            return this.innerModel().scaleAction().direction();
        }
        return null;
    }

    @Override
    public ScaleType scaleType() {
        if (this.innerModel().scaleAction() != null) {
            return this.innerModel().scaleAction().type();
        }
        return null;
    }

    @Override
    public int scaleInstanceCount() {
        if (this.innerModel().scaleAction() != null) {
            return Integer.parseInt(this.innerModel().scaleAction().value());
        }
        return 0;
    }

    @Override
    public Duration cooldown() {
        if (this.innerModel().scaleAction() != null) {
            return this.innerModel().scaleAction().cooldown();
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
        this.innerModel().metricTrigger().withMetricResourceUri(metricSourceResourceId);
        return this;
    }

    @Override
    public ScaleRuleImpl withMetricName(String metricName) {
        this.innerModel().metricTrigger().withMetricName(metricName);
        return this;
    }

    @Override
    public ScaleRuleImpl withStatistic(Duration duration, Duration frequency, MetricStatisticType statisticType) {
        this.innerModel().metricTrigger().withStatistic(statisticType);
        this.innerModel().metricTrigger().withTimeWindow(duration);
        this.innerModel().metricTrigger().withTimeGrain(frequency);
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
    public ScaleRuleImpl withCondition(
        TimeAggregationType timeAggregation, ComparisonOperationType condition, double threshold) {
        this.innerModel().metricTrigger().withOperator(condition);
        this.innerModel().metricTrigger().withTimeAggregation(timeAggregation);
        this.innerModel().metricTrigger().withThreshold(threshold);
        return this;
    }

    @Override
    public ScaleRuleImpl withScaleAction(
        ScaleDirection direction, ScaleType type, int instanceCountChange, Duration cooldown) {
        this.innerModel().scaleAction().withDirection(direction);
        this.innerModel().scaleAction().withType(type);
        this.innerModel().scaleAction().withValue(Integer.toString(instanceCountChange));
        this.innerModel().scaleAction().withCooldown(cooldown);
        return this;
    }
}
