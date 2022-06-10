// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.monitor.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.monitor.MonitorManager;
import com.azure.resourcemanager.monitor.models.AutoscaleNotification;
import com.azure.resourcemanager.monitor.models.AutoscaleProfile;
import com.azure.resourcemanager.monitor.models.AutoscaleSetting;
import com.azure.resourcemanager.monitor.models.EmailNotification;
import com.azure.resourcemanager.monitor.models.WebhookNotification;
import com.azure.resourcemanager.monitor.fluent.models.AutoscaleProfileInner;
import com.azure.resourcemanager.monitor.fluent.models.AutoscaleSettingResourceInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import reactor.core.publisher.Mono;

/** Implementation for AutoscaleSetting. */
class AutoscaleSettingImpl
    extends GroupableResourceImpl<AutoscaleSetting, AutoscaleSettingResourceInner, AutoscaleSettingImpl, MonitorManager>
    implements AutoscaleSetting, AutoscaleSetting.Definition, AutoscaleSetting.Update {

    private final ClientLogger logger = new ClientLogger(getClass());

    AutoscaleSettingImpl(
        String name, final AutoscaleSettingResourceInner innerModel, final MonitorManager monitorManager) {
        super(name, innerModel, monitorManager);
        if (isInCreateMode()) {
            this.innerModel().withEnabled(true);
        }
        if (this.innerModel().notifications() == null) {
            this.innerModel().withNotifications(new ArrayList<AutoscaleNotification>());
            this.innerModel().notifications().add(new AutoscaleNotification());
        }
        if (this.innerModel().profiles() == null) {
            this.innerModel().withProfiles(new ArrayList<AutoscaleProfileInner>());
        }
    }

    @Override
    public String targetResourceId() {
        return this.innerModel().targetResourceUri();
    }

    @Override
    public Map<String, AutoscaleProfile> profiles() {
        Map<String, AutoscaleProfile> result = new HashMap<>();
        for (AutoscaleProfileInner profileInner : this.innerModel().profiles()) {
            AutoscaleProfile profileImpl = new AutoscaleProfileImpl(profileInner.name(), profileInner, this);
            result.put(profileImpl.name(), profileImpl);
        }
        return result;
    }

    @Override
    public boolean autoscaleEnabled() {
        return this.innerModel().enabled();
    }

    @Override
    public boolean adminEmailNotificationEnabled() {
        if (this.innerModel().notifications() != null
            && this.innerModel().notifications().get(0) != null
            && this.innerModel().notifications().get(0).email() != null) {
            return this.innerModel().notifications().get(0).email().sendToSubscriptionAdministrator();
        }
        return false;
    }

    @Override
    public boolean coAdminEmailNotificationEnabled() {
        if (this.innerModel().notifications() != null
            && this.innerModel().notifications().get(0) != null
            && this.innerModel().notifications().get(0).email() != null) {
            return this.innerModel().notifications().get(0).email().sendToSubscriptionCoAdministrators();
        }
        return false;
    }

    @Override
    public List<String> customEmailsNotification() {
        if (this.innerModel().notifications() != null
            && this.innerModel().notifications().get(0) != null
            && this.innerModel().notifications().get(0).email() != null
            && this.innerModel().notifications().get(0).email().customEmails() != null) {
            return this.innerModel().notifications().get(0).email().customEmails();
        }
        return new ArrayList<>();
    }

    @Override
    public String webhookNotification() {
        if (this.innerModel().notifications() != null
            && this.innerModel().notifications().get(0) != null
            && this.innerModel().notifications().get(0).email() != null
            && this.innerModel().notifications().get(0).webhooks() != null
            && this.innerModel().notifications().get(0).webhooks().size() > 0) {
            return this.innerModel().notifications().get(0).webhooks().get(0).serviceUri();
        }
        return null;
    }

    @Override
    public AutoscaleProfileImpl defineAutoscaleProfile(String name) {
        return new AutoscaleProfileImpl(name, new AutoscaleProfileInner(), this);
    }

    @Override
    public AutoscaleProfileImpl updateAutoscaleProfile(String name) {
        int idx = getProfileIndexByName(name);
        if (idx == -1) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("Cannot find autoscale profile with the name '" + name + "'"));
        }
        AutoscaleProfileInner innerProfile = this.innerModel().profiles().get(idx);

        return new AutoscaleProfileImpl(innerProfile.name(), innerProfile, this);
    }

    @Override
    public AutoscaleSettingImpl withoutAutoscaleProfile(String name) {
        int idx = getProfileIndexByName(name);
        if (idx != -1) {
            this.innerModel().profiles().remove(idx);
        }
        return this;
    }

    @Override
    public AutoscaleSettingImpl withTargetResource(String targetResourceId) {
        this.innerModel().withTargetResourceUri(targetResourceId);
        return this;
    }

    @Override
    public AutoscaleSettingImpl withAdminEmailNotification() {
        AutoscaleNotification notificationInner = getNotificationInner();
        notificationInner.email().withSendToSubscriptionAdministrator(true);
        return this;
    }

    @Override
    public AutoscaleSettingImpl withCoAdminEmailNotification() {
        AutoscaleNotification notificationInner = getNotificationInner();
        notificationInner.email().withSendToSubscriptionCoAdministrators(true);
        return this;
    }

    @Override
    public AutoscaleSettingImpl withCustomEmailsNotification(String... customEmailAddresses) {
        AutoscaleNotification notificationInner = getNotificationInner();
        notificationInner.email().withCustomEmails(new ArrayList<String>());
        for (String strEmail : customEmailAddresses) {
            notificationInner.email().customEmails().add(strEmail);
        }
        return this;
    }

    @Override
    public AutoscaleSettingImpl withWebhookNotification(String serviceUri) {
        AutoscaleNotification notificationInner = getNotificationInner();
        if (notificationInner.webhooks() == null) {
            notificationInner.withWebhooks(new ArrayList<WebhookNotification>());
        }
        if (notificationInner.webhooks().isEmpty()) {
            notificationInner.webhooks().add(new WebhookNotification());
        }
        notificationInner.webhooks().get(0).withServiceUri(serviceUri);
        return this;
    }

    @Override
    public AutoscaleSettingImpl withoutAdminEmailNotification() {
        AutoscaleNotification notificationInner = getNotificationInner();
        notificationInner.email().withSendToSubscriptionAdministrator(false);
        return this;
    }

    @Override
    public AutoscaleSettingImpl withoutCoAdminEmailNotification() {
        AutoscaleNotification notificationInner = getNotificationInner();
        notificationInner.email().withSendToSubscriptionCoAdministrators(false);
        return this;
    }

    @Override
    public AutoscaleSettingImpl withoutCustomEmailsNotification() {
        AutoscaleNotification notificationInner = getNotificationInner();
        notificationInner.email().withCustomEmails(null);
        return this;
    }

    @Override
    public AutoscaleSettingImpl withoutWebhookNotification() {
        AutoscaleNotification notificationInner = getNotificationInner();
        notificationInner.withWebhooks(null);
        return this;
    }

    @Override
    public AutoscaleSettingImpl withAutoscaleEnabled() {
        this.innerModel().withEnabled(true);
        return this;
    }

    @Override
    public AutoscaleSettingImpl withAutoscaleDisabled() {
        this.innerModel().withEnabled(false);
        return this;
    }

    @Override
    public Mono<AutoscaleSetting> createResourceAsync() {
        return this
            .manager()
            .serviceClient()
            .getAutoscaleSettings()
            .createOrUpdateAsync(this.resourceGroupName(), this.name(), this.innerModel())
            .map(innerToFluentMap(this));
    }

    @Override
    protected Mono<AutoscaleSettingResourceInner> getInnerAsync() {
        return this
            .manager()
            .serviceClient()
            .getAutoscaleSettings()
            .getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    public AutoscaleSettingImpl addNewAutoscaleProfile(AutoscaleProfileImpl profile) {
        this.innerModel().profiles().add(profile.innerModel());
        return this;
    }

    private int getProfileIndexByName(String name) {
        int idxResult = -1;
        for (int idx = 0; idx < this.innerModel().profiles().size(); idx++) {
            if (this.innerModel().profiles().get(idx).name().equalsIgnoreCase(name)) {
                idxResult = idx;
                break;
            }
        }
        return idxResult;
    }

    private AutoscaleNotification getNotificationInner() {
        AutoscaleNotification notificationInner = this.innerModel().notifications().get(0);
        if (notificationInner.email() == null) {
            notificationInner.withEmail(new EmailNotification());
        }
        return notificationInner;
    }
}
