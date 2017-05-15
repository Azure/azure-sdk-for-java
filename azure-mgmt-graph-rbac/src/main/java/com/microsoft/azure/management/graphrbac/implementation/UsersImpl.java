/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac.implementation;

import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.graphrbac.User;
import com.microsoft.azure.management.graphrbac.Users;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import rx.Observable;
import rx.functions.Func1;

/**
 * The implementation of Users and its parent interfaces.
 */
@LangDefinition(ContainerName = "/Microsoft.Azure.Management.Fluent.Graph.RBAC")
class UsersImpl
        extends ReadableWrappersImpl<
                    User,
                    UserImpl,
                    UserInner>
        implements
            Users,
            HasInner<UsersInner> {
    private final GraphRbacManager manager;

    UsersImpl(
            final GraphRbacManager manager) {
        this.manager = manager;
    }

    @Override
    public PagedList<User> list() {
        return wrapList(this.manager().inner().users().list());
    }

    @Override
    protected UserImpl wrapModel(UserInner userInner) {
        if (userInner == null) {
            return null;
        }
        return new UserImpl(userInner, manager());
    }

    @Override
    public UserImpl getById(String objectId) {
        return (UserImpl) getByIdAsync(objectId).toBlocking().single();
    }

    @Override
    public Observable<User> getByIdAsync(String id) {
        return manager().inner().users().getAsync(id).map(new Func1<UserInner, User>() {
            @Override
            public User call(UserInner userInner) {
                if (userInner == null) {
                    return null;
                } else {
                    return new UserImpl(userInner, manager());
                }
            }
        });
    }

    @Override
    public ServiceFuture<User> getByIdAsync(String id, ServiceCallback<User> callback) {
        return ServiceFuture.fromBody(getByIdAsync(id), callback);
    }

    @Override
    public UserImpl getByName(String upn) {
        return (UserImpl) getByNameAsync(upn).toBlocking().single();
    }

    @Override
    public Observable<User> getByNameAsync(final String name) {
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
                .map(new Func1<UserInner, User>() {
                    @Override
                    public User call(UserInner userInnerServiceResponse) {
                        if (userInnerServiceResponse == null) {
                            return null;
                        }
                        return new UserImpl(userInnerServiceResponse, manager());
                    }
                });
    }

    @Override
    public UsersInner inner() {
        return this.manager().inner().users();
    }

    @Override
    public Observable<User> listAsync() {
        return wrapPageAsync(this.inner().listAsync());
    }

    @Override
    public GraphRbacManager manager() {
        return manager;
    }
}
