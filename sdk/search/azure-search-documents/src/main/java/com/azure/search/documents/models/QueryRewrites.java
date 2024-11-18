// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.models;

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
     */
    public QueryRewrites(QueryRewritesType rewritesType) {
        this.rewritesType = rewritesType;
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
        if (rewritesType == null) {
            return null;
        }

        String queryRewritesTypeString = rewritesType.toString();

        if (rewritesType == QueryRewritesType.NONE || count == null) {
            return queryRewritesTypeString;
        }

        return queryRewritesTypeString + "|count-" + count;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof QueryRewrites)) {
            return false;
        }

        QueryRewrites other = (QueryRewrites) obj;
        return toString().equals(other.toString());
    }

    public static QueryRewrites fromString(String str) {
        if (str == null) {
            return null;
        }

        String[] parts = str.split("\\|");
        QueryRewritesType rewritesType = QueryRewritesType.fromString(parts[0]);

        if (parts.length == 1) {
            return new QueryRewrites(rewritesType);
        }

        String[] countParts = parts[1].split("-");
        return new QueryRewrites(rewritesType).setCount(Integer.parseInt(countParts[1]));
    }
}
