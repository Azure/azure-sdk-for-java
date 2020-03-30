/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.graphrbac.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.management.graphrbac.ServicePrincipal;
import com.azure.management.graphrbac.ServicePrincipals;
import com.azure.management.graphrbac.models.ServicePrincipalInner;
import com.azure.management.graphrbac.models.ServicePrincipalsInner;
import com.azure.management.resources.fluentcore.arm.collection.implementation.CreatableWrappersImpl;
import com.azure.management.resources.fluentcore.arm.models.HasManager;
import com.azure.management.resources.fluentcore.model.HasInner;
import reactor.core.publisher.Mono;

/**
 * The implementation of ServicePrincipals and its parent interfaces.
 */
class ServicePrincipalsImpl
        extends CreatableWrappersImpl<ServicePrincipal, ServicePrincipalImpl, ServicePrincipalInner>
        implements ServicePrincipals,
            HasManager<GraphRbacManager>,
            HasInner<ServicePrincipalsInner> {
    private ServicePrincipalsInner innerCollection;
    private GraphRbacManager manager;

    ServicePrincipalsImpl(
            final ServicePrincipalsInner client,
            final GraphRbacManager graphRbacManager) {
        this.innerCollection = client;
        this.manager = graphRbacManager;
    }

    @Override
    public PagedIterable<ServicePrincipal> list() {
        return inner().list(null).mapPage(servicePrincipalInner -> {
            ServicePrincipalImpl servicePrincipal = wrapModel(servicePrincipalInner);
            return servicePrincipal.refreshCredentialsAsync().block();
        });
    }

    @Override
    public PagedFlux<ServicePrincipal> listAsync() {
        return inner().listAsync(null).mapPage(servicePrincipalInner -> {
            ServicePrincipalImpl servicePrincipal = wrapModel(servicePrincipalInner);
            servicePrincipal.refreshCredentialsAsync();
            return servicePrincipal;
        });
    }

    @Override
    protected ServicePrincipalImpl wrapModel(ServicePrincipalInner servicePrincipalInner) {
        if (servicePrincipalInner == null) {
            return null;
        }
        return new ServicePrincipalImpl(servicePrincipalInner, manager());
    }

    @Override
    public ServicePrincipalImpl getById(String id) {
        return (ServicePrincipalImpl) getByIdAsync(id).block();
    }

    @Override
    public Mono<ServicePrincipal> getByIdAsync(String id) {
        return innerCollection.getAsync(id)
                .onErrorResume(GraphErrorException.class, e -> Mono.empty())
                .flatMap(servicePrincipalInner -> new ServicePrincipalImpl(servicePrincipalInner, manager()).refreshCredentialsAsync());
    }

    @Override
    public ServicePrincipal getByName(String spn) {
        return getByNameAsync(spn).block();
    }

    @Override
    public Mono<ServicePrincipal> getByNameAsync(final String name) {
        return inner().listAsync(String.format("servicePrincipalNames/any(c:c eq '%s')", name)).singleOrEmpty()
                .switchIfEmpty(Mono.defer(() -> inner().listAsync(String.format("displayName eq '%s'", name)).singleOrEmpty()))
                .map(servicePrincipalInner -> new ServicePrincipalImpl(servicePrincipalInner, manager()))
                .flatMap(servicePrincipal -> servicePrincipal.refreshCredentialsAsync());
    }

    @Override
    public ServicePrincipalImpl define(String name) {
        return new ServicePrincipalImpl(new ServicePrincipalInner().setDisplayName(name), manager());
    }

    @Override
    protected ServicePrincipalImpl wrapModel(String name) {
        return new ServicePrincipalImpl(new ServicePrincipalInner().setDisplayName(name), manager());
    }

    @Override
    public Mono<Void> deleteByIdAsync(String id) {
        return inner().deleteAsync(id);
    }

    @Override
    public GraphRbacManager manager() {
        return this.manager;
    }

    @Override
    public ServicePrincipalsInner inner() {
        return manager().inner().servicePrincipals();
    }
}
