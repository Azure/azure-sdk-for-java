// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.data.cosmosdb.core.generator;

import com.azure.data.cosmos.SqlQuerySpec;
import com.microsoft.azure.spring.data.cosmosdb.core.query.DocumentQuery;

public class FindQuerySpecGenerator extends AbstractQueryGenerator implements QuerySpecGenerator {

    public FindQuerySpecGenerator() {
    }

    @Override
    public SqlQuerySpec generateCosmos(DocumentQuery query) {
        return super.generateCosmosQuery(query, "SELECT * FROM ROOT r");
    }
}
