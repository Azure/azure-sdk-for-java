/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.graphrbac.User;
import com.microsoft.azure.management.graphrbac.Users;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.CreatableWrappersImpl;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponse;
import rx.Observable;
import rx.functions.Func1;

/**
 * The implementation of Users and its parent interfaces.
 */
@LangDefinition(ContainerName = "/Microsoft.Azure.Management.Fluent.Graph.RBAC")
class UsersImpl
        extends CreatableWrappersImpl<
            User,
            UserImpl,
            UserInner>
        implements Users {
    private UsersInner innerCollection;
    private GraphRbacManager manager;

    UsersImpl(
            final UsersInner client,
            final GraphRbacManager graphRbacManager) {
        this.innerCollection = client;
        this.manager = graphRbacManager;
    }

    @Override
    public PagedList<User> list() {
        return wrapList(this.innerCollection.list());
    }

    @Override
    public UserImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    protected UserImpl wrapModel(String userPrincipalName) {
        return new UserImpl(userPrincipalName, innerCollection);
    }

    @Override
    protected UserImpl wrapModel(UserInner userInner) {
        return new UserImpl(userInner, this.innerCollection);
    }

    @Override
    public UserImpl getByObjectId(String objectId) {
        return new UserImpl(innerCollection.get(objectId), innerCollection);
    }

    @Override
    public UserImpl getByUserPrincipalName(String upn) {
        return new UserImpl(innerCollection.get(upn), innerCollection);
    }

    @Override
    public ServiceCall<User> getByUserPrincipalNameAsync(String upn, final ServiceCallback<User> callback) {
        return ServiceCall.create(
                getByUserPrincipalNameAsync(upn).map(new Func1<User, ServiceResponse<User>>() {
                    @Override
                    public ServiceResponse<User> call(User fluentModelT) {
                        return new ServiceResponse<>(fluentModelT, null);
                    }
                }), callback
        );
    }

    @Override
    public Observable<User> getByUserPrincipalNameAsync(String upn) {
        return innerCollection.getAsync(upn)
                .map(new Func1<UserInner, User>() {
                    @Override
                    public User call(UserInner userInnerServiceResponse) {
                        return new UserImpl(userInnerServiceResponse, innerCollection);
                    }
                });
    }
}
