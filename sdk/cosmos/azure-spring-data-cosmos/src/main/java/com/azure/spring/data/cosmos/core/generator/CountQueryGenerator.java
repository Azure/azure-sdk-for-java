// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.core.generator;

import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.spring.data.cosmos.core.query.CosmosQuery;

/**
 * Generate count query
 */
public class CountQueryGenerator extends AbstractQueryGenerator implements QuerySpecGenerator {

    @Override
    public SqlQuerySpec generateCosmos(CosmosQuery query) {
        return super.generateCosmosQuery(query, "SELECT VALUE COUNT(1) FROM r");
    }
}
