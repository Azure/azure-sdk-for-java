// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.authorization.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.authorization.AuthorizationManager;
import com.azure.resourcemanager.authorization.fluent.ServicePrincipalsServicePrincipalsClient;
import com.azure.resourcemanager.authorization.fluent.models.MicrosoftGraphServicePrincipalInner;
import com.azure.resourcemanager.authorization.models.ServicePrincipal;
import com.azure.resourcemanager.authorization.models.ServicePrincipals;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.CreatableWrappersImpl;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import reactor.core.publisher.Mono;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;

/** The implementation of ServicePrincipals and its parent interfaces. */
public class ServicePrincipalsImpl
    extends CreatableWrappersImpl<ServicePrincipal, ServicePrincipalImpl, MicrosoftGraphServicePrincipalInner>
    implements ServicePrincipals, HasManager<AuthorizationManager> {
    private final AuthorizationManager manager;

    public ServicePrincipalsImpl(final AuthorizationManager authorizationManager) {
        this.manager = authorizationManager;
    }

    @Override
    public PagedIterable<ServicePrincipal> list() {
        return new PagedIterable<>(listAsync());
    }

    @Override
    public PagedFlux<ServicePrincipal> listAsync() {
        return PagedConverter.mapPage(inner().listServicePrincipalAsync(), this::wrapModel);
    }

    @Override
    protected ServicePrincipalImpl wrapModel(MicrosoftGraphServicePrincipalInner servicePrincipalInner) {
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
        return inner()
            .getServicePrincipalAsync(id)
            .map(this::wrapModel);
    }

    @Override
    public ServicePrincipal getByName(String spn) {
        return getByNameAsync(spn).block();
    }

    @Override
    public Mono<ServicePrincipal> getByNameAsync(final String name) {
        return listByFilterAsync(String.format("displayName eq '%s'", name))
            .singleOrEmpty()
            .switchIfEmpty(
                listByFilterAsync(String.format("servicePrincipalNames/any(c:c eq '%s')", name)).singleOrEmpty());
    }

    @Override
    public ServicePrincipalImpl define(String name) {
        return new ServicePrincipalImpl(new MicrosoftGraphServicePrincipalInner().withDisplayName(name), manager());
    }

    @Override
    protected ServicePrincipalImpl wrapModel(String name) {
        return new ServicePrincipalImpl(new MicrosoftGraphServicePrincipalInner().withDisplayName(name), manager());
    }

    @Override
    public Mono<Void> deleteByIdAsync(String id) {
        return inner().deleteServicePrincipalAsync(id);
    }

    @Override
    public AuthorizationManager manager() {
        return this.manager;
    }

    public ServicePrincipalsServicePrincipalsClient inner() {
        return manager().serviceClient().getServicePrincipalsServicePrincipals();
    }

    @Override
    public PagedIterable<ServicePrincipal> listByFilter(String filter) {
        return new PagedIterable<>(listByFilterAsync(filter));
    }

    @Override
    public PagedFlux<ServicePrincipal> listByFilterAsync(String filter) {
        return PagedConverter.mapPage(inner().listServicePrincipalAsync(null, null, null, null, filter, null, null, null, null),
            this::wrapModel);
    }
}
