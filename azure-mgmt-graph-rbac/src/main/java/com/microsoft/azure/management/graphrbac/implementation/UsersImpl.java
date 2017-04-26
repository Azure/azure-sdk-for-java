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
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.microsoft.rest.ServiceFuture;
import com.microsoft.rest.ServiceCallback;
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
            HasManager<GraphRbacManager>,
            HasInner<UsersInner> {
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
    protected UserImpl wrapModel(UserInner userInner) {
        if (userInner == null) {
            return null;
        }
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
    public ServiceFuture<User> getByUserPrincipalNameAsync(String upn, final ServiceCallback<User> callback) {
        return ServiceFuture.fromBody(getByUserPrincipalNameAsync(upn), callback);
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

    @Override
    public GraphRbacManager manager() {
        return this.manager;
    }

    @Override
    public UsersInner inner() {
        return this.innerCollection;
    }

    @Override
    public Observable<User> listAsync() {
        return wrapPageAsync(this.inner().listAsync());
    }
}
