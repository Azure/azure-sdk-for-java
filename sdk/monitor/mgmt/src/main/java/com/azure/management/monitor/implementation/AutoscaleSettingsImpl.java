/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.monitor.implementation;

import com.azure.management.monitor.AutoscaleSettings;
import com.azure.management.monitor.AutoscaleSetting;
import com.azure.management.monitor.models.AutoscaleSettingResourceInner;
import com.azure.management.monitor.models.AutoscaleSettingsInner;
import com.azure.management.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;

/**
 * Implementation for {@link AutoscaleSettings}.
 */
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
        if (inner ==  null) {
            return null;
        }
        return new AutoscaleSettingImpl(inner.getName(), inner, this.manager());
    }

    @Override
    public AutoscaleSettingImpl define(String name) {
        return wrapModel(name);
    }
}
