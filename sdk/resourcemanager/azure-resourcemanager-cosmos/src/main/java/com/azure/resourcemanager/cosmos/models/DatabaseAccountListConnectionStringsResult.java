// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.cosmos.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.cosmos.fluent.inner.DatabaseAccountListConnectionStringsResultInner;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import java.util.List;

/** An immutable client-side representation of an Azure Cosmos DB DatabaseAccountListConnectionStringsResult. */
@Fluent
public interface DatabaseAccountListConnectionStringsResult
    extends HasInner<DatabaseAccountListConnectionStringsResultInner> {
    /** @return a list that contains the connection strings for the CosmosDB account. */
    List<DatabaseAccountConnectionString> connectionStrings();
}
