// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.implementation.models.DataSourceCredentials;
import com.azure.search.documents.indexes.implementation.models.SearchIndexerDataSource;
import com.azure.search.documents.indexes.models.SearchIndexerDataSourceConnection;

import java.util.Objects;

/**
 * A converter between {@link SearchIndexerDataSource} and {@link SearchIndexerDataSourceConnection}.
 */
public final class SearchIndexerDataSourceConverter {
    /**
     * Maps from {@link SearchIndexerDataSource} to {@link SearchIndexerDataSourceConnection}.
     */
    public static SearchIndexerDataSourceConnection map(SearchIndexerDataSource obj) {
        if (obj == null) {
            return null;
        }

        String connectionString = obj.getCredentials() == null ? null
            : obj.getCredentials().getConnectionString();
        SearchIndexerDataSourceConnection searchIndexerDataSourceConnection = new SearchIndexerDataSourceConnection(
            obj.getName(), obj.getType(), connectionString, obj.getContainer());

        searchIndexerDataSourceConnection.setDataChangeDetectionPolicy(obj.getDataChangeDetectionPolicy());
        searchIndexerDataSourceConnection.setDescription(obj.getDescription());
        searchIndexerDataSourceConnection.setDataDeletionDetectionPolicy(obj.getDataDeletionDetectionPolicy());
        searchIndexerDataSourceConnection.setETag(obj.getETag());
        searchIndexerDataSourceConnection.setEncryptionKey(obj.getEncryptionKey());

        return searchIndexerDataSourceConnection;
    }

    /**
     * Maps from {@link SearchIndexerDataSourceConnection} to {@link SearchIndexerDataSource}.
     */
    public static SearchIndexerDataSource map(SearchIndexerDataSourceConnection obj) {
        if (obj == null) {
            return null;
        }
        Objects.requireNonNull(obj.getName(), "The SearchIndexerDataSourceConnection name cannot be null");

        DataSourceCredentials credentials = new DataSourceCredentials();
        credentials.setConnectionString(obj.getConnectionString());
        SearchIndexerDataSource searchIndexerDataSource = new SearchIndexerDataSource()
            .setName(obj.getName())
            .setType(obj.getType())
            .setCredentials(credentials)
            .setContainer(obj.getContainer());

        searchIndexerDataSource.setDataChangeDetectionPolicy(obj.getDataChangeDetectionPolicy());
        searchIndexerDataSource.setDescription(obj.getDescription());
        searchIndexerDataSource.setDataDeletionDetectionPolicy(obj.getDataDeletionDetectionPolicy());
        searchIndexerDataSource.setETag(obj.getETag());
        searchIndexerDataSource.setEncryptionKey(obj.getEncryptionKey());

        return searchIndexerDataSource;
    }

    private SearchIndexerDataSourceConverter() {
    }
}
