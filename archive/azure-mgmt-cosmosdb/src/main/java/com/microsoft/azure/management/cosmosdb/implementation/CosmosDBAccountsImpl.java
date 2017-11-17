/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.cosmosdb.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.cosmosdb.CosmosDBAccount;
import com.microsoft.azure.management.cosmosdb.CosmosDBAccounts;
import com.microsoft.azure.management.cosmosdb.Location;
import com.microsoft.azure.management.cosmosdb.KeyKind;
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
@LangDefinition
class CosmosDBAccountsImpl
        extends
        GroupableResourcesImpl<
                CosmosDBAccount,
                CosmosDBAccountImpl,
                DatabaseAccountInner,
                DatabaseAccountsInner,
                CosmosDBManager>
        implements CosmosDBAccounts {

    CosmosDBAccountsImpl(final CosmosDBManager manager) {
        super(manager.inner().databaseAccounts(), manager);
    }

    @Override
    public PagedList<CosmosDBAccount> list() {
        final CosmosDBAccountsImpl self = this;
        return new GroupPagedList<CosmosDBAccount>(this.manager().resourceManager().resourceGroups().list()) {
            @Override
            public List<CosmosDBAccount> listNextGroup(String resourceGroupName) {
                return wrapList(self.inner().listByResourceGroup(resourceGroupName));

            }
        };
    }

    @Override
    public Observable<CosmosDBAccount> listAsync() {
        return this.manager().resourceManager().resourceGroups().listAsync()
                .flatMap(new Func1<ResourceGroup, Observable<CosmosDBAccount>>() {
                    @Override
                    public Observable<CosmosDBAccount> call(ResourceGroup resourceGroup) {
                        return wrapPageAsync(inner().listByResourceGroupAsync(resourceGroup.name()));
                    }
                });
    }

    @Override
    public Observable<CosmosDBAccount> listByResourceGroupAsync(String resourceGroupName) {
        return wrapPageAsync(this.inner().listByResourceGroupAsync(resourceGroupName));
    }


    @Override
    public PagedList<CosmosDBAccount> listByResourceGroup(String groupName) {
        return wrapList(this.inner().listByResourceGroup(groupName));
    }

    @Override
    protected Observable<DatabaseAccountInner> getInnerAsync(String resourceGroupName, String name) {
        return this.inner().getByResourceGroupAsync(resourceGroupName, name);
    }

    @Override
    public CosmosDBAccountImpl define(String name) {
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
    protected CosmosDBAccountImpl wrapModel(String name) {
        return new CosmosDBAccountImpl(name,
                new DatabaseAccountInner(),
                this.manager());
    }

    @Override
    protected CosmosDBAccountImpl wrapModel(DatabaseAccountInner containerServiceInner) {
        if (containerServiceInner == null) {
            return null;
        }

        return new CosmosDBAccountImpl(containerServiceInner.name(),
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
    public DatabaseAccountListKeysResult listKeys(String groupName, String accountName) {
        return this.listKeysAsync(groupName, accountName).toBlocking().last();
    }

    @Override
    public Observable<DatabaseAccountListKeysResult> listKeysAsync(String groupName, String accountName) {
        return this.manager().inner().databaseAccounts().listKeysAsync(groupName, accountName);
    }

    @Override
    public DatabaseAccountListConnectionStringsResult listConnectionStrings(String groupName, String accountName) {
        return this.listConnectionStringsAsync(groupName, accountName).toBlocking().last();
    }

    @Override
    public Observable<DatabaseAccountListConnectionStringsResult> listConnectionStringsAsync(String groupName, String accountName) {
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
