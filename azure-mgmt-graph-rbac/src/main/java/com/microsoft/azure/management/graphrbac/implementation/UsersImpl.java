/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.graphrbac.GraphErrorException;
import com.microsoft.azure.management.graphrbac.User;
import com.microsoft.azure.management.graphrbac.Users;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.CreatableWrappersImpl;
import com.microsoft.rest.RestException;

import java.io.IOException;

/**
 * The implementation of StorageAccounts and its parent interfaces.
 */
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
    public PagedList<User> list() throws RestException, IOException {
        return wrapList(this.innerCollection.list().getBody());
    }

    @Override
    public void delete(String id) throws Exception {
        innerCollection.delete(id);
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
    public UserImpl getByObjectId(String objectId) throws GraphErrorException, IOException {
        return new UserImpl(innerCollection.get(objectId).getBody(), innerCollection);
    }

    @Override
    public UserImpl getByUserPrincipalName(String upn) throws GraphErrorException, IOException {
        return new UserImpl(innerCollection.get(upn).getBody(), innerCollection);
    }
}
