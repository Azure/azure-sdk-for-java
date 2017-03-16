/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.monitor.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.monitor.AutoscaleSetting;
import com.microsoft.azure.management.monitor.AutoscaleSettings;
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
        return new AutoscaleSettingImpl(name, new AutoscaleSettingResourceInner(), this.manager());
    }

    @Override
    protected AutoscaleSettingImpl wrapModel(AutoscaleSettingResourceInner inner) {
        return new AutoscaleSettingImpl(inner.name(), inner, this.manager());
    }

    @Override
    public AutoscaleSettingImpl define(String name) {
        return wrapModel(name);
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
    public RecurrenceImpl defineRecurrence() {
        return null;
    }
}
