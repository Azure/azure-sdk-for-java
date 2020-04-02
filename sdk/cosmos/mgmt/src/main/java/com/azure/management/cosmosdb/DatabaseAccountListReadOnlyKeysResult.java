/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.cosmosdb;


import com.azure.core.annotation.Fluent;
import com.azure.management.cosmosdb.models.DatabaseAccountListReadOnlyKeysResultInner;
import com.azure.management.resources.fluentcore.model.HasInner;

/**
 * An immutable client-side representation of an Azure Cosmos DB DatabaseAccountListReadOnlyKeysResult.
 */
@Fluent
public interface DatabaseAccountListReadOnlyKeysResult extends HasInner<DatabaseAccountListReadOnlyKeysResultInner> {
    /**
     * @return Base 64 encoded value of the primary read-only key.
     */
    String primaryReadonlyMasterKey();

    /**
     * @return Base 64 encoded value of the secondary read-only key.
     */
    String secondaryReadonlyMasterKey();
}
