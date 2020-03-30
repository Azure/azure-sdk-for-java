/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.cosmosdb;


import com.azure.core.annotation.Fluent;
import com.azure.management.cosmosdb.models.DatabaseAccountListConnectionStringsResultInner;
import com.azure.management.resources.fluentcore.model.HasInner;

import java.util.List;

/**
 * An immutable client-side representation of an Azure Cosmos DB DatabaseAccountListConnectionStringsResult.
 */
@Fluent
public interface DatabaseAccountListConnectionStringsResult extends HasInner<DatabaseAccountListConnectionStringsResultInner> {
    /**
     * @return a list that contains the connection strings for the CosmosDB account.
     */
    List<DatabaseAccountConnectionString> connectionStrings();
}
