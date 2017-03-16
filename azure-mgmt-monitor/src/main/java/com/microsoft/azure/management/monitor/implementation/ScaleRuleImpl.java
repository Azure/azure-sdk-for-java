/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.monitor.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.monitor.MetricTrigger;
import com.microsoft.azure.management.monitor.ScaleAction;
import com.microsoft.azure.management.monitor.ScaleRule;

/**
 * Implementation for ScaleRule.
 */
@LangDefinition
class ScaleRuleImpl
        implements
            ScaleRule,
            ScaleRule.Definition,
            ScaleRule.ParentUpdateDefinition,
            ScaleRule.UpdateDefinition,
            ScaleRule.Update {

    ScaleRuleImpl(String name) {
    }

    @Override
    public MetricTrigger metricTrigger() {
        return null;
    }

    @Override
    public ScaleAction scaleAction() {
        return null;
    }

    @Override
    public MetricTriggerImpl defineMetricTrigger(String name) {
        return null;
    }

    @Override
    public ScaleActionImpl defineScaleAction() {
        return null;
    }

    @Override
    public MetricTriggerImpl updateMetricTrigger() {
        return null;
    }

    @Override
    public ScaleActionImpl updateScaleAction() {
        return null;
    }

    @Override
    public AutoscaleProfileImpl parent() {
        return null;
    }

    @Override
    public AutoscaleProfileImpl attach() {
        return null;
    }
}
