// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.core.generator;

import com.azure.cosmos.models.SqlQuerySpec;
import org.springframework.data.domain.Sort;
import org.springframework.util.Assert;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Augment custom queries sourced from @Query annotations
 */
public class NativeQueryGenerator {

    /**
     * Creates an instance of {@link NativeQueryGenerator}.
     */
    public NativeQueryGenerator() {
    }

    private static final NativeQueryGenerator INSTANCE = new NativeQueryGenerator();

    /**
     * Return the native query generator instance.
     * @return The native query generator instance
     */
    public static NativeQueryGenerator getInstance() {
        return INSTANCE;
    }

    private SqlQuerySpec cloneWithQueryText(SqlQuerySpec querySpec, String queryText) {
        SqlQuerySpec clone = new SqlQuerySpec();
        clone.setQueryText(queryText);
        clone.setParameters(querySpec.getParameters());
        return clone;
    }

    /**
     * Generate sorted query.
     *
     * @param querySpec SQL query spec
     * @param sort Sort
     * @return sorted query
     *
     * WARNING: This function is only to be used with @Query annotations
     */
    public SqlQuerySpec generateSortedQuery(SqlQuerySpec querySpec, Sort sort) {
        if (sort == null || sort.isUnsorted()) {
            return querySpec;
        } else {
            Matcher matcher = Pattern.compile("\\s(?i)from\\s").matcher(querySpec.getQueryText());
            matcher.find();
            int beginIndex = matcher.start(0) + 6;
            String tableName = querySpec.getQueryText().substring(beginIndex);
            if (tableName.indexOf(" ") != -1) {
                tableName = tableName.substring(0, tableName.indexOf(" "));
            }

            String querySort = AbstractQueryGenerator.generateQuerySort(sort, tableName);
            String queryText = querySpec.getQueryText() + " " + querySort;
            return cloneWithQueryText(querySpec, queryText);
        }
    }

    /**
     * Generate count query.
     *
     * @param querySpec SQL query spec.
     * @return count query
     *
     * WARNING: This function is only to be used with @Query annotations
     */
    public SqlQuerySpec generateCountQuery(SqlQuerySpec querySpec) {
        String queryText = querySpec.getQueryText();
        int fromIndex = queryText.toLowerCase(Locale.US).indexOf(" from ");
        Assert.isTrue(fromIndex >= 0, "query missing from keyword, query=" + queryText);

        String countQueryText = "select value count(1) " + queryText.substring(fromIndex);
        return cloneWithQueryText(querySpec, countQueryText);
    }

}
