// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.authorization.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.authorization.AuthorizationManager;
import com.azure.resourcemanager.authorization.models.ActiveDirectoryGroup;
import com.azure.resourcemanager.authorization.models.ActiveDirectoryGroups;
import com.azure.resourcemanager.authorization.models.GraphErrorException;
import com.azure.resourcemanager.authorization.fluent.inner.ADGroupInner;
import com.azure.resourcemanager.authorization.fluent.GroupsClient;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.CreatableWrappersImpl;
import reactor.core.publisher.Mono;

/** The implementation of Users and its parent interfaces. */
public class ActiveDirectoryGroupsImpl
    extends CreatableWrappersImpl<ActiveDirectoryGroup, ActiveDirectoryGroupImpl, ADGroupInner>
    implements ActiveDirectoryGroups {
    private final AuthorizationManager manager;

    public ActiveDirectoryGroupsImpl(final AuthorizationManager manager) {
        this.manager = manager;
    }

    @Override
    public PagedIterable<ActiveDirectoryGroup> list() {
        return wrapList(this.manager.inner().getGroups().list(null));
    }

    @Override
    protected ActiveDirectoryGroupImpl wrapModel(ADGroupInner groupInner) {
        if (groupInner == null) {
            return null;
        }
        return new ActiveDirectoryGroupImpl(groupInner, manager());
    }

    @Override
    public ActiveDirectoryGroupImpl getById(String objectId) {
        return (ActiveDirectoryGroupImpl) getByIdAsync(objectId).block();
    }

    @Override
    public Mono<ActiveDirectoryGroup> getByIdAsync(String id) {
        return manager
            .inner()
            .getGroups()
            .getAsync(id)
            .onErrorResume(GraphErrorException.class, e -> Mono.empty())
            .map(groupInner -> new ActiveDirectoryGroupImpl(groupInner, manager()));
    }

    @Override
    public PagedFlux<ActiveDirectoryGroup> listAsync() {
        return wrapPageAsync(manager().inner().getGroups().listAsync(null));
    }

    @Override
    public Mono<ActiveDirectoryGroup> getByNameAsync(String name) {
        return manager()
            .inner()
            .getGroups()
            .listAsync(String.format("displayName eq '%s'", name))
            .singleOrEmpty()
            .map(adGroupInner -> new ActiveDirectoryGroupImpl(adGroupInner, manager()));
    }

    @Override
    public ActiveDirectoryGroup getByName(String name) {
        return getByNameAsync(name).block();
    }

    @Override
    public ActiveDirectoryGroupImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    protected ActiveDirectoryGroupImpl wrapModel(String name) {
        return wrapModel(new ADGroupInner().withDisplayName(name));
    }

    @Override
    public Mono<Void> deleteByIdAsync(String id) {
        return manager().inner().getGroups().deleteAsync(id);
    }

    @Override
    public AuthorizationManager manager() {
        return this.manager;
    }

    @Override
    public GroupsClient inner() {
        return manager().inner().getGroups();
    }
}
