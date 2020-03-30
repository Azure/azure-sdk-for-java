/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.graphrbac.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.management.graphrbac.ActiveDirectoryGroup;
import com.azure.management.graphrbac.ActiveDirectoryGroups;
import com.azure.management.graphrbac.models.ADGroupInner;
import com.azure.management.graphrbac.models.GroupsInner;
import com.azure.management.resources.fluentcore.arm.collection.implementation.CreatableWrappersImpl;
import reactor.core.publisher.Mono;

/**
 * The implementation of Users and its parent interfaces.
 */
class ActiveDirectoryGroupsImpl
        extends CreatableWrappersImpl<ActiveDirectoryGroup, ActiveDirectoryGroupImpl, ADGroupInner>
        implements ActiveDirectoryGroups {
    private final GraphRbacManager manager;

    ActiveDirectoryGroupsImpl(final GraphRbacManager manager) {
        this.manager = manager;
    }

    @Override
    public PagedIterable<ActiveDirectoryGroup> list() {
        return wrapList(this.manager.inner().groups().list(null));
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
        return manager.inner().groups().getAsync(id)
                .onErrorResume(GraphErrorException.class, e -> Mono.empty())
                .map(groupInner -> new ActiveDirectoryGroupImpl(groupInner, manager()));
    }

    @Override
    public PagedFlux<ActiveDirectoryGroup> listAsync() {
        return wrapPageAsync(manager().inner().groups().listAsync(null));
    }

    @Override
    public Mono<ActiveDirectoryGroup> getByNameAsync(String name) {
        return manager().inner().groups().listAsync(String.format("displayName eq '%s'", name))
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
        return wrapModel(new ADGroupInner().setDisplayName(name));
    }

    @Override
    public Mono<Void> deleteByIdAsync(String id) {
        return manager().inner().groups().deleteAsync(id);
    }

    @Override
    public GraphRbacManager manager() {
        return this.manager;
    }

    @Override
    public GroupsInner inner() {
        return manager().inner().groups();
    }
}
