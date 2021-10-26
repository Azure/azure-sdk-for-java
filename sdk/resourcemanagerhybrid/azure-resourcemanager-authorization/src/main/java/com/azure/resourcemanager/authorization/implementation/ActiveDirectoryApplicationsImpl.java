// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.authorization.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.authorization.AuthorizationManager;
import com.azure.resourcemanager.authorization.fluent.ApplicationsApplicationsClient;
import com.azure.resourcemanager.authorization.fluent.models.MicrosoftGraphApplicationInner;
import com.azure.resourcemanager.authorization.models.ActiveDirectoryApplication;
import com.azure.resourcemanager.authorization.models.ActiveDirectoryApplications;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.CreatableResourcesImpl;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import reactor.core.publisher.Mono;

import java.util.UUID;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;

/** The implementation of Applications and its parent interfaces. */
public class ActiveDirectoryApplicationsImpl
    extends CreatableResourcesImpl<
        ActiveDirectoryApplication,
        ActiveDirectoryApplicationImpl,
        MicrosoftGraphApplicationInner>
    implements ActiveDirectoryApplications, HasManager<AuthorizationManager> {
    private AuthorizationManager manager;

    public ActiveDirectoryApplicationsImpl(final AuthorizationManager authorizationManager) {
        this.manager = authorizationManager;
    }

    @Override
    public PagedIterable<ActiveDirectoryApplication> list() {
        return new PagedIterable<>(listAsync());
    }

    @Override
    public PagedFlux<ActiveDirectoryApplication> listAsync() {
        return PagedConverter.mapPage(inner().listApplicationAsync(), this::wrapModel);
    }

    @Override
    protected ActiveDirectoryApplicationImpl wrapModel(MicrosoftGraphApplicationInner applicationInner) {
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
        return inner()
            .getApplicationAsync(id)
            .map(this::wrapModel);
    }

    @Override
    public ActiveDirectoryApplication getByName(String spn) {
        return getByNameAsync(spn).block();
    }

    @Override
    public Mono<ActiveDirectoryApplication> getByNameAsync(String name) {
        final String trimmed = name.replaceFirst("^'+", "").replaceAll("'+$", "");
        return listByFilterAsync(String.format("displayName eq '%s'", trimmed))
            .singleOrEmpty()
            .switchIfEmpty(Mono.defer(() -> {
                try {
                    UUID.fromString(trimmed);
                } catch (IllegalArgumentException e) {
                    // abort if name does not look like an application ID
                    return Mono.empty();
                }
                return listByFilterAsync(String.format("appId eq '%s'", trimmed)).singleOrEmpty();
            }));
    }

    @Override
    protected ActiveDirectoryApplicationImpl wrapModel(String name) {
        return new ActiveDirectoryApplicationImpl(
            new MicrosoftGraphApplicationInner().withDisplayName(name), manager());
    }

    @Override
    public Mono<Void> deleteByIdAsync(String id) {
        return inner().deleteApplicationAsync(id);
    }

    @Override
    public ActiveDirectoryApplicationImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    public AuthorizationManager manager() {
        return this.manager;
    }

    public ApplicationsApplicationsClient inner() {
        return manager().serviceClient().getApplicationsApplications();
    }

    @Override
    public PagedIterable<ActiveDirectoryApplication> listByFilter(String filter) {
        return new PagedIterable<>(listByFilterAsync(filter));
    }

    @Override
    public PagedFlux<ActiveDirectoryApplication> listByFilterAsync(String filter) {
        return PagedConverter.mapPage(inner().listApplicationAsync(null, null, null, null, filter, null, null, null, null),
            this::wrapModel);
    }
}
