/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.cosmosdb.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.management.cosmosdb.CosmosDBAccount;
import com.azure.management.cosmosdb.CosmosDBAccounts;
import com.azure.management.cosmosdb.DatabaseAccountListConnectionStringsResult;
import com.azure.management.cosmosdb.DatabaseAccountListKeysResult;
import com.azure.management.cosmosdb.DatabaseAccountListReadOnlyKeysResult;
import com.azure.management.cosmosdb.FailoverPolicy;
import com.azure.management.cosmosdb.KeyKind;
import com.azure.management.cosmosdb.Location;
import com.azure.management.cosmosdb.models.DatabaseAccountGetResultsInner;
import com.azure.management.cosmosdb.models.DatabaseAccountsInner;
import com.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation for Registries.
 */
class CosmosDBAccountsImpl
        extends
        GroupableResourcesImpl<
                        CosmosDBAccount,
                        CosmosDBAccountImpl,
                        DatabaseAccountGetResultsInner,
                        DatabaseAccountsInner,
                        CosmosDBManager>
        implements CosmosDBAccounts {

    CosmosDBAccountsImpl(final CosmosDBManager manager) {
        super(manager.inner().databaseAccounts(), manager);
    }

    @Override
    public PagedIterable<CosmosDBAccount> list() {
        return new PagedIterable<>(this.listAsync());
    }

    @Override
    public PagedFlux<CosmosDBAccount> listAsync() {
        return this.inner().listAsync()
                .mapPage(inner -> new CosmosDBAccountImpl(inner.getName(), inner, this.manager()));
    }

    @Override
    public PagedFlux<CosmosDBAccount> listByResourceGroupAsync(String resourceGroupName) {
        return wrapPageAsync(this.inner().listByResourceGroupAsync(resourceGroupName));
    }


    @Override
    public PagedIterable<CosmosDBAccount> listByResourceGroup(String groupName) {
        return wrapList(this.inner().listByResourceGroup(groupName));
    }

    @Override
    protected Mono<DatabaseAccountGetResultsInner> getInnerAsync(String resourceGroupName, String name) {
        return this.inner().getByResourceGroupAsync(resourceGroupName, name);
    }

    @Override
    public CosmosDBAccountImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    protected Mono<Void> deleteInnerAsync(String groupName, String name) {
        return this.inner().deleteAsync(groupName, name);
    }

    /**************************************************************
     * Fluent model helpers.
     **************************************************************/

    @Override
    protected CosmosDBAccountImpl wrapModel(String name) {
        return new CosmosDBAccountImpl(name,
                new DatabaseAccountGetResultsInner(),
                this.manager());
    }

    @Override
    protected CosmosDBAccountImpl wrapModel(DatabaseAccountGetResultsInner containerServiceInner) {
        if (containerServiceInner == null) {
            return null;
        }

        return new CosmosDBAccountImpl(containerServiceInner.getName(),
                containerServiceInner,
                this.manager());
    }

    @Override
    public void failoverPriorityChange(String groupName, String accountName, List<Location> failoverLocations) {
        this.failoverPriorityChangeAsync(groupName, accountName, failoverLocations).block();
    }

    @Override
    public Mono<Void> failoverPriorityChangeAsync(String groupName, String accountName, List<Location> failoverLocations) {
        List<FailoverPolicy> policyInners = new ArrayList<FailoverPolicy>();
        for (int i = 0; i < failoverLocations.size(); i++) {
            Location location  = failoverLocations.get(i);
            FailoverPolicy policyInner = new FailoverPolicy();
            policyInner.withLocationName(location.locationName());
            policyInner.withFailoverPriority(i);
            policyInners.add(policyInner);
        }

        return this.manager().inner().databaseAccounts().failoverPriorityChangeAsync(groupName, accountName, policyInners);
    }

    @Override
    public DatabaseAccountListKeysResult listKeys(String groupName, String accountName) {
        return this.listKeysAsync(groupName, accountName).block();
    }

    @Override
    public DatabaseAccountListReadOnlyKeysResult listReadOnlyKeys(String groupName, String accountName) {
        return this.listReadOnlyKeysAsync(groupName, accountName).block();
    }

    @Override
    public Mono<DatabaseAccountListKeysResult> listKeysAsync(String groupName, String accountName) {
        return this.manager().inner().databaseAccounts()
            .listKeysAsync(groupName, accountName)
            .map(databaseAccountListKeysResultInner -> new DatabaseAccountListKeysResultImpl(databaseAccountListKeysResultInner));
    }

    @Override
    public Mono<DatabaseAccountListReadOnlyKeysResult> listReadOnlyKeysAsync(String groupName, String accountName) {
        return this.manager().inner().databaseAccounts()
            .listReadOnlyKeysAsync(groupName, accountName)
            .map(databaseAccountListReadOnlyKeysResultInner -> new DatabaseAccountListReadOnlyKeysResultImpl(databaseAccountListReadOnlyKeysResultInner));
    }

    @Override
    public DatabaseAccountListConnectionStringsResult listConnectionStrings(String groupName, String accountName) {
        return this.listConnectionStringsAsync(groupName, accountName).block();
    }

    @Override
    public Mono<DatabaseAccountListConnectionStringsResult> listConnectionStringsAsync(String groupName, String accountName) {
        return this.manager().inner().databaseAccounts()
            .listConnectionStringsAsync(groupName, accountName)
            .map(databaseAccountListConnectionStringsResultInner -> new DatabaseAccountListConnectionStringsResultImpl(databaseAccountListConnectionStringsResultInner));
    }

    @Override
    public void regenerateKey(String groupName, String accountName, KeyKind keyKind) {
        this.regenerateKeyAsync(groupName, accountName, keyKind).block();
    }

    @Override
    public Mono<Void> regenerateKeyAsync(String groupName, String accountName, KeyKind keyKind) {
        return this.manager().inner().databaseAccounts().regenerateKeyAsync(groupName, accountName, keyKind);
    }
}
