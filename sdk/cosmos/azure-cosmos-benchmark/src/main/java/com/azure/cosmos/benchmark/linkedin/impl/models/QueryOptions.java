// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.linkedin.impl.models;

import com.azure.cosmos.models.SqlParameter;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * QueryOptions are the options you can specify with SQL expression and SQL parameters for CRUD operations on a data store.
 */
public class QueryOptions {
    // DocumentDB SQL query expression
    // TODO: Replace this with the object structure in our implementation if needed
    private final String _documentDBQuery;

    //SQL expression parameters
    private final List<SqlParameter> _sqlParameters;

    // ContinuationToken represents whether there are more matched items.
    private final String _continuationToken;

    private final Integer _pageSize;

    private String _partitioningKey;

    private String _stringRepresentation;

    private QueryOptions(final String continuationToken, final String documentDBQuery,
        final List<SqlParameter> sqlParameters, final Integer pageSize, final String partitioningKey) {
        _continuationToken = continuationToken;
        _documentDBQuery = documentDBQuery;
        _sqlParameters = sqlParameters;
        _pageSize = pageSize;
        _partitioningKey = partitioningKey;
    }

    public String getDocumentDBQuery() {
        return _documentDBQuery;
    }

    public Optional<List<SqlParameter>> getSqlParameterList() {
        return Optional.ofNullable(_sqlParameters);
    }

    public Optional<String> getContinuationToken() {
        return Optional.ofNullable(_continuationToken);
    }

    public Optional<Integer> getPageSize() {
        return Optional.ofNullable(_pageSize);
    }

    public Optional<String> getPartitioningKey() {
        return Optional.ofNullable(_partitioningKey);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        QueryOptions that = (QueryOptions) o;

        return Objects.equals(_documentDBQuery, that._documentDBQuery) && Objects.equals(_continuationToken,
            that._continuationToken) && Objects.equals(_pageSize, that._pageSize) && Objects.equals(_partitioningKey,
            that._partitioningKey) && (mapToKeyValueSqlParams(_sqlParameters).equals(
            mapToKeyValueSqlParams(that._sqlParameters)));
    }

    @Override
    public int hashCode() {
        return Objects.hash(_continuationToken, _documentDBQuery, _pageSize, _partitioningKey,
            mapToKeyValueSqlParams(_sqlParameters));
    }

    @Override
    public String toString() {
        if (Objects.isNull(_stringRepresentation)) {
            _stringRepresentation = MoreObjects.toStringHelper(this)
                .omitNullValues()
                .add("documentDBQuery", _documentDBQuery)
                .add("sqlParameters", _sqlParameters)
                .add("pageSize", _pageSize)
                .add("partitioningKey", _partitioningKey)
                .toString();
        }

        return _stringRepresentation;
    }

    private Map<String, Object> mapToKeyValueSqlParams(final List<SqlParameter> sqlParameters) {
        return Optional.ofNullable(sqlParameters)
            .map(Collection::stream)
            .orElseGet(Stream::empty)
            .collect(Collectors.toMap(SqlParameter::getName, param -> param.getValue(String.class)));
    }

    /**
     * Builder class to ensure an invalid QueryOptions can never be created/passed around
     */
    public static class Builder {
        // SQL query expression for query in cosmos DB
        private String _documentDBQuery;

        // SQL parameters for building the SQL expression
        private List<SqlParameter> _sqlParameters;

        // ContinuationToken for paging, which indicates there are more items
        private String _continuationToken;

        // pageSize for paging, which determines the size of the pages returned in the result.
        private Integer _pageSize;

        // partitioningKey to use for narrowing down the search space.
        private String _partitioningKey;

        public Builder setDocumentDBQuery(final String documentDBQuery) {
            Preconditions.checkNotNull(documentDBQuery, "SQL expression for querying can not be null");
            _documentDBQuery = documentDBQuery;
            return this;
        }

        public Builder setSqlParameterList(final List<SqlParameter> sqlParameters) {
            _sqlParameters = Preconditions.checkNotNull(sqlParameters, "sqlParameters cannot be null");
            return this;
        }

        public Builder setContinuationToken(final String continuationToken) {
            _continuationToken = Preconditions.checkNotNull(continuationToken, "continuation cannot be null");
            return this;
        }

        public Builder setPageSize(final Integer pageSize) {
            _pageSize = Preconditions.checkNotNull(pageSize, "pageSize cannot be null");
            return this;
        }

        public Builder setPartitioningKey(final String partitioningKey) {
            _partitioningKey = Preconditions.checkNotNull(partitioningKey, "partitioningKey cannot be null");
            return this;
        }

        /**
         * There are no Preconditions for the QueryOptions
         */
        public QueryOptions build() {
            Preconditions.checkNotNull(_documentDBQuery, "SQL expression cannot be null for querying");
            return new QueryOptions(_continuationToken, _documentDBQuery, _sqlParameters, _pageSize, _partitioningKey);
        }
    }
}
