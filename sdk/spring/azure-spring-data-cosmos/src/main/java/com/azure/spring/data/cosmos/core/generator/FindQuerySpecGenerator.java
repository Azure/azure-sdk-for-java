// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.core.generator;

import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.spring.data.cosmos.core.query.CosmosQuery;

/**
 * Generate sql find query
 */
public class FindQuerySpecGenerator extends AbstractQueryGenerator implements QuerySpecGenerator {
    /**
     * Initialization
     */
    public FindQuerySpecGenerator() {
    }

    @Override
    public SqlQuerySpec generateCosmos(CosmosQuery query) {
        return super.generateCosmosQuery(query, "SELECT * FROM ROOT r");
    }
}
