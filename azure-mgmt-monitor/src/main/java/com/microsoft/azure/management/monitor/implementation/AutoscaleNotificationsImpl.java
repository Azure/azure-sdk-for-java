/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.monitor.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.monitor.AutoscaleNotification;
import com.microsoft.azure.management.monitor.AutoscaleSetting;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ExternalChildResourcesCachedImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Implementation for AutoscaleNotification collection.
 */
@LangDefinition
class AutoscaleNotificationsImpl extends
        ExternalChildResourcesCachedImpl<AutoscaleNotificationImpl,
                AutoscaleNotification,
                AutoscaleNotificationInner,
                AutoscaleSettingImpl,
                AutoscaleSetting> {

    AutoscaleNotificationsImpl(AutoscaleSettingImpl parent) {
        super(parent, "AutoscaleNotification");
        if (parent.id() != null) {
            this.cacheCollection();
        }
    }

    @Override
    protected List<AutoscaleNotificationImpl> listChildResources() {
        List<AutoscaleNotificationImpl> childResources = new ArrayList<>();

        for (AutoscaleNotificationInner innerNotification : this.parent().inner().notifications()) {
            AutoscaleNotificationImpl profile = new AutoscaleNotificationImpl(this.parent(), innerNotification);
            childResources.add(profile);
        }
        return Collections.unmodifiableList(childResources);
    }

    @Override
    protected AutoscaleNotificationImpl newChildResource(String name) {
        return new AutoscaleNotificationImpl(this.parent(), new AutoscaleNotificationInner());
    }

    List<AutoscaleNotification> profilesAsList() {
        List<AutoscaleNotification> result = new ArrayList<>();
        for (Map.Entry<String, AutoscaleNotificationImpl> entry : this.collection().entrySet()) {
            AutoscaleNotification notification = entry.getValue();
            result.add(notification);
        }
        return Collections.unmodifiableList(result);
    }

    public AutoscaleNotificationImpl defineNotification() {
        return this.prepareDefine("");
    }

    public void removeNotification(String name) {
        this.prepareRemove(name);
    }

    public void clear() {
        this.collection().clear();
    }

    public AutoscaleNotificationImpl updateNotification(String name) {
        return this.prepareUpdate(name);
    }
}
