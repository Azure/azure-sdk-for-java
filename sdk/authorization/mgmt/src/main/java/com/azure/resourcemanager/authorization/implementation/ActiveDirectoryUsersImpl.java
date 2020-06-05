// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.authorization.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.authorization.GraphRbacManager;
import com.azure.resourcemanager.authorization.models.ActiveDirectoryUser;
import com.azure.resourcemanager.authorization.models.ActiveDirectoryUsers;
import com.azure.resourcemanager.authorization.models.GraphErrorException;
import com.azure.resourcemanager.authorization.fluent.inner.UserInner;
import com.azure.resourcemanager.authorization.fluent.UsersClient;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.CreatableWrappersImpl;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import reactor.core.publisher.Mono;

/** The implementation of Users and its parent interfaces. */
class ActiveDirectoryUsersImpl extends CreatableWrappersImpl<ActiveDirectoryUser, ActiveDirectoryUserImpl, UserInner>
    implements ActiveDirectoryUsers, HasInner<UsersClient> {
    private final GraphRbacManager manager;

    ActiveDirectoryUsersImpl(final GraphRbacManager manager) {
        this.manager = manager;
    }

    @Override
    public PagedIterable<ActiveDirectoryUser> list() {
        return wrapList(this.manager().inner().users().list());
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
            .inner()
            .users()
            .getAsync(id)
            .onErrorResume(GraphErrorException.class, e -> Mono.empty())
            .map(userInner -> new ActiveDirectoryUserImpl(userInner, manager()));
    }

    @Override
    public ActiveDirectoryUserImpl getByName(String upn) {
        return (ActiveDirectoryUserImpl) getByNameAsync(upn).block();
    }

    @Override
    public Mono<ActiveDirectoryUser> getByNameAsync(final String name) {
        return manager()
            .inner()
            .users()
            .getAsync(name)
            .onErrorResume(
                GraphErrorException.class,
                e -> {
                    if (name.contains("@")) {
                        return manager()
                            .inner()
                            .users()
                            .listAsync(
                                String
                                    .format("mail eq '%s' or mailNickName eq '%s#EXT#'", name, name.replace("@", "_")),
                                null)
                            .singleOrEmpty();
                    } else {
                        return manager()
                            .inner()
                            .users()
                            .listAsync(String.format("displayName eq '%s'", name), null)
                            .singleOrEmpty();
                    }
                })
            .map(userInner -> new ActiveDirectoryUserImpl(userInner, manager()));
    }

    @Override
    public PagedFlux<ActiveDirectoryUser> listAsync() {
        return wrapPageAsync(this.inner().listAsync());
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
        return manager().inner().users().deleteAsync(id);
    }

    @Override
    public GraphRbacManager manager() {
        return this.manager;
    }

    @Override
    public UsersClient inner() {
        return manager().inner().users();
    }
}
