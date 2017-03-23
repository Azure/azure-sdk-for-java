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
import rx.functions.Func1;

import java.util.List;
import java.util.Map;

/**
 * Implementation for AutoscaleSetting.
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

    private AutoscaleProfilesImpl autoscaleProfilesImpl;
    private AutoscaleNotificationsImpl autoscaleNotificationsImpl;

    AutoscaleSettingImpl(String name, final AutoscaleSettingResourceInner innerModel, final MonitorManager monitorManager) {
        super(name, innerModel, monitorManager);
    }

    @Override
    public void setInner(AutoscaleSettingResourceInner inner) {
        super.setInner(inner);
        autoscaleProfilesImpl = new AutoscaleProfilesImpl(this);
        autoscaleNotificationsImpl = new AutoscaleNotificationsImpl(this);
    }

    @Override
    public Map<String, AutoscaleProfile> profiles() {
        return this.autoscaleProfilesImpl.profilesAsMap();
    }

    @Override
    public List<AutoscaleNotification> notifications() {
        return this.autoscaleNotificationsImpl.profilesAsList();
    }

    @Override
    public boolean enabled() {
        return this.inner().enabled();
    }

    @Override
    public String targetResourceUri() {
        return this.inner().targetResourceUri();
    }

    @Override
    public AutoscaleSettingImpl withoutAutoscaleProfile(String name) {
        this.autoscaleProfilesImpl.removeProfile(name);
        return this;
    }

    @Override
    public AutoscaleProfileImpl defineAutoscaleProfile(String name) {
        return this.autoscaleProfilesImpl.defineProfile(name);
    }

    @Override
    public AutoscaleProfileImpl updateAutoscaleProfile(String name) {
        return this.autoscaleProfilesImpl.updateProfile(name);
    }

    @Override
    public AutoscaleSettingImpl withoutAutoscaleNotification(AutoscaleNotification notification) {
        this.autoscaleNotificationsImpl.removeNotification(notification.name());
        return this;
    }

    @Override
    public AutoscaleSettingImpl withoutAutoscaleNotifications() {
        this.autoscaleNotificationsImpl.clear();
        return this;
    }

    @Override
    public AutoscaleNotificationImpl defineAutoscaleNotification() {
        return this.autoscaleNotificationsImpl.defineNotification();
    }

    @Override
    public AutoscaleNotificationImpl updateAutoscaleNotification(AutoscaleNotification notification) {
        return this.autoscaleNotificationsImpl.updateNotification(notification.name());
    }

    @Override
    public AutoscaleSettingImpl withAutoscaleSettingEnabled() {
        this.inner().withEnabled(true);
        return this;
    }

    @Override
    public AutoscaleSettingImpl withAutoscaleSettingDisabled() {
        this.inner().withEnabled(false);
        return this;
    }

    @Override
    public AutoscaleSettingImpl withTargetResourceUri(String targetResourceUri) {
        this.inner().withTargetResourceUri(targetResourceUri);
        return this;
    }

    @Override
    public AutoscaleSettingImpl withoutTargetResourceUri() {
        this.inner().withTargetResourceUri(null);
        return this;
    }

    @Override
    protected Observable<AutoscaleSettingResourceInner> getInnerAsync() {
        return this.manager().inner().autoscaleSettings().getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    @Override
    public Observable<AutoscaleSetting> createResourceAsync() {
        final AutoscaleSettingImpl self = this;
        return this.manager().inner().autoscaleSettings().createOrUpdateAsync(resourceGroupName(), name(), inner())
                .map(new Func1<AutoscaleSettingResourceInner, AutoscaleSetting>() {
                    @Override
                    public AutoscaleSetting call(AutoscaleSettingResourceInner profileInner) {
                        self.setInner(profileInner);
                        return self;
                    }
                });
    }
}
