/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.graphrbac.User;
import com.microsoft.azure.management.graphrbac.Users;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.CreatableWrappersImpl;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;
import com.microsoft.azure.management.storage.CheckNameAvailabilityResult;
import com.microsoft.azure.management.storage.SkuName;
import com.microsoft.azure.management.storage.StorageAccount;

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
    }

    @Override
    public PagedList<User> list() throws CloudException, IOException {
        return wrapList(this.innerCollection.list().getBody());
    }

    @Override
    public void delete(String id) throws Exception {
        innerCollection.delete(id);
    }

    @Override
    public UserImpl define(String name) {
        return wrapModel(name)
                .withSku(SkuName.STANDARD_GRS)
                .withGeneralPurposeAccountKind();
    }

    @Override
    protected UserImpl wrapModel(String name) {
        return new UserImpl(
                name,
                new UserInner(),
                innerCollection,
                manager);
    }

    @Override
    protected StorageAccountImpl wrapModel(UserInner userInner) {
        return new StorageAccountImpl(
                userInner.name(),
                userInner,
                this.innerCollection,
                manager);
    }

    @Override
    public User getByName(String name) throws CloudException, IOException {
        return null;
    }

    @Override
    protected UserImpl wrapModel(UserInner inner) {
        return null;
    }
}
