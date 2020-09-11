// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.monitor.implementation;

import com.azure.resourcemanager.monitor.models.AggregationType;
import com.azure.resourcemanager.monitor.models.MetricAlert;
import com.azure.resourcemanager.monitor.models.MetricAlertCondition;
import com.azure.resourcemanager.monitor.models.MetricAlertRuleCondition;
import com.azure.resourcemanager.monitor.models.MetricAlertRuleTimeAggregation;
import com.azure.resourcemanager.monitor.models.MetricCriteria;
import com.azure.resourcemanager.monitor.models.Operator;
import java.util.ArrayList;

/** Implementation for MetricAlertCondition. */
class MetricAlertConditionImpl extends MetricAlertConditionBaseImpl<MetricCriteria, MetricAlertConditionImpl>
    implements MetricAlertCondition,
        MetricAlertCondition.DefinitionStages,
        MetricAlertCondition.DefinitionStages.Blank.MetricName<MetricAlert.DefinitionStages.WithCreate>,
        MetricAlertCondition.DefinitionStages.WithCriteriaOperator<MetricAlert.DefinitionStages.WithCreate>,
        MetricAlertCondition.DefinitionStages.WithConditionAttach<MetricAlert.DefinitionStages.WithCreate>,
        MetricAlertCondition.UpdateDefinitionStages,
        MetricAlertCondition.UpdateDefinitionStages.Blank.MetricName<MetricAlert.Update>,
        MetricAlertCondition.UpdateDefinitionStages.WithCriteriaOperator<MetricAlert.Update>,
        MetricAlertCondition.UpdateDefinitionStages.WithConditionAttach<MetricAlert.Update>,
        MetricAlertCondition.UpdateStages {

    MetricAlertConditionImpl(String name, MetricCriteria innerObject, MetricAlertImpl parent) {
        super(name, innerObject, parent);
    }

    @Override
    public MetricAlertConditionImpl withCondition(
        MetricAlertRuleTimeAggregation timeAggregation, MetricAlertRuleCondition condition, double threshold) {
        this.inner().withOperator(Operator.fromString(condition.toString()));
        this.inner().withTimeAggregation(AggregationType.fromString(timeAggregation.toString()));
        this.inner().withThreshold(threshold);
        return this;
    }

    @Override
    public MetricAlertImpl attach() {
        this.inner().withDimensions(new ArrayList<>(this.dimensions.values()));
        return this.parent().withAlertCriteria(this);
    }

    @Override
    public MetricAlertRuleCondition condition() {
        return MetricAlertRuleCondition.fromString(this.inner().operator().toString());
    }

    @Override
    public double threshold() {
        return this.inner().threshold();
    }
}
