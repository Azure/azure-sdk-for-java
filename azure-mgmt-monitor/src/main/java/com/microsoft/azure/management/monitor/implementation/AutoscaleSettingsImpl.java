/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.monitor.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.monitor.AutoscaleProfile;
import com.microsoft.azure.management.monitor.AutoscaleSetting;
import com.microsoft.azure.management.monitor.AutoscaleSettings;
import com.microsoft.azure.management.monitor.MetricTrigger;
import com.microsoft.azure.management.monitor.Recurrence;
import com.microsoft.azure.management.monitor.ScaleAction;
import com.microsoft.azure.management.monitor.ScaleRule;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;

/**
 * Implementation for {@link AutoscaleSettings}.
 */
@LangDefinition
class AutoscaleSettingsImpl
        extends TopLevelModifiableResourcesImpl<
                            AutoscaleSetting,
                            AutoscaleSettingImpl,
                            AutoscaleSettingResourceInner,
                            AutoscaleSettingsInner,
                            MonitorManager>
        implements AutoscaleSettings {

    AutoscaleSettingsImpl(final MonitorManager monitorManager) {
        super(monitorManager.inner().autoscaleSettings(), monitorManager);
    }

    @Override
    protected AutoscaleSettingImpl wrapModel(String name) {
        return null;
    }

    @Override
    protected AutoscaleSettingImpl wrapModel(AutoscaleSettingResourceInner inner) {
        return null;
    }

    @Override
    public AutoscaleSettingImpl define(String name) {
        return null;
    }

    /*
    @Override
    public MetricTrigger createMetricTrigger(String name) {
        return null;
    }

    @Override
    public ScaleAction createScaleAction() {
        return null;
    }

    @Override
    public Recurrence createRecurrence() {
        return null;
    }*/

    /*@Override
    public ScaleRule.DefinitionStages.MetricTriggerDefinitionStages.WithMetricResourceUri<MetricTrigger> createMetricTrigger(String name) {
        return null;
    }

    @Override
    public ScaleRule.DefinitionStages.ScaleActionDefinitionStages.WithDirection<ScaleAction> createScaleAction() {
        return null;
    }

    @Override
    public AutoscaleProfile.DefinitionStages.RecurrenceDefinitionStages.WithRecurrenceFrequency<Recurrence> createRecurrence() {
        return null;
    }
    */
}
