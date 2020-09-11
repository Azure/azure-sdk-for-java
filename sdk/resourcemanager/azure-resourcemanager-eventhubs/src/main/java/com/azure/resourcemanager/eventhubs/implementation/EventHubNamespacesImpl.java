// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.eventhubs.implementation;

import com.azure.resourcemanager.eventhubs.EventHubsManager;
import com.azure.resourcemanager.eventhubs.fluent.NamespacesClient;
import com.azure.resourcemanager.eventhubs.fluent.inner.EHNamespaceInner;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;
import com.azure.resourcemanager.eventhubs.models.EventHubNamespace;
import com.azure.resourcemanager.eventhubs.models.EventHubNamespaceAuthorizationRules;
import com.azure.resourcemanager.eventhubs.models.EventHubNamespaces;
import com.azure.resourcemanager.eventhubs.models.EventHubs;

/**
 * Implementation for {@link EventHubNamespaces}.
 */
public final class EventHubNamespacesImpl
    extends TopLevelModifiableResourcesImpl<
        EventHubNamespace,
        EventHubNamespaceImpl,
        EHNamespaceInner,
        NamespacesClient,
    EventHubsManager>
    implements EventHubNamespaces {

    public EventHubNamespacesImpl(EventHubsManager manager) {
        super(manager.inner().getNamespaces(), manager);
    }

    @Override
    protected EventHubNamespaceImpl wrapModel(String name) {
        return new EventHubNamespaceImpl(name, new EHNamespaceInner(), this.manager());
    }

    @Override
    protected EventHubNamespaceImpl wrapModel(EHNamespaceInner inner) {
        return new EventHubNamespaceImpl(inner.name(), inner, this.manager());
    }

    @Override
    public EventHubNamespaceImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    public EventHubNamespaceAuthorizationRules authorizationRules() {
        return this.manager().namespaceAuthorizationRules();
    }

    @Override
    public EventHubs eventHubs() {
        return this.manager().eventHubs();
    }
}
