/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.monitor.implementation;

import com.azure.management.monitor.ActivityLogAlert;
import com.azure.management.monitor.ActivityLogAlerts;
import com.azure.management.monitor.models.ActivityLogAlertResourceInner;
import com.azure.management.monitor.models.ActivityLogAlertsInner;
import com.azure.management.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;

/**
 * Implementation for {@link ActivityLogAlerts}.
 */
class ActivityLogAlertsImpl
        extends TopLevelModifiableResourcesImpl<
        ActivityLogAlert,
        ActivityLogAlertImpl,
        ActivityLogAlertResourceInner,
        ActivityLogAlertsInner,
        MonitorManager>
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
        if (inner ==  null) {
            return null;
        }
        return new ActivityLogAlertImpl(inner.getName(), inner, this.manager());
    }

    @Override
    public ActivityLogAlertImpl define(String name) {
        return wrapModel(name);
    }
}
