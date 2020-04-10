// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.management.cosmosdb;

import com.azure.core.annotation.Fluent;
import com.azure.management.cosmosdb.implementation.CosmosDBManager;
import com.azure.management.cosmosdb.models.DatabaseAccountsInner;
import com.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByResourceGroup;
import com.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.management.resources.fluentcore.arm.collection.SupportsGettingByResourceGroup;
import com.azure.management.resources.fluentcore.arm.collection.SupportsListingByResourceGroup;
import com.azure.management.resources.fluentcore.arm.models.HasManager;
import com.azure.management.resources.fluentcore.collection.SupportsBatchCreation;
import com.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.azure.management.resources.fluentcore.collection.SupportsListing;
import com.azure.management.resources.fluentcore.model.HasInner;
import java.util.List;
import reactor.core.publisher.Mono;

/** Entry point to Cosmos DB management API. */
@Fluent
public interface CosmosDBAccounts
    extends SupportsCreating<CosmosDBAccount.DefinitionStages.Blank>,
        HasManager<CosmosDBManager>,
        HasInner<DatabaseAccountsInner>,
        SupportsBatchCreation<CosmosDBAccount>,
        SupportsGettingById<CosmosDBAccount>,
        SupportsDeletingById,
        SupportsDeletingByResourceGroup,
        SupportsListing<CosmosDBAccount>,
        SupportsListingByResourceGroup<CosmosDBAccount>,
        SupportsGettingByResourceGroup<CosmosDBAccount> {

    /**
     * Changes the failover priority for the Azure CosmosDB database account. A failover priority of 0 indicates a write
     * region. The maximum value for a failover priority = (total number of regions - 1). Failover priority values must
     * be unique for each of the regions in which the database account exists.
     *
     * @param groupName the group name
     * @param accountName the account name
     * @param failoverPolicies the list of failover policies
     */
    void failoverPriorityChange(String groupName, String accountName, List<Location> failoverPolicies);

    /**
     * Lists the access keys for the specified Azure CosmosDB database account.
     *
     * @param groupName the group name
     * @param accountName the account name
     * @return a list of keys
     */
    DatabaseAccountListKeysResult listKeys(String groupName, String accountName);

    /**
     * Lists the read-only access keys for the specified Azure CosmosDB database account.
     *
     * @param groupName the group name
     * @param accountName the account name
     * @return a list of keys
     */
    DatabaseAccountListReadOnlyKeysResult listReadOnlyKeys(String groupName, String accountName);

    /**
     * Lists the connection strings for the specified Azure CosmosDB database account.
     *
     * @param groupName the group name
     * @param accountName the account name
     * @return a list of connection strings
     */
    DatabaseAccountListConnectionStringsResult listConnectionStrings(String groupName, String accountName);

    /**
     * Regenerates an access key for the specified Azure CosmosDB database account.
     *
     * @param groupName the group name
     * @param accountName the account name
     * @param keyKind the key kind
     */
    void regenerateKey(String groupName, String accountName, KeyKind keyKind);

    /**
     * Changes the failover priority for the Azure CosmosDB database account. A failover priority of 0 indicates a write
     * region. The maximum value for a failover priority = (total number of regions - 1). Failover priority values must
     * be unique for each of the regions in which the database account exists.
     *
     * @param groupName the group name
     * @param accountName the account name
     * @param failoverPolicies the list of failover policies
     * @return the ServiceResponse object if successful.
     */
    Mono<Void> failoverPriorityChangeAsync(String groupName, String accountName, List<Location> failoverPolicies);

    /**
     * Lists the access keys for the specified Azure CosmosDB database account.
     *
     * @param groupName the group name
     * @param accountName the account name
     * @return a list of keys
     */
    Mono<DatabaseAccountListKeysResult> listKeysAsync(String groupName, String accountName);

    /**
     * Lists the read-only access keys for the specified Azure CosmosDB database account.
     *
     * @param groupName the group name
     * @param accountName the account name
     * @return a list of keys
     */
    Mono<DatabaseAccountListReadOnlyKeysResult> listReadOnlyKeysAsync(String groupName, String accountName);

    /**
     * Lists the connection strings for the specified Azure CosmosDB database account.
     *
     * @param groupName the group name
     * @param accountName the account name
     * @return a list of connection strings
     */
    Mono<DatabaseAccountListConnectionStringsResult> listConnectionStringsAsync(String groupName, String accountName);

    /**
     * Regenerates an access key for the specified Azure CosmosDB database account.
     *
     * @param groupName the group name
     * @param accountName the account name
     * @param keyKind the key kind
     * @return the ServiceResponse object if successful.
     */
    Mono<Void> regenerateKeyAsync(String groupName, String accountName, KeyKind keyKind);
}
