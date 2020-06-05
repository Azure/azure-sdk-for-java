// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.monitor.implementation;

import com.azure.resourcemanager.monitor.AutoscaleSetting;
import com.azure.resourcemanager.monitor.AutoscaleSettings;
import com.azure.resourcemanager.monitor.models.AutoscaleSettingResourceInner;
import com.azure.resourcemanager.monitor.models.AutoscaleSettingsInner;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;

/** Implementation for {@link AutoscaleSettings}. */
class AutoscaleSettingsImpl
    extends TopLevelModifiableResourcesImpl<
        AutoscaleSetting, AutoscaleSettingImpl, AutoscaleSettingResourceInner, AutoscaleSettingsInner, MonitorManager>
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
