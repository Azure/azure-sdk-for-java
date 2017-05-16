/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.documentdb.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.documentdb.DatabaseAccount;
import com.microsoft.azure.management.documentdb.DatabaseAccounts;
import com.microsoft.azure.management.documentdb.Location;
import com.microsoft.azure.management.documentdb.KeyKind;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupPagedList;
import rx.Completable;
import rx.Observable;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation for Registries.
 */
class DatabaseAccountsImpl
        extends
        GroupableResourcesImpl<
                DatabaseAccount,
                DatabaseAccountImpl,
                DatabaseAccountInner,
                DatabaseAccountsInner,
                DocumentDBManager>
        implements DatabaseAccounts {

    DatabaseAccountsImpl(final DocumentDBManager manager) {
        super(manager.inner().databaseAccounts(), manager);
    }

    @Override
    public PagedList<DatabaseAccount> list() {
        final DatabaseAccountsImpl self = this;
        return new GroupPagedList<DatabaseAccount>(this.manager().resourceManager().resourceGroups().list()) {
            @Override
            public List<DatabaseAccount> listNextGroup(String resourceGroupName) {
                return wrapList(self.inner().listByResourceGroup(resourceGroupName));

            }
        };
    }

    @Override
    public Observable<DatabaseAccount> listAsync() {
        return this.manager().resourceManager().resourceGroups().listAsync()
                .flatMap(new Func1<ResourceGroup, Observable<DatabaseAccount>>() {
                    @Override
                    public Observable<DatabaseAccount> call(ResourceGroup resourceGroup) {
                        return wrapPageAsync(inner().listByResourceGroupAsync(resourceGroup.name()));
                    }
                });
    }

    @Override
    public Observable<DatabaseAccount> listByResourceGroupAsync(String resourceGroupName) {
        return wrapPageAsync(this.inner().listByResourceGroupAsync(resourceGroupName));
    }


    @Override
    public PagedList<DatabaseAccount> listByResourceGroup(String groupName) {
        return wrapList(this.inner().listByResourceGroup(groupName));
    }

    @Override
    protected Observable<DatabaseAccountInner> getInnerAsync(String resourceGroupName, String name) {
        return this.inner().getByResourceGroupAsync(resourceGroupName, name);
    }

    @Override
    public DatabaseAccountImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    protected Completable deleteInnerAsync(String groupName, String name) {
        return this.inner().deleteAsync(groupName, name).toCompletable();
    }

    /**************************************************************
     * Fluent model helpers.
     **************************************************************/

    @Override
    protected DatabaseAccountImpl wrapModel(String name) {
        return new DatabaseAccountImpl(name,
                new DatabaseAccountInner(),
                this.manager());
    }

    @Override
    protected DatabaseAccountImpl wrapModel(DatabaseAccountInner containerServiceInner) {
        if (containerServiceInner == null) {
            return null;
        }

        return new DatabaseAccountImpl(containerServiceInner.name(),
                containerServiceInner,
                this.manager());
    }

    @Override
    public void failoverPriorityChange(String groupName, String accountName, List<Location> failoverLocations) {
        this.failoverPriorityChangeAsync(groupName, accountName, failoverLocations).toBlocking().last();
    }

    @Override
    public Observable<Void> failoverPriorityChangeAsync(String groupName, String accountName, List<Location> failoverLocations) {
        List<FailoverPolicyInner> policyInners = new ArrayList<FailoverPolicyInner>();
        for (int i = 0; i < failoverLocations.size(); i++) {
            Location location  = failoverLocations.get(i);
            FailoverPolicyInner policyInner = new FailoverPolicyInner();
            policyInner.withLocationName(location.locationName());
            policyInner.withFailoverPriority(i);
            policyInners.add(policyInner);
        }

        return this.manager().inner().databaseAccounts().failoverPriorityChangeAsync(groupName, accountName, policyInners);
    }

    @Override
    public DatabaseAccountListKeysResultInner listKeys(String groupName, String accountName) {
        return this.listKeysAsync(groupName, accountName).toBlocking().last();
    }

    @Override
    public Observable<DatabaseAccountListKeysResultInner> listKeysAsync(String groupName, String accountName) {
        return this.manager().inner().databaseAccounts().listKeysAsync(groupName, accountName);
    }

    @Override
    public DatabaseAccountListConnectionStringsResultInner listConnectionStrings(String groupName, String accountName) {
        return this.listConnectionStringsAsync(groupName, accountName).toBlocking().last();
    }

    @Override
    public Observable<DatabaseAccountListConnectionStringsResultInner> listConnectionStringsAsync(String groupName, String accountName) {
        return this.manager().inner().databaseAccounts().listConnectionStringsAsync(groupName, accountName);
    }

    @Override
    public void regenerateKey(String groupName, String accountName, KeyKind keyKind) {
        this.regenerateKeyAsync(groupName, accountName, keyKind).toBlocking().last();
    }

    @Override
    public Observable<Void> regenerateKeyAsync(String groupName, String accountName, KeyKind keyKind) {
        return this.manager().inner().databaseAccounts().regenerateKeyAsync(groupName, accountName, keyKind);
    }
}
