/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.resourcemanager.eventhubs.implementation;

import com.azure.resourcemanager.eventhubs.EventHubManager;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.azure.resourcemanager.eventhubs.models.EventHubNamespace;
import com.azure.resourcemanager.eventhubs.models.EventHubNamespaceAuthorizationRules;
import com.azure.resourcemanager.eventhubs.models.EventHubNamespaces;
import com.azure.resourcemanager.eventhubs.models.EventHubs;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;

/**
 * Implementation for {@link EventHubNamespaces}.
 */
@LangDefinition
class EventHubNamespacesImpl extends TopLevelModifiableResourcesImpl<
        EventHubNamespace,
        EventHubNamespaceImpl,
        EHNamespaceInner,
        NamespacesInner,
    EventHubManager>
    implements EventHubNamespaces {

    protected EventHubNamespacesImpl(EventHubManager manager) {
        super(manager.inner().namespaces(), manager);
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
