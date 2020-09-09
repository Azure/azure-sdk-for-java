// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.monitor.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.monitor.models.AggregationType;
import com.azure.resourcemanager.monitor.models.DynamicMetricCriteria;
import com.azure.resourcemanager.monitor.models.DynamicThresholdFailingPeriods;
import com.azure.resourcemanager.monitor.models.DynamicThresholdOperator;
import com.azure.resourcemanager.monitor.models.DynamicThresholdSensitivity;
import com.azure.resourcemanager.monitor.models.MetricAlert;
import com.azure.resourcemanager.monitor.models.MetricAlertRuleTimeAggregation;
import com.azure.resourcemanager.monitor.models.MetricDynamicAlertCondition;
import java.time.OffsetDateTime;
import java.util.ArrayList;

class MetricDynamicAlertConditionImpl
    extends MetricAlertConditionBaseImpl<DynamicMetricCriteria, MetricDynamicAlertConditionImpl>
    implements MetricDynamicAlertCondition,
        MetricDynamicAlertCondition.DefinitionStages,
        MetricDynamicAlertCondition.DefinitionStages.Blank.MetricName<MetricAlert.DefinitionStages.WithCreate>,
        MetricDynamicAlertCondition.DefinitionStages.WithCriteriaOperator<MetricAlert.DefinitionStages.WithCreate>,
        MetricDynamicAlertCondition.DefinitionStages.WithFailingPeriods<MetricAlert.DefinitionStages.WithCreate>,
        MetricDynamicAlertCondition.DefinitionStages.WithConditionAttach<MetricAlert.DefinitionStages.WithCreate>,
        MetricDynamicAlertCondition.UpdateDefinitionStages,
        MetricDynamicAlertCondition.UpdateDefinitionStages.Blank.MetricName<MetricAlert.Update>,
        MetricDynamicAlertCondition.UpdateDefinitionStages.WithCriteriaOperator<MetricAlert.Update>,
        MetricDynamicAlertCondition.UpdateDefinitionStages.WithFailingPeriods<MetricAlert.Update>,
        MetricDynamicAlertCondition.UpdateDefinitionStages.WithConditionAttach<MetricAlert.Update>,
        MetricDynamicAlertCondition.UpdateStages {

    private final ClientLogger logger = new ClientLogger(getClass());

    MetricDynamicAlertConditionImpl(String name, DynamicMetricCriteria innerObject, MetricAlertImpl parent) {
        super(name, innerObject, parent);
    }

    @Override
    public DynamicThresholdOperator condition() {
        return DynamicThresholdOperator.fromString(this.inner().operator().toString());
    }

    @Override
    public DynamicThresholdSensitivity alertSensitivity() {
        return DynamicThresholdSensitivity.fromString(this.inner().alertSensitivity().toString());
    }

    @Override
    public DynamicThresholdFailingPeriods failingPeriods() {
        return this.inner().failingPeriods();
    }

    @Override
    public OffsetDateTime ignoreDataBefore() {
        return this.inner().ignoreDataBefore();
    }

    @Override
    public MetricAlertImpl attach() {
        this.inner().withDimensions(new ArrayList<>(this.dimensions.values()));
        return this.parent().withDynamicAlertCriteria(this);
    }

    @Override
    public MetricDynamicAlertConditionImpl withCondition(
        MetricAlertRuleTimeAggregation timeAggregation,
        DynamicThresholdOperator condition,
        DynamicThresholdSensitivity alertSensitivity) {
        this.inner().withOperator(condition);
        this.inner().withTimeAggregation(AggregationType.fromString(timeAggregation.toString()));
        this.inner().withAlertSensitivity(alertSensitivity);
        return this;
    }

    @Override
    public MetricDynamicAlertConditionImpl withFailingPeriods(DynamicThresholdFailingPeriods failingPeriods) {
        if (failingPeriods.minFailingPeriodsToAlert() > failingPeriods.numberOfEvaluationPeriods()) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                "The number of evaluation periods should be greater than or equal to the number of failing periods"));
        }

        this.inner().withFailingPeriods(failingPeriods);
        return this;
    }

    @Override
    public MetricDynamicAlertConditionImpl withIgnoreDataBefore(OffsetDateTime date) {
        this.inner().withIgnoreDataBefore(date);
        return this;
    }

    @Override
    public MetricDynamicAlertConditionImpl withoutIgnoreDataBefore() {
        this.inner().withIgnoreDataBefore(null);
        return this;
    }
}
