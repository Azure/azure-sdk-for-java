// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.monitor.implementation;

import com.azure.resourcemanager.monitor.ActivityLogAlert;
import com.azure.resourcemanager.monitor.ActivityLogAlerts;
import com.azure.resourcemanager.monitor.models.ActivityLogAlertResourceInner;
import com.azure.resourcemanager.monitor.models.ActivityLogAlertsInner;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;

/** Implementation for {@link ActivityLogAlerts}. */
class ActivityLogAlertsImpl
    extends TopLevelModifiableResourcesImpl<
        ActivityLogAlert, ActivityLogAlertImpl, ActivityLogAlertResourceInner, ActivityLogAlertsInner, MonitorManager>
    implements ActivityLogAlerts {

    ActivityLogAlertsImpl(final MonitorManager monitorManager) {
        super(monitorManager.inner().activityLogAlerts(), monitorManager);
    }

    @Override
    protected ActivityLogAlertImpl wrapModel(String name) {
        return new ActivityLogAlertImpl(name, new ActivityLogAlertResourceInner(), this.manager());
    }

    @Override
    protected ActivityLogAlertImpl wrapModel(ActivityLogAlertResourceInner inner) {
        if (inner == null) {
            return null;
        }
        return new ActivityLogAlertImpl(inner.name(), inner, this.manager());
    }

    @Override
    public ActivityLogAlertImpl define(String name) {
        return wrapModel(name);
    }
}
