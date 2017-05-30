/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac.implementation;

import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.graphrbac.ActiveDirectoryUser;
import com.microsoft.azure.management.graphrbac.ActiveDirectoryUsers;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import rx.Observable;
import rx.functions.Func1;

/**
 * The implementation of Users and its parent interfaces.
 */
@LangDefinition(ContainerName = "/Microsoft.Azure.Management.Graph.RBAC.Fluent")
class ActiveDirectoryUsersImpl
        extends ReadableWrappersImpl<
        ActiveDirectoryUser,
        ActiveDirectoryUserImpl,
                    UserInner>
        implements
        ActiveDirectoryUsers,
            HasInner<UsersInner> {
    private final GraphRbacManager manager;

    ActiveDirectoryUsersImpl(
            final GraphRbacManager manager) {
        this.manager = manager;
    }

    @Override
    public PagedList<ActiveDirectoryUser> list() {
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
        return (ActiveDirectoryUserImpl) getByIdAsync(objectId).toBlocking().single();
    }

    @Override
    public Observable<ActiveDirectoryUser> getByIdAsync(String id) {
        return manager().inner().users().getAsync(id).map(new Func1<UserInner, ActiveDirectoryUser>() {
            @Override
            public ActiveDirectoryUser call(UserInner userInner) {
                if (userInner == null) {
                    return null;
                } else {
                    return new ActiveDirectoryUserImpl(userInner, manager());
                }
            }
        });
    }

    @Override
    public ServiceFuture<ActiveDirectoryUser> getByIdAsync(String id, ServiceCallback<ActiveDirectoryUser> callback) {
        return ServiceFuture.fromBody(getByIdAsync(id), callback);
    }

    @Override
    public ActiveDirectoryUserImpl getByName(String upn) {
        return (ActiveDirectoryUserImpl) getByNameAsync(upn).toBlocking().single();
    }

    @Override
    public Observable<ActiveDirectoryUser> getByNameAsync(final String name) {
        return manager().inner().users().getAsync(name)
                .flatMap(new Func1<UserInner, Observable<UserInner>>() {
                    @Override
                    public Observable<UserInner> call(UserInner userInner) {
                        // Exact match
                        if (userInner != null) {
                            return Observable.just(userInner);
                        }
                        // Search mail & mail nickname
                        if (name.contains("@")) {
                            return manager().inner().users().listAsync(String.format("mail eq '%s' or mailNickName eq '%s#EXT#'", name, name.replace("@", "_")))
                                    .map(new Func1<Page<UserInner>, UserInner>() {
                                        @Override
                                        public UserInner call(Page<UserInner> userInnerPage) {
                                            if (userInnerPage.items() == null || userInnerPage.items().isEmpty()) {
                                                return null;
                                            }
                                            return userInnerPage.items().get(0);
                                        }
                                    });
                        }
                        // Search display name
                        else {
                            return manager().inner().users().listAsync(String.format("displayName eq '%s'", name))
                                    .map(new Func1<Page<UserInner>, UserInner>() {
                                        @Override
                                        public UserInner call(Page<UserInner> userInnerPage) {
                                            if (userInnerPage.items() == null || userInnerPage.items().isEmpty()) {
                                                return null;
                                            }
                                            return userInnerPage.items().get(0);
                                        }
                                    });
                        }
                    }
                })
                .map(new Func1<UserInner, ActiveDirectoryUser>() {
                    @Override
                    public ActiveDirectoryUser call(UserInner userInnerServiceResponse) {
                        if (userInnerServiceResponse == null) {
                            return null;
                        }
                        return new ActiveDirectoryUserImpl(userInnerServiceResponse, manager());
                    }
                });
    }

    @Override
    public UsersInner inner() {
        return this.manager().inner().users();
    }

    @Override
    public Observable<ActiveDirectoryUser> listAsync() {
        return wrapPageAsync(this.inner().listAsync());
    }

    @Override
    public GraphRbacManager manager() {
        return manager;
    }
}
