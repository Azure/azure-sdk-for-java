// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.monitor.implementation;

import com.azure.resourcemanager.monitor.MonitorManager;
import com.azure.resourcemanager.monitor.models.AutoscaleSetting;
import com.azure.resourcemanager.monitor.models.AutoscaleSettings;
import com.azure.resourcemanager.monitor.fluent.inner.AutoscaleSettingResourceInner;
import com.azure.resourcemanager.monitor.fluent.AutoscaleSettingsClient;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;

/** Implementation for {@link AutoscaleSettings}. */
public class AutoscaleSettingsImpl
    extends TopLevelModifiableResourcesImpl<
        AutoscaleSetting, AutoscaleSettingImpl, AutoscaleSettingResourceInner, AutoscaleSettingsClient, MonitorManager>
    implements AutoscaleSettings {

    public AutoscaleSettingsImpl(final MonitorManager monitorManager) {
        super(monitorManager.inner().getAutoscaleSettings(), monitorManager);
    }

    @Override
    protected AutoscaleSettingImpl wrapModel(String name) {
        return new AutoscaleSettingImpl(name, new AutoscaleSettingResourceInner(), this.manager());
    }

    @Override
    protected AutoscaleSettingImpl wrapModel(AutoscaleSettingResourceInner inner) {
        if (inner == null) {
            return null;
        }
        return new AutoscaleSettingImpl(inner.name(), inner, this.manager());
    }

    @Override
    public AutoscaleSettingImpl define(String name) {
        return wrapModel(name);
    }
}
