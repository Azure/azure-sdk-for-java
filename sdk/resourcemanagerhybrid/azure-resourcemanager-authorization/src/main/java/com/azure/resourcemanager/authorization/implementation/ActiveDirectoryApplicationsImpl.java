// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.authorization.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.authorization.AuthorizationManager;
import com.azure.resourcemanager.authorization.models.ActiveDirectoryApplication;
import com.azure.resourcemanager.authorization.models.ActiveDirectoryApplications;
import com.azure.resourcemanager.authorization.fluent.models.ApplicationInner;
import com.azure.resourcemanager.authorization.fluent.ApplicationsClient;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.CreatableResourcesImpl;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import java.util.UUID;

import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;
import reactor.core.publisher.Mono;

import java.util.UUID;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;

/** The implementation of Applications and its parent interfaces. */
public class ActiveDirectoryApplicationsImpl
    extends CreatableResourcesImpl<ActiveDirectoryApplication, ActiveDirectoryApplicationImpl, ApplicationInner>
    implements ActiveDirectoryApplications, HasManager<AuthorizationManager> {
    private ApplicationsClient innerCollection;
    private AuthorizationManager manager;

    public ActiveDirectoryApplicationsImpl(
        final ApplicationsClient client, final AuthorizationManager authorizationManager) {
        this.innerCollection = client;
        this.manager = authorizationManager;
    }

    @Override
    public PagedIterable<ActiveDirectoryApplication> list() {
        return new PagedIterable<>(listAsync());
    }

    @Override
    public PagedFlux<ActiveDirectoryApplication> listAsync() {
        return PagedConverter.flatMapPage(inner().listAsync(this.manager.tenantId()), applicationInner -> {
            ActiveDirectoryApplicationImpl application = this.wrapModel(applicationInner);
            return application.refreshCredentialsAsync().thenReturn(application);
        });
    }

    @Override
    protected ActiveDirectoryApplicationImpl wrapModel(ApplicationInner applicationInner) {
        if (applicationInner == null) {
            return null;
        }
        return new ActiveDirectoryApplicationImpl(applicationInner, manager());
    }

    @Override
    public ActiveDirectoryApplicationImpl getById(String id) {
        return (ActiveDirectoryApplicationImpl) getByIdAsync(id).block();
    }

    @Override
    public Mono<ActiveDirectoryApplication> getByIdAsync(String id) {
        return innerCollection
            .getAsync(id, this.manager.tenantId())
            .flatMap(
                applicationInner ->
                    new ActiveDirectoryApplicationImpl(applicationInner, manager()).refreshCredentialsAsync());
    }

    @Override
    public ActiveDirectoryApplication getByName(String spn) {
        return getByNameAsync(spn).block();
    }

    @Override
    public Mono<ActiveDirectoryApplication> getByNameAsync(String name) {
        final String trimmed = name.replaceFirst("^'+", "").replaceAll("'+$", "");
        return inner()
            .listAsync(this.manager.tenantId(), String.format("displayName eq '%s'", trimmed))
            .singleOrEmpty()
            .switchIfEmpty(Mono.defer(() -> {
                try {
                    UUID.fromString(trimmed);
                } catch (IllegalArgumentException e) {
                    return Mono.empty();
                }

                return inner()
                    .listAsync(this.manager.tenantId(), String.format("appId eq '%s'", trimmed))
                    .singleOrEmpty();
            }))
            .map(applicationInner -> new ActiveDirectoryApplicationImpl(applicationInner, manager()))
            .flatMap(activeDirectoryApplication -> activeDirectoryApplication.refreshCredentialsAsync());
    }

    @Override
    protected ActiveDirectoryApplicationImpl wrapModel(String name) {
        return new ActiveDirectoryApplicationImpl(new ApplicationInner().withDisplayName(name), manager());
    }

    @Override
    public Mono<Void> deleteByIdAsync(String id) {
        return inner().deleteAsync(id, this.manager.tenantId());
    }

    @Override
    public ActiveDirectoryApplicationImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    public AuthorizationManager manager() {
        return this.manager;
    }

    public ApplicationsClient inner() {
        return this.innerCollection;
    }

    @Override
    public PagedIterable<ActiveDirectoryApplication> listByFilter(String filter) {
        return new PagedIterable<>(listByFilterAsync(filter));
    }

    @Override
    public PagedFlux<ActiveDirectoryApplication> listByFilterAsync(String filter) {
        return PagedConverter.flatMapPage(inner().listAsync(this.manager.tenantId(), filter), applicationInner -> {
            ActiveDirectoryApplicationImpl application = this.wrapModel(applicationInner);
            return application.refreshCredentialsAsync().thenReturn(application);
        });
    }
}
