// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.models;

import java.util.HashMap;
import java.util.Objects;

/**
 * Configuration for how semantic search rewrites a query.
 */
public class QueryRewrites {

    private final QueryRewritesType rewritesType;
    private Integer count;

    /**
     * Creates a new instance of {@link QueryRewrites}.
     *
     * @param rewritesType The type of query rewrites to perform.
     * @throws NullPointerException If {@code rewritesType} is null.
     */
    public QueryRewrites(QueryRewritesType rewritesType) {
        this.rewritesType = Objects.requireNonNull(rewritesType, "'rewritesType' cannot be null");
    }

    /**
     * Gets the type of query rewrites to perform.
     *
     * @return The type of query rewrites to perform.
     */
    public QueryRewritesType getRewritesType() {
        return rewritesType;
    }

    /**
     * Gets the number of rewrites to generate.
     * <p>
     * The number of rewrites to return is optional and will default to 10.
     *
     * @return The number of rewrites to generate.
     */
    public Integer getCount() {
        return count;
    }

    /**
     * Sets the number of rewrites to generate.
     * <p>
     * The number of rewrites to return is optional and will default to 10.
     *
     * @param count The number of rewrites to generate.
     * @return The QueryRewrites object itself.
     */
    public QueryRewrites setCount(Integer count) {
        this.count = count;
        return this;
    }

    @Override
    public String toString() {
        String queryRewritesTypeString = rewritesType.toString();

        if (rewritesType == QueryRewritesType.NONE || count == null) {
            return queryRewritesTypeString;
        }

        return queryRewritesTypeString + "|count-" + count;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rewritesType, count);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof QueryRewrites)) {
            return false;
        }

        QueryRewrites other = (QueryRewrites) obj;
        return Objects.equals(rewritesType, other.rewritesType) && Objects.equals(count, other.count);
    }

    /**
     * Parses a {@link QueryRewrites} from a string.
     * @param str The string to parse.
     * @return The parsed {@link QueryRewrites}.
     * @throws IllegalArgumentException If the string is invalid.
     */
    public static QueryRewrites fromString(String str) {
        if (str == null || str.isEmpty()) {
            return null;
        }

        if (!str.contains("|")) {
            return new QueryRewrites(QueryRewritesType.fromString(str));
        }

        String[] parts = new String[2];

        parts[0] = str.substring(0, str.indexOf("|"));
        parts[1] = str.substring(str.indexOf("|") + 1);
        QueryRewritesType rewritesType = QueryRewritesType.fromString(parts[0]);
        HashMap<String, Object> queryRewriteOptions = new HashMap<>();
        for (String queryRewriteOption : parts[1].split(",")) {
            if (queryRewriteOption.contains("-")) {
                String[] optionParts = queryRewriteOption.split("-");
                queryRewriteOptions.putIfAbsent(optionParts[0], optionParts[1]);
            }
        }

        QueryRewrites queryRewrites = new QueryRewrites(rewritesType);

        if (queryRewriteOptions.containsKey("count")) {
            queryRewrites.setCount(Integer.parseInt(queryRewriteOptions.get("count").toString()));
        }

        return queryRewrites;
    }
}
