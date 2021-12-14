// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.authorization.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.authorization.AuthorizationManager;
import com.azure.resourcemanager.authorization.fluent.UsersClient;
import com.azure.resourcemanager.authorization.fluent.models.UserInner;
import com.azure.resourcemanager.authorization.models.ActiveDirectoryUser;
import com.azure.resourcemanager.authorization.models.ActiveDirectoryUsers;
import com.azure.resourcemanager.authorization.models.GraphErrorException;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.CreatableWrappersImpl;
import reactor.core.publisher.Mono;

/** The implementation of Users and its parent interfaces. */
public class ActiveDirectoryUsersImpl
    extends CreatableWrappersImpl<ActiveDirectoryUser, ActiveDirectoryUserImpl, UserInner>
    implements ActiveDirectoryUsers {
    private final AuthorizationManager manager;

    public ActiveDirectoryUsersImpl(final AuthorizationManager manager) {
        this.manager = manager;
    }

    @Override
    public PagedIterable<ActiveDirectoryUser> list() {
        return wrapList(this.manager().serviceClient().getUsers().list(this.manager.tenantId()));
    }

    @Override
    protected ActiveDirectoryUserImpl wrapModel(UserInner userInner) {
        if (userInner == null) {
            return null;
        }
        return new ActiveDirectoryUserImpl(userInner, manager());
    }

    @Override
    public ActiveDirectoryUserImpl getById(String objectId) {
        return (ActiveDirectoryUserImpl) getByIdAsync(objectId).block();
    }

    @Override
    public Mono<ActiveDirectoryUser> getByIdAsync(String id) {
        return manager()
            .serviceClient()
            .getUsers()
            .getAsync(id, this.manager.tenantId())
            .map(userInner -> new ActiveDirectoryUserImpl(userInner, manager()));
    }

    @Override
    public ActiveDirectoryUserImpl getByName(String upn) {
        return (ActiveDirectoryUserImpl) getByNameAsync(upn).block();
    }

    @Override
    public Mono<ActiveDirectoryUser> getByNameAsync(final String name) {
        return manager()
            .serviceClient()
            .getUsers()
            .getAsync(name, this.manager.tenantId())
            .onErrorResume(
                GraphErrorException.class,
                e -> manager()
                    .serviceClient()
                    .getUsers()
                    .listAsync(
                        this.manager.tenantId(),
                        name.contains("@")
                            ? String.format("mail eq '%s' or mailNickName eq '%s#EXT#'", name, name.replace("@", "_"))
                            : String.format("displayName eq '%s'", name),
                        null,
                        null)
                    .singleOrEmpty())
            .map(userInner -> new ActiveDirectoryUserImpl(userInner, manager()));
    }

    @Override
    public PagedFlux<ActiveDirectoryUser> listAsync() {
        return wrapPageAsync(this.inner().listAsync(this.manager.tenantId()));
    }

    @Override
    public ActiveDirectoryUserImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    protected ActiveDirectoryUserImpl wrapModel(String name) {
        return new ActiveDirectoryUserImpl(new UserInner().withDisplayName(name), manager());
    }

    @Override
    public Mono<Void> deleteByIdAsync(String id) {
        return manager().serviceClient().getUsers().deleteAsync(id, this.manager.tenantId());
    }

    @Override
    public AuthorizationManager manager() {
        return this.manager;
    }

    public UsersClient inner() {
        return manager().serviceClient().getUsers();
    }

    @Override
    public PagedIterable<ActiveDirectoryUser> listByFilter(String filter) {
        return new PagedIterable<>(listByFilterAsync(filter));
    }

    @Override
    public PagedFlux<ActiveDirectoryUser> listByFilterAsync(String filter) {
        return inner().listAsync(this.manager.tenantId(), filter, null, null).mapPage(this::wrapModel);
    }
}
