/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.storage.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import com.microsoft.azure.management.storage.CheckNameAvailabilityResult;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.management.storage.StorageAccounts;
import com.microsoft.azure.management.storage.SkuName;

import java.io.IOException;

/**
 * The implementation of StorageAccounts and its parent interfaces.
 */
class StorageAccountsImpl
        extends GroupableResourcesImpl<
            StorageAccount,
            StorageAccountImpl,
            StorageAccountInner,
            StorageAccountsInner,
            StorageManager>
        implements StorageAccounts {

    StorageAccountsImpl(
            final StorageAccountsInner client,
            final StorageManager storageManager) {
        super(client, storageManager);
    }

    @Override
    public CheckNameAvailabilityResult checkNameAvailability(String name) throws CloudException, IOException {
        return new CheckNameAvailabilityResult(this.innerCollection.checkNameAvailability(name));
    }

    @Override
    public PagedList<StorageAccount> list() throws CloudException, IOException {
        return wrapList(this.innerCollection.list());
    }

    @Override
    public PagedList<StorageAccount> listByGroup(String groupName) throws CloudException, IOException {
        return wrapList(this.innerCollection.listByResourceGroup(groupName));
    }

    @Override
    public StorageAccount getByGroup(String groupName, String name) throws CloudException, IOException {
        return wrapModel(this.innerCollection.getProperties(groupName, name));
    }

    @Override
    public void delete(String id) throws Exception {
        delete(ResourceUtils.groupFromResourceId(id), ResourceUtils.nameFromResourceId(id));
    }

    @Override
    public void delete(String groupName, String name) throws Exception {
        this.innerCollection.delete(groupName, name);
    }

    @Override
    public StorageAccountImpl define(String name) {
        return wrapModel(name)
                .withSku(SkuName.STANDARD_GRS)
                .withGeneralPurposeAccountKind();
    }

    @Override
    protected StorageAccountImpl wrapModel(String name) {
        return new StorageAccountImpl(
                name,
                new StorageAccountInner(),
                this.innerCollection,
                super.myManager);
    }

    @Override
    protected StorageAccountImpl wrapModel(StorageAccountInner storageAccountInner) {
        return new StorageAccountImpl(
                storageAccountInner.name(),
                storageAccountInner,
                this.innerCollection,
                super.myManager);
    }
}
