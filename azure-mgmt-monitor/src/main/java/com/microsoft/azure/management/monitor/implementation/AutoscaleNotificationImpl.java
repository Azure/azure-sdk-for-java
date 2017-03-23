/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.monitor.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.monitor.AutoscaleNotification;
import com.microsoft.azure.management.monitor.AutoscaleSetting;
import com.microsoft.azure.management.monitor.WebhookNotification;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import rx.Observable;

import java.util.List;
import java.util.Map;

/**
 * Implementation for AutoscaleNotification.
 */
@LangDefinition
class AutoscaleNotificationImpl extends
        ExternalChildResourceImpl<AutoscaleNotification,
                        AutoscaleNotificationInner,
                        AutoscaleSettingImpl,
                        AutoscaleSetting>

        implements
            AutoscaleNotification,
            AutoscaleNotification.Definition,
            AutoscaleNotification.UpdateDefinition,
            AutoscaleNotification.Update {

    AutoscaleNotificationImpl(AutoscaleSettingImpl parent, AutoscaleNotificationInner inner) {
        super(SdkContext.randomResourceName("runtimeId_", 50), parent, inner);
    }

    @Override
    public String operation() {
        return null;
    }

    @Override
    public boolean sendToSubscriptionAdministrator() {
        return false;
    }

    @Override
    public boolean sendToSubscriptionCoAdministrators() {
        return false;
    }

    @Override
    public List<String> customEmails() {
        return null;
    }

    @Override
    public Map<String, WebhookNotification> webhooks() {
        return null;
    }

    @Override
    public AutoscaleSettingImpl attach() {
        return null;
    }

    @Override
    public AutoscaleNotificationImpl withSendToSubscriptionAdministrator() {
        return null;
    }

    @Override
    public AutoscaleNotificationImpl withoutSendToSubscriptionAdministrator() {
        return null;
    }

    @Override
    public AutoscaleNotificationImpl withSendToSubscriptionCoAdministrators() {
        return null;
    }

    @Override
    public AutoscaleNotificationImpl withoutSendToSubscriptionCoAdministrators() {
        return null;
    }

    @Override
    public AutoscaleNotificationImpl withEmailNotificationCustomEmail(String customEmail) {
        return null;
    }

    @Override
    public AutoscaleNotificationImpl withoutEmailNotificationCustomEmail(String customEmails) {
        return null;
    }

    @Override
    public AutoscaleNotificationImpl withoutEmailNotificationCustomEmails() {
        return null;
    }

    @Override
    public AutoscaleNotificationImpl withoutWebhookNotification() {
        return null;
    }

    @Override
    public AutoscaleNotificationImpl withWebhookNotification(String serviceUri, Map properties) {
        return null;
    }

    @Override
    public AutoscaleNotificationImpl withEmailNotificationCustomEmails(List customEmails) {
        return null;
    }

    @Override
    public AutoscaleSettingImpl parent() {
        return null;
    }

    @Override
    public String id() {
        return this.name();
    }

    @Override
    public Observable<AutoscaleNotification> createAsync() {
        return null;
    }

    @Override
    public Observable<AutoscaleNotification> updateAsync() {
        return null;
    }

    @Override
    public Observable<Void> deleteAsync() {
        return null;
    }

    @Override
    protected Observable<AutoscaleNotificationInner> getInnerAsync() {
        return null;
    }
}
