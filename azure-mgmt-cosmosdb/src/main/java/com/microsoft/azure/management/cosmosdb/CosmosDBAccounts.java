/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.cosmosdb;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.cosmosdb.implementation.DatabaseAccountListConnectionStringsResult;
import com.microsoft.azure.management.cosmosdb.implementation.DatabaseAccountListKeysResult;
import com.microsoft.azure.management.cosmosdb.implementation.DatabaseAccountsInner;
import com.microsoft.azure.management.cosmosdb.implementation.CosmosDBManager;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsListingByResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsBatchCreation;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import rx.Observable;

import java.util.List;


/**
 *  Entry point to cosmos db management API.
 */
@Fluent()
@Beta(Beta.SinceVersion.V1_1_0)
public interface CosmosDBAccounts extends
        SupportsCreating<CosmosDBAccount.DefinitionStages.Blank>,
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
     * Changes the failover priority for the Azure CosmosDB database account. A failover priority of 0 indicates
     * a write region. The maximum value for a failover priority = (total number of regions - 1).
     * Failover priority values must be unique for each of the regions in which the database account exists.
     * @param groupName the group name
     * @param accountName the account name
     * @param failoverPolicies the list of failover policies
     */
    void failoverPriorityChange(String groupName, String accountName, List<Location> failoverPolicies);

    /**
     * Lists the access keys for the specified Azure CosmosDB database account.
     * @param groupName the group name
     * @param accountName the account name
     * @return a list of keys
     */
    DatabaseAccountListKeysResult listKeys(String groupName, String accountName);

    /**
     * Lists the connection strings for the specified Azure CosmosDB database account.
     * @param groupName the group name
     * @param accountName the account name
     * @return a list of connection strings
     */
    DatabaseAccountListConnectionStringsResult listConnectionStrings(String groupName, String accountName);

    /**
     * Regenerates an access key for the specified Azure CosmosDB database account.
     * @param groupName the group name
     * @param accountName the account name
     * @param keyKind the key kind
     */
    void regenerateKey(String groupName, String accountName, KeyKind keyKind);

    /**
     * Changes the failover priority for the Azure CosmosDB database account. A failover priority of 0 indicates
     * a write region. The maximum value for a failover priority = (total number of regions - 1).
     * Failover priority values must be unique for each of the regions in which the database account exists.
     * @param groupName the group name
     * @param accountName the account name
     * @param failoverPolicies the list of failover policies
     * @return the ServiceResponse object if successful.
     */
    Observable<Void> failoverPriorityChangeAsync(String groupName, String accountName, List<Location> failoverPolicies);

    /**
     * Lists the access keys for the specified Azure CosmosDB database account.
     * @param groupName the group name
     * @param accountName the account name
     * @return a list of keys
     */
    Observable<DatabaseAccountListKeysResult> listKeysAsync(String groupName, String accountName);

    /**
     * Lists the connection strings for the specified Azure CosmosDB database account.
     * @param groupName the group name
     * @param accountName the account name
     * @return a list of connection strings
     */
    Observable<DatabaseAccountListConnectionStringsResult> listConnectionStringsAsync(String groupName, String accountName);

    /**
     * Regenerates an access key for the specified Azure CosmosDB database account.
     * @param groupName the group name
     * @param accountName the account name
     * @param keyKind the key kind
     * @return the ServiceResponse object if successful.
     */
    Observable<Void> regenerateKeyAsync(String groupName, String accountName, KeyKind keyKind);

}
