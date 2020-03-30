/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.graphrbac.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.management.graphrbac.ActiveDirectoryUser;
import com.azure.management.graphrbac.ActiveDirectoryUsers;
import com.azure.management.graphrbac.models.UserInner;
import com.azure.management.graphrbac.models.UsersInner;
import com.azure.management.resources.fluentcore.arm.collection.implementation.CreatableWrappersImpl;
import com.azure.management.resources.fluentcore.model.HasInner;
import reactor.core.publisher.Mono;

/**
 * The implementation of Users and its parent interfaces.
 */
class ActiveDirectoryUsersImpl
        extends CreatableWrappersImpl<ActiveDirectoryUser, ActiveDirectoryUserImpl, UserInner>
        implements ActiveDirectoryUsers,
            HasInner<UsersInner> {
    private final GraphRbacManager manager;

    ActiveDirectoryUsersImpl(
            final GraphRbacManager manager) {
        this.manager = manager;
    }

    @Override
    public PagedIterable<ActiveDirectoryUser> list() {
        return wrapList(this.manager().inner().users().list(null));
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
        return manager().inner().users().getAsync(id)
                .onErrorResume(GraphErrorException.class, e -> Mono.empty())
                .map(userInner -> new ActiveDirectoryUserImpl(userInner, manager()));
    }

    @Override
    public ActiveDirectoryUserImpl getByName(String upn) {
        return (ActiveDirectoryUserImpl) getByNameAsync(upn).block();
    }

    @Override
    public Mono<ActiveDirectoryUser> getByNameAsync(final String name) {
        return manager().inner().users().getAsync(name)
                .onErrorResume(GraphErrorException.class, e -> {
                    if (name.contains("@")) {
                        return manager().inner().users().listAsync(
                                String.format("mail eq '%s' or mailNickName eq '%s#EXT#'", name, name.replace("@", "_"))
                        ).singleOrEmpty();
                    } else {
                        return manager().inner().users().listAsync(
                                String.format("displayName eq '%s'", name)
                        ).singleOrEmpty();
                    }
                })
                .map(userInner -> new ActiveDirectoryUserImpl(userInner, manager()));
    }

    @Override
    public PagedFlux<ActiveDirectoryUser> listAsync() {
        return wrapPageAsync(this.inner().listAsync(null));
    }

    @Override
    public ActiveDirectoryUserImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    protected ActiveDirectoryUserImpl wrapModel(String name) {
        return new ActiveDirectoryUserImpl(new UserInner().setDisplayName(name), manager());
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
    public UsersInner inner() {
        return manager().inner().users();
    }
}
