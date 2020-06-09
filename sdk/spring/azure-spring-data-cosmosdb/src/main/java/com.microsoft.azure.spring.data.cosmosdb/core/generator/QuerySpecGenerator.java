// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.data.cosmosdb.core.generator;

import com.azure.data.cosmos.SqlQuerySpec;
import com.microsoft.azure.spring.data.cosmosdb.core.query.DocumentQuery;

public interface QuerySpecGenerator {

    /**
     * Generate the SqlQuerySpec for cosmosDB client.
     * @param query tree structured query condition.
     * @return SqlQuerySpec executed by cosmos client.
     */
    SqlQuerySpec generateCosmos(DocumentQuery query);
}
