// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.monitor.implementation;

import com.azure.resourcemanager.monitor.MonitorManager;
import com.azure.resourcemanager.monitor.models.ActionGroup;
import com.azure.resourcemanager.monitor.models.ActionGroups;
import com.azure.resourcemanager.monitor.fluent.inner.ActionGroupResourceInner;
import com.azure.resourcemanager.monitor.fluent.ActionGroupsClient;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;
import reactor.core.publisher.Mono;

/** Implementation for {@link ActionGroups}. */
public class ActionGroupsImpl
    extends TopLevelModifiableResourcesImpl<
        ActionGroup, ActionGroupImpl, ActionGroupResourceInner, ActionGroupsClient, MonitorManager>
    implements ActionGroups {

    public ActionGroupsImpl(final MonitorManager monitorManager) {
        super(monitorManager.inner().getActionGroups(), monitorManager);
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
