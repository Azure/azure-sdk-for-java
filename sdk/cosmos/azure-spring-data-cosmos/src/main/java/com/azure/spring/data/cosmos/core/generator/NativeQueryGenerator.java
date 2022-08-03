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

    private static final NativeQueryGenerator INSTANCE = new NativeQueryGenerator();

    /**
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
     */
    public SqlQuerySpec generateSortedQuery(SqlQuerySpec querySpec, Sort sort) {
        if (sort == null || sort.isUnsorted()) {
            return querySpec;
        } else {
            Pattern pattern1 = Pattern.compile("\\s(?i)from root\\s");
            Pattern pattern2 = Pattern.compile("\\s(?i)from\\s");
            Matcher matcher = pattern1.matcher(querySpec.getQueryText());
            int beginIndex = 0;
            if (matcher.find()) {
                beginIndex = matcher.start(0) + 11;
            } else {
                matcher = pattern2.matcher(querySpec.getQueryText());
                matcher.find();
                beginIndex = matcher.start(0) + 6;
            }
            String tableName = querySpec.getQueryText().substring(beginIndex);
            tableName = tableName.substring(0, tableName.indexOf(" "));

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
     */
    public SqlQuerySpec generateCountQuery(SqlQuerySpec querySpec) {
        String queryText = querySpec.getQueryText();
        int fromIndex = queryText.toLowerCase(Locale.US).indexOf(" from ");
        Assert.isTrue(fromIndex >= 0, "query missing from keyword, query=" + queryText);

        String countQueryText = "select value count(1) " + queryText.substring(fromIndex);
        return cloneWithQueryText(querySpec, countQueryText);
    }

}
