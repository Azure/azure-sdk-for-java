// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.authorization.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.authorization.AuthorizationManager;
import com.azure.resourcemanager.authorization.models.ServicePrincipal;
import com.azure.resourcemanager.authorization.models.ServicePrincipals;
import com.azure.resourcemanager.authorization.fluent.models.ServicePrincipalInner;
import com.azure.resourcemanager.authorization.fluent.ServicePrincipalsClient;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.CreatableWrappersImpl;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;
import reactor.core.publisher.Mono;

/** The implementation of ServicePrincipals and its parent interfaces. */
public class ServicePrincipalsImpl
    extends CreatableWrappersImpl<ServicePrincipal, ServicePrincipalImpl, ServicePrincipalInner>
    implements ServicePrincipals, HasManager<AuthorizationManager> {
    private ServicePrincipalsClient innerCollection;
    private AuthorizationManager manager;

    public ServicePrincipalsImpl(
        final ServicePrincipalsClient client, final AuthorizationManager authorizationManager) {
        this.innerCollection = client;
        this.manager = authorizationManager;
    }

    @Override
    public PagedIterable<ServicePrincipal> list() {
        return new PagedIterable<>(listAsync());
    }

    @Override
    public PagedFlux<ServicePrincipal> listAsync() {
        return PagedConverter.flatMapPage(inner().listAsync(), servicePrincipalInner -> {
            ServicePrincipalImpl servicePrincipal = this.wrapModel(servicePrincipalInner);
            return servicePrincipal.refreshCredentialsAsync().thenReturn(servicePrincipal);
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
        return innerCollection
            .getAsync(id)
            .flatMap(
                servicePrincipalInner ->
                    new ServicePrincipalImpl(servicePrincipalInner, manager()).refreshCredentialsAsync());
    }

    @Override
    public ServicePrincipal getByName(String spn) {
        return getByNameAsync(spn).block();
    }

    @Override
    public Mono<ServicePrincipal> getByNameAsync(final String name) {
        return inner()
            .listAsync(String.format("servicePrincipalNames/any(c:c eq '%s')", name))
            .singleOrEmpty()
            .switchIfEmpty(
                Mono.defer(() -> inner().listAsync(String.format("displayName eq '%s'", name)).singleOrEmpty()))
            .map(servicePrincipalInner -> new ServicePrincipalImpl(servicePrincipalInner, manager()))
            .flatMap(servicePrincipal -> servicePrincipal.refreshCredentialsAsync());
    }

    @Override
    public ServicePrincipalImpl define(String name) {
        return new ServicePrincipalImpl(new ServicePrincipalInner().withDisplayName(name), manager());
    }

    @Override
    protected ServicePrincipalImpl wrapModel(String name) {
        return new ServicePrincipalImpl(new ServicePrincipalInner().withDisplayName(name), manager());
    }

    @Override
    public Mono<Void> deleteByIdAsync(String id) {
        return inner().deleteAsync(id);
    }

    @Override
    public AuthorizationManager manager() {
        return this.manager;
    }

    public ServicePrincipalsClient inner() {
        return manager().serviceClient().getServicePrincipals();
    }

    @Override
    public PagedIterable<ServicePrincipal> listByFilter(String filter) {
        return new PagedIterable<>(listByFilterAsync(filter));
    }

    @Override
    public PagedFlux<ServicePrincipal> listByFilterAsync(String filter) {
        return PagedConverter.flatMapPage(inner().listAsync(filter), servicePrincipalInner -> {
            ServicePrincipalImpl servicePrincipal = this.wrapModel(servicePrincipalInner);
            return servicePrincipal.refreshCredentialsAsync().thenReturn(servicePrincipal);
        });
    }
}
