// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.core.generator;

import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.spring.data.cosmos.core.query.CosmosQuery;

/**
 * Interface of generating SqlQuerySpec
 */
public interface QuerySpecGenerator {

    /**
     * Generate the SqlQuerySpec for cosmosDB client.
     * @param query tree structured query condition.
     * @return SqlQuerySpec executed by cosmos client.
     */
    SqlQuerySpec generateCosmos(CosmosQuery query);
}
