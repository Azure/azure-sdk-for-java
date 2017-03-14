/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.monitor.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.monitor.AutoscaleNotification;
import com.microsoft.azure.management.monitor.AutoscaleProfile;
import com.microsoft.azure.management.monitor.AutoscaleSetting;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import rx.Observable;

import java.util.List;
import java.util.Map;

/**
 * Implementation for CdnProfile.
 */
@LangDefinition
class AutoscaleSettingImpl
        extends GroupableResourceImpl<
            AutoscaleSetting,
            AutoscaleSettingResourceInner,
            AutoscaleSettingImpl,
            MonitorManager>
        implements
            AutoscaleSetting,
            AutoscaleSetting.Definition,
            AutoscaleSetting.Update {

    AutoscaleSettingImpl(String name, final AutoscaleSettingResourceInner innerModel, final MonitorManager monitorManager) {
        super(name, innerModel, monitorManager);
    }

    @Override
    public Map<String, AutoscaleProfile> profiles() {
        return null;
    }

    @Override
    public List<AutoscaleNotification> notifications() {
        return null;
    }

    @Override
    public boolean enabled() {
        return false;
    }

    @Override
    public String targetResourceUri() {
        return null;
    }

    @Override
    public AutoscaleProfileImpl defineAutoscaleProfile(String name) {
        return null;
    }

    @Override
    public AutoscaleProfileImpl updateAutoscaleProfile(String name) {
        return null;
    }

    @Override
    public AutoscaleSettingImpl withoutAutoscaleNotification(String name) {
        return this;
    }

    @Override
    public AutoscaleSettingImpl withoutAutoscaleNotifications() {
        return this;
    }

    @Override
    public AutoscaleNotificationImpl defineAutoscaleNotification() {
        return null;
    }

    @Override
    public AutoscaleNotificationImpl updateAutoscaleNotification(AutoscaleNotification notification) {
        return null;
    }

    @Override
    public AutoscaleSettingImpl withAutoscaleSettingEnabled() {
        return this;
    }

    @Override
    public AutoscaleSettingImpl withAutoscaleSettingDisabled() {
        return this;
    }

    @Override
    public AutoscaleSettingImpl withTargetResourceUri(String targetResourceUri) {
        return this;
    }

    @Override
    public AutoscaleSettingImpl withAutoscaleSettingResource() {
        return this;
    }

    @Override
    public AutoscaleSettingImpl withoutAutoscaleSettingResource() {
        return this;
    }

    @Override
    public AutoscaleSettingImpl withAutoscaleSettingResourceName(String name) {
        return this;
    }

    @Override
    public AutoscaleSettingImpl withAutoscaleSettingResourceTargetResourceUri(String targetResourceUri) {
        return this;
    }

    @Override
    protected Observable<AutoscaleSettingResourceInner> getInnerAsync() {
        return null;
    }

    @Override
    public Observable<AutoscaleSetting> createResourceAsync() {
        return null;
    }
}
