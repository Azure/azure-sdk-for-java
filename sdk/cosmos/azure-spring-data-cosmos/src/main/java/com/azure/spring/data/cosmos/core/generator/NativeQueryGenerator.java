// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.core.generator;

import com.azure.cosmos.models.SqlQuerySpec;
import org.springframework.data.domain.Sort;
import org.springframework.util.Assert;

import java.util.Locale;

/**
 * Augment custom queries sourced from @Query annotations
 */
public class NativeQueryGenerator {

    private static final NativeQueryGenerator INSTANCE = new NativeQueryGenerator();

    public static NativeQueryGenerator getInstance() {
        return INSTANCE;
    }

    private SqlQuerySpec cloneWithQueryText(SqlQuerySpec querySpec, String queryText) {
        SqlQuerySpec clone = new SqlQuerySpec();
        clone.setQueryText(queryText);
        clone.setParameters(querySpec.getParameters());
        return clone;
    }

    public SqlQuerySpec generateSortedQuery(SqlQuerySpec querySpec, Sort sort) {
        if (sort == null || sort.isUnsorted()) {
            return querySpec;
        } else {
            String querySort = AbstractQueryGenerator.generateQuerySort(sort);
            String queryText = "select * from (" + querySpec.getQueryText() + ") r " + querySort;
            return cloneWithQueryText(querySpec, queryText);
        }
    }

    public SqlQuerySpec generateCountQuery(SqlQuerySpec querySpec) {
        String queryText = querySpec.getQueryText();
        int fromIndex = queryText.toLowerCase(Locale.US).indexOf(" from ");
        Assert.isTrue(fromIndex >= 0, "query missing from keyword, query=" + queryText);

        String countQueryText = "select value count(1) " + queryText.substring(fromIndex);
        return cloneWithQueryText(querySpec, countQueryText);
    }

}
