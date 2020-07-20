// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.authorization.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.authorization.AuthorizationManager;
import com.azure.resourcemanager.authorization.models.ActiveDirectoryApplication;
import com.azure.resourcemanager.authorization.models.ActiveDirectoryApplications;
import com.azure.resourcemanager.authorization.fluent.inner.ApplicationInner;
import com.azure.resourcemanager.authorization.fluent.ApplicationsClient;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.CreatableResourcesImpl;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import java.util.UUID;
import reactor.core.publisher.Mono;

/** The implementation of Applications and its parent interfaces. */
public class ActiveDirectoryApplicationsImpl
    extends CreatableResourcesImpl<ActiveDirectoryApplication, ActiveDirectoryApplicationImpl, ApplicationInner>
    implements ActiveDirectoryApplications, HasManager<AuthorizationManager>, HasInner<ApplicationsClient> {
    private ApplicationsClient innerCollection;
    private AuthorizationManager manager;

    public ActiveDirectoryApplicationsImpl(
        final ApplicationsClient client, final AuthorizationManager authorizationManager) {
        this.innerCollection = client;
        this.manager = authorizationManager;
    }

    @Override
    public PagedIterable<ActiveDirectoryApplication> list() {
        return this
            .innerCollection
            .list(null)
            .mapPage(
                inner -> {
                    ActiveDirectoryApplicationImpl application = wrapModel(inner);
                    return application.refreshCredentialsAsync().block();
                });
    }

    @Override
    public PagedFlux<ActiveDirectoryApplication> listAsync() {
        return this
            .innerCollection
            .listAsync(null)
            .mapPage(
                inner -> {
                    ActiveDirectoryApplicationImpl application = wrapModel(inner);
                    application.refreshCredentialsAsync();
                    return application;
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
            .getAsync(id)
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
            .listAsync(String.format("displayName eq '%s'", trimmed))
            .singleOrEmpty()
            .switchIfEmpty(
                Mono
                    .defer(
                        () -> {
                            try {
                                UUID.fromString(trimmed);
                                return inner().listAsync(String.format("appId eq '%s'", trimmed)).singleOrEmpty();
                            } catch (IllegalArgumentException e) {
                                return Mono.empty();
                            }
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
        return inner().deleteAsync(id);
    }

    @Override
    public ActiveDirectoryApplicationImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    public AuthorizationManager manager() {
        return this.manager;
    }

    @Override
    public ApplicationsClient inner() {
        return this.innerCollection;
    }
}
