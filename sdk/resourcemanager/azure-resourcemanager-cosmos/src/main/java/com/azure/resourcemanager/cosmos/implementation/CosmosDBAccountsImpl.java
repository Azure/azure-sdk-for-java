// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.cosmos.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.CoreUtils;
import com.azure.resourcemanager.cosmos.CosmosManager;
import com.azure.resourcemanager.cosmos.fluent.DatabaseAccountsClient;
import com.azure.resourcemanager.cosmos.fluent.models.DatabaseAccountGetResultsInner;
import com.azure.resourcemanager.cosmos.models.CosmosDBAccount;
import com.azure.resourcemanager.cosmos.models.CosmosDBAccounts;
import com.azure.resourcemanager.cosmos.models.DatabaseAccountListConnectionStringsResult;
import com.azure.resourcemanager.cosmos.models.DatabaseAccountListKeysResult;
import com.azure.resourcemanager.cosmos.models.DatabaseAccountListReadOnlyKeysResult;
import com.azure.resourcemanager.cosmos.models.DatabaseAccountRegenerateKeyParameters;
import com.azure.resourcemanager.cosmos.models.FailoverPolicies;
import com.azure.resourcemanager.cosmos.models.FailoverPolicy;
import com.azure.resourcemanager.cosmos.models.KeyKind;
import com.azure.resourcemanager.cosmos.models.Location;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;

/** Implementation for Registries. */
public class CosmosDBAccountsImpl
    extends GroupableResourcesImpl<
        CosmosDBAccount, CosmosDBAccountImpl, DatabaseAccountGetResultsInner, DatabaseAccountsClient, CosmosManager>
    implements CosmosDBAccounts {

    public CosmosDBAccountsImpl(final CosmosManager manager) {
        super(manager.serviceClient().getDatabaseAccounts(), manager);
    }

    @Override
    public PagedIterable<CosmosDBAccount> list() {
        return new PagedIterable<>(this.listAsync());
    }

    @Override
    public PagedFlux<CosmosDBAccount> listAsync() {
        return PagedConverter.mapPage(this
            .inner()
            .listAsync(),
            inner -> new CosmosDBAccountImpl(inner.name(), inner, this.manager()));
    }

    @Override
    public PagedFlux<CosmosDBAccount> listByResourceGroupAsync(String resourceGroupName) {
        if (CoreUtils.isNullOrEmpty(resourceGroupName)) {
            return new PagedFlux<>(() -> Mono.error(
                new IllegalArgumentException("Parameter 'resourceGroupName' is required and cannot be null.")));
        }
        return wrapPageAsync(this.inner().listByResourceGroupAsync(resourceGroupName));
    }

    @Override
    public PagedIterable<CosmosDBAccount> listByResourceGroup(String groupName) {
        return new PagedIterable<>(this.listByResourceGroupAsync(groupName));
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
        return new CosmosDBAccountImpl(name, new DatabaseAccountGetResultsInner(), this.manager());
    }

    @Override
    protected CosmosDBAccountImpl wrapModel(DatabaseAccountGetResultsInner containerServiceInner) {
        if (containerServiceInner == null) {
            return null;
        }

        return new CosmosDBAccountImpl(containerServiceInner.name(), containerServiceInner, this.manager());
    }

    @Override
    public void failoverPriorityChange(String groupName, String accountName, List<Location> failoverLocations) {
        this.failoverPriorityChangeAsync(groupName, accountName, failoverLocations).block();
    }

    @Override
    public Mono<Void> failoverPriorityChangeAsync(
        String groupName, String accountName, List<Location> failoverLocations) {
        List<FailoverPolicy> policyInners = new ArrayList<FailoverPolicy>();
        for (int i = 0; i < failoverLocations.size(); i++) {
            Location location = failoverLocations.get(i);
            FailoverPolicy policyInner = new FailoverPolicy();
            policyInner.withLocationName(location.locationName());
            policyInner.withFailoverPriority(i);
            policyInners.add(policyInner);
        }

        return this
            .manager()
            .serviceClient()
            .getDatabaseAccounts()
            .failoverPriorityChangeAsync(groupName, accountName,
                new FailoverPolicies().withFailoverPolicies(policyInners));
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
        return this
            .manager()
            .serviceClient()
            .getDatabaseAccounts()
            .listKeysAsync(groupName, accountName)
            .map(
                databaseAccountListKeysResultInner ->
                    new DatabaseAccountListKeysResultImpl(databaseAccountListKeysResultInner));
    }

    @Override
    public Mono<DatabaseAccountListReadOnlyKeysResult> listReadOnlyKeysAsync(String groupName, String accountName) {
        return this
            .manager()
            .serviceClient()
            .getDatabaseAccounts()
            .listReadOnlyKeysAsync(groupName, accountName)
            .map(
                databaseAccountListReadOnlyKeysResultInner ->
                    new DatabaseAccountListReadOnlyKeysResultImpl(databaseAccountListReadOnlyKeysResultInner));
    }

    @Override
    public DatabaseAccountListConnectionStringsResult listConnectionStrings(String groupName, String accountName) {
        return this.listConnectionStringsAsync(groupName, accountName).block();
    }

    @Override
    public Mono<DatabaseAccountListConnectionStringsResult> listConnectionStringsAsync(
        String groupName, String accountName) {
        return this
            .manager()
            .serviceClient()
            .getDatabaseAccounts()
            .listConnectionStringsAsync(groupName, accountName)
            .map(
                databaseAccountListConnectionStringsResultInner ->
                    new DatabaseAccountListConnectionStringsResultImpl(
                        databaseAccountListConnectionStringsResultInner));
    }

    @Override
    public void regenerateKey(String groupName, String accountName, KeyKind keyKind) {
        this.regenerateKeyAsync(groupName, accountName, keyKind).block();
    }

    @Override
    public Mono<Void> regenerateKeyAsync(String groupName, String accountName, KeyKind keyKind) {
        return this.manager().serviceClient().getDatabaseAccounts().regenerateKeyAsync(groupName, accountName,
            new DatabaseAccountRegenerateKeyParameters().withKeyKind(keyKind));
    }
}
