// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.authorization.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.authorization.AuthorizationManager;
import com.azure.resourcemanager.authorization.fluent.UsersUsersClient;
import com.azure.resourcemanager.authorization.fluent.models.MicrosoftGraphUserInner;
import com.azure.resourcemanager.authorization.models.ActiveDirectoryUser;
import com.azure.resourcemanager.authorization.models.ActiveDirectoryUsers;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.CreatableWrappersImpl;
import reactor.core.publisher.Mono;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;

/** The implementation of Users and its parent interfaces. */
public class ActiveDirectoryUsersImpl
    extends CreatableWrappersImpl<ActiveDirectoryUser, ActiveDirectoryUserImpl, MicrosoftGraphUserInner>
    implements ActiveDirectoryUsers {
    private final AuthorizationManager manager;

    public ActiveDirectoryUsersImpl(final AuthorizationManager manager) {
        this.manager = manager;
    }

    @Override
    public PagedIterable<ActiveDirectoryUser> list() {
        return new PagedIterable<>(listAsync());
    }

    @Override
    protected ActiveDirectoryUserImpl wrapModel(MicrosoftGraphUserInner userInner) {
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
        return inner().getUserAsync(id)
            .map(userInner -> new ActiveDirectoryUserImpl(userInner, manager()));
    }

    @Override
    public ActiveDirectoryUserImpl getByName(String upn) {
        return (ActiveDirectoryUserImpl) getByNameAsync(upn).block();
    }

    @Override
    public Mono<ActiveDirectoryUser> getByNameAsync(final String name) {
        return inner().getUserAsync(name)
            .map(userInner -> (ActiveDirectoryUser) new ActiveDirectoryUserImpl(userInner, manager()))
            .onErrorResume(
                ManagementException.class,
                e -> {
                    if (name.contains("@")) {
                        return listByFilterAsync(
                                String
                                    .format("mail eq '%s' or mailNickName eq '%s#EXT#'", name, name.replace("@", "_")))
                            .singleOrEmpty();
                    } else {
                        return listByFilterAsync(String.format("displayName eq '%s'", name))
                            .singleOrEmpty();
                    }
                });
    }

    @Override
    public PagedFlux<ActiveDirectoryUser> listAsync() {
        return wrapPageAsync(this.inner().listUserAsync());
    }

    @Override
    public ActiveDirectoryUserImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    protected ActiveDirectoryUserImpl wrapModel(String name) {
        return new ActiveDirectoryUserImpl(new MicrosoftGraphUserInner().withDisplayName(name), manager());
    }

    @Override
    public Mono<Void> deleteByIdAsync(String id) {
        return inner().deleteUserAsync(id);
    }

    @Override
    public AuthorizationManager manager() {
        return this.manager;
    }

    public UsersUsersClient inner() {
        return manager().serviceClient().getUsersUsers();
    }

    @Override
    public PagedIterable<ActiveDirectoryUser> listByFilter(String filter) {
        return new PagedIterable<>(listByFilterAsync(filter));
    }

    @Override
    public PagedFlux<ActiveDirectoryUser> listByFilterAsync(String filter) {
        return PagedConverter.mapPage(inner().listUserAsync(null, null, null, null, filter, null, null, null, null),
            this::wrapModel);
    }
}
