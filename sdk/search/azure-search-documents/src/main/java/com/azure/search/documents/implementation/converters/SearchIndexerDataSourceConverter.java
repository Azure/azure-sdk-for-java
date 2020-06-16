// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.implementation.models.DataSourceCredentials;
import com.azure.search.documents.indexes.implementation.models.SearchIndexerDataSource;
import com.azure.search.documents.indexes.models.DataChangeDetectionPolicy;
import com.azure.search.documents.indexes.models.DataDeletionDetectionPolicy;
import com.azure.search.documents.indexes.models.SearchIndexerDataContainer;
import com.azure.search.documents.indexes.models.SearchIndexerDataSourceConnection;
import com.azure.search.documents.indexes.models.SearchIndexerDataSourceType;

/**
 * A converter between {@link SearchIndexerDataSource} and
 * {@link SearchIndexerDataSourceConnection}.
 */
public final class SearchIndexerDataSourceConverter {
    /**
     * Maps from {@link SearchIndexerDataSource} to
     * {@link SearchIndexerDataSourceConnection}.
     */
    public static SearchIndexerDataSourceConnection map(SearchIndexerDataSource obj) {
        if (obj == null) {
            return null;
        }
        SearchIndexerDataSourceConnection searchIndexerDataSourceConnection = new SearchIndexerDataSourceConnection();

        if (obj.getContainer() != null) {
            SearchIndexerDataContainer container = SearchIndexerDataContainerConverter.map(obj.getContainer());
            searchIndexerDataSourceConnection.setContainer(container);
        }

        if (obj.getDataChangeDetectionPolicy() != null) {
            DataChangeDetectionPolicy dataChangeDetectionPolicy =
                DataChangeDetectionPolicyConverter.map(obj.getDataChangeDetectionPolicy());
            searchIndexerDataSourceConnection.setDataChangeDetectionPolicy(dataChangeDetectionPolicy);
        }
        if (obj.getCredentials() != null) {
            searchIndexerDataSourceConnection.setConnectionString(obj.getCredentials().getConnectionString());
        }

        String name = obj.getName();
        searchIndexerDataSourceConnection.setName(name);

        String description = obj.getDescription();
        searchIndexerDataSourceConnection.setDescription(description);

        if (obj.getDataDeletionDetectionPolicy() != null) {
            DataDeletionDetectionPolicy dataDeletionDetectionPolicy =
                DataDeletionDetectionPolicyConverter.map(obj.getDataDeletionDetectionPolicy());
            searchIndexerDataSourceConnection.setDataDeletionDetectionPolicy(dataDeletionDetectionPolicy);
        }

        String eTag = obj.getETag();
        searchIndexerDataSourceConnection.setETag(eTag);

        if (obj.getType() != null) {
            SearchIndexerDataSourceType type = SearchIndexerDataSourceTypeConverter.map(obj.getType());
            searchIndexerDataSourceConnection.setType(type);
        }
        return searchIndexerDataSourceConnection;
    }

    /**
     * Maps from {@link SearchIndexerDataSourceConnection} to
     * {@link SearchIndexerDataSource}.
     */
    public static SearchIndexerDataSource map(SearchIndexerDataSourceConnection obj) {
        if (obj == null) {
            return null;
        }
        SearchIndexerDataSource searchIndexerDataSource =
            new SearchIndexerDataSource();

        if (obj.getContainer() != null) {
            com.azure.search.documents.indexes.implementation.models.SearchIndexerDataContainer container =
                SearchIndexerDataContainerConverter.map(obj.getContainer());
            searchIndexerDataSource.setContainer(container);
        }

        if (obj.getDataChangeDetectionPolicy() != null) {
            com.azure.search.documents.indexes.implementation.models.DataChangeDetectionPolicy
                dataChangeDetectionPolicy = DataChangeDetectionPolicyConverter.map(obj.getDataChangeDetectionPolicy());
            searchIndexerDataSource.setDataChangeDetectionPolicy(dataChangeDetectionPolicy);
        }

        DataSourceCredentials credentials = new DataSourceCredentials();
        credentials.setConnectionString(obj.getConnectionString());
        searchIndexerDataSource.setCredentials(credentials);

        String name = obj.getName();
        searchIndexerDataSource.setName(name);

        String description = obj.getDescription();
        searchIndexerDataSource.setDescription(description);

        if (obj.getDataDeletionDetectionPolicy() != null) {
            com.azure.search.documents.indexes.implementation.models.DataDeletionDetectionPolicy dataDeletionDetectionPolicy
                = DataDeletionDetectionPolicyConverter.map(obj.getDataDeletionDetectionPolicy());
            searchIndexerDataSource.setDataDeletionDetectionPolicy(dataDeletionDetectionPolicy);
        }

        String eTag = obj.getETag();
        searchIndexerDataSource.setETag(eTag);

        if (obj.getType() != null) {
            com.azure.search.documents.indexes.implementation.models.SearchIndexerDataSourceType type =
                SearchIndexerDataSourceTypeConverter.map(obj.getType());
            searchIndexerDataSource.setType(type);
        }
        return searchIndexerDataSource;
    }

    private SearchIndexerDataSourceConverter() {
    }
}
