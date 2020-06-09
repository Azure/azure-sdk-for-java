// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.monitor.implementation;

import com.azure.resourcemanager.monitor.ActionGroup;
import com.azure.resourcemanager.monitor.ActionGroups;
import com.azure.resourcemanager.monitor.models.ActionGroupResourceInner;
import com.azure.resourcemanager.monitor.models.ActionGroupsInner;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;
import reactor.core.publisher.Mono;

/** Implementation for {@link ActionGroups}. */
class ActionGroupsImpl
    extends TopLevelModifiableResourcesImpl<
        ActionGroup, ActionGroupImpl, ActionGroupResourceInner, ActionGroupsInner, MonitorManager>
    implements ActionGroups {

    ActionGroupsImpl(final MonitorManager monitorManager) {
        super(monitorManager.inner().actionGroups(), monitorManager);
    }

    @Override
    protected ActionGroupImpl wrapModel(String name) {
        return new ActionGroupImpl(name, new ActionGroupResourceInner(), this.manager());
    }

    @Override
    protected ActionGroupImpl wrapModel(ActionGroupResourceInner inner) {
        if (inner == null) {
            return null;
        }
        return new ActionGroupImpl(inner.name(), inner, this.manager());
    }

    @Override
    public ActionGroupImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    public void enableReceiver(String resourceGroupName, String actionGroupName, String receiverName) {
        this.inner().enableReceiver(resourceGroupName, actionGroupName, receiverName);
    }

    @Override
    public Mono<Void> enableReceiverAsync(String resourceGroupName, String actionGroupName, String receiverName) {
        return this.inner().enableReceiverAsync(resourceGroupName, actionGroupName, receiverName);
    }
}
