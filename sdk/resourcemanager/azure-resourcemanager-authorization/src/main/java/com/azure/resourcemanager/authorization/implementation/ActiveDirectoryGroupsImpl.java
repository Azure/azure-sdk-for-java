// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.authorization.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.authorization.AuthorizationManager;
import com.azure.resourcemanager.authorization.fluent.GroupsGroupsClient;
import com.azure.resourcemanager.authorization.fluent.models.MicrosoftGraphGroupInner;
import com.azure.resourcemanager.authorization.models.ActiveDirectoryGroup;
import com.azure.resourcemanager.authorization.models.ActiveDirectoryGroups;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.CreatableWrappersImpl;
import reactor.core.publisher.Mono;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;

/** The implementation of Users and its parent interfaces. */
public class ActiveDirectoryGroupsImpl
    extends CreatableWrappersImpl<ActiveDirectoryGroup, ActiveDirectoryGroupImpl, MicrosoftGraphGroupInner>
    implements ActiveDirectoryGroups {
    private final AuthorizationManager manager;

    public ActiveDirectoryGroupsImpl(final AuthorizationManager manager) {
        this.manager = manager;
    }

    @Override
    public PagedIterable<ActiveDirectoryGroup> list() {
        return new PagedIterable<>(listAsync());
    }

    @Override
    protected ActiveDirectoryGroupImpl wrapModel(MicrosoftGraphGroupInner groupInner) {
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
        return inner().getGroupAsync(id)
            .map(groupInner -> new ActiveDirectoryGroupImpl(groupInner, manager()));
    }

    @Override
    public PagedFlux<ActiveDirectoryGroup> listAsync() {
        return wrapPageAsync(inner().listGroupAsync());
    }

    @Override
    public Mono<ActiveDirectoryGroup> getByNameAsync(String name) {
        return listByFilterAsync(String.format("displayName eq '%s'", name))
            .singleOrEmpty();
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
        return wrapModel(new MicrosoftGraphGroupInner().withDisplayName(name));
    }

    @Override
    public Mono<Void> deleteByIdAsync(String id) {
        return inner().deleteGroupAsync(id);
    }

    @Override
    public AuthorizationManager manager() {
        return this.manager;
    }

    public GroupsGroupsClient inner() {
        return manager().serviceClient().getGroupsGroups();
    }

    @Override
    public PagedIterable<ActiveDirectoryGroup> listByFilter(String filter) {
        return new PagedIterable<>(listByFilterAsync(filter));
    }

    @Override
    public PagedFlux<ActiveDirectoryGroup> listByFilterAsync(String filter) {
        return PagedConverter.mapPage(inner().listGroupAsync(null, null, null, null, filter, null, null, null, null),
            this::wrapModel);
    }
}
