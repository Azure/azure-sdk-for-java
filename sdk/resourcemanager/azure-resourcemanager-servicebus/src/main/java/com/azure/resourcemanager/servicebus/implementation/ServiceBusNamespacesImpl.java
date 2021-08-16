// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.servicebus.implementation;

import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;
import com.azure.resourcemanager.servicebus.ServiceBusManager;
import com.azure.resourcemanager.servicebus.fluent.NamespacesClient;
import com.azure.resourcemanager.servicebus.fluent.models.SBNamespaceInner;
import com.azure.resourcemanager.servicebus.models.CheckNameAvailabilityResult;
import com.azure.resourcemanager.servicebus.models.ServiceBusNamespace;
import com.azure.resourcemanager.servicebus.models.ServiceBusNamespaces;
import reactor.core.publisher.Mono;

/**
 * Implementation for ServiceBusNamespaces.
 */
public final class ServiceBusNamespacesImpl extends TopLevelModifiableResourcesImpl<
    ServiceBusNamespace,
    ServiceBusNamespaceImpl,
    SBNamespaceInner,
    NamespacesClient,
    ServiceBusManager>
    implements ServiceBusNamespaces {

    public ServiceBusNamespacesImpl(NamespacesClient innerCollection, ServiceBusManager manager) {
        super(innerCollection, manager);
    }

    @Override
    public ServiceBusNamespace.DefinitionStages.Blank define(String name) {
        return wrapModel(name);
    }

    @Override
    public CheckNameAvailabilityResult checkNameAvailability(String name) {
        return this.checkNameAvailabilityAsync(name).block();
    }

    @Override
    public Mono<CheckNameAvailabilityResult> checkNameAvailabilityAsync(String name) {
        return this.inner().checkNameAvailabilityAsync(name)
            .map(inner -> new CheckNameAvailabilityResultImpl(inner));
    }

    @Override
    protected ServiceBusNamespaceImpl wrapModel(String name) {
        return new ServiceBusNamespaceImpl(name,
                new SBNamespaceInner(),
                this.manager());
    }

    @Override
    protected ServiceBusNamespaceImpl wrapModel(SBNamespaceInner inner) {
        return new ServiceBusNamespaceImpl(inner.name(),
                inner,
                this.manager());
    }
}
