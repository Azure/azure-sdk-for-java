// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.DataChangeDetectionPolicy;
import com.azure.search.documents.models.DataDeletionDetectionPolicy;
import com.azure.search.documents.models.DataSourceCredentials;
import com.azure.search.documents.models.SearchIndexerDataContainer;
import com.azure.search.documents.models.SearchIndexerDataSource;
import com.azure.search.documents.models.SearchIndexerDataSourceType;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.SearchIndexerDataSource} and
 * {@link SearchIndexerDataSource}.
 */
public final class SearchIndexerDataSourceConverter {
    /**
     * Maps from {@link com.azure.search.documents.implementation.models.SearchIndexerDataSource} to
     * {@link SearchIndexerDataSource}.
     */
    public static SearchIndexerDataSource map(com.azure.search.documents.implementation.models.SearchIndexerDataSource obj) {
        if (obj == null) {
            return null;
        }
        SearchIndexerDataSource searchIndexerDataSource = new SearchIndexerDataSource();

        if (obj.getContainer() != null) {
            SearchIndexerDataContainer container = SearchIndexerDataContainerConverter.map(obj.getContainer());
            searchIndexerDataSource.setContainer(container);
        }

        if (obj.getDataChangeDetectionPolicy() != null) {
            DataChangeDetectionPolicy dataChangeDetectionPolicy =
                DataChangeDetectionPolicyConverter.map(obj.getDataChangeDetectionPolicy());
            searchIndexerDataSource.setDataChangeDetectionPolicy(dataChangeDetectionPolicy);
        }

        if (obj.getCredentials() != null) {
            DataSourceCredentials credentials = DataSourceCredentialsConverter.map(obj.getCredentials());
            searchIndexerDataSource.setCredentials(credentials);
        }

        String name = obj.getName();
        searchIndexerDataSource.setName(name);

        String description = obj.getDescription();
        searchIndexerDataSource.setDescription(description);

        if (obj.getDataDeletionDetectionPolicy() != null) {
            DataDeletionDetectionPolicy dataDeletionDetectionPolicy =
                DataDeletionDetectionPolicyConverter.map(obj.getDataDeletionDetectionPolicy());
            searchIndexerDataSource.setDataDeletionDetectionPolicy(dataDeletionDetectionPolicy);
        }

        String eTag = obj.getETag();
        searchIndexerDataSource.setETag(eTag);

        if (obj.getType() != null) {
            SearchIndexerDataSourceType type = SearchIndexerDataSourceTypeConverter.map(obj.getType());
            searchIndexerDataSource.setType(type);
        }
        return searchIndexerDataSource;
    }

    /**
     * Maps from {@link SearchIndexerDataSource} to
     * {@link com.azure.search.documents.implementation.models.SearchIndexerDataSource}.
     */
    public static com.azure.search.documents.implementation.models.SearchIndexerDataSource map(SearchIndexerDataSource obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.SearchIndexerDataSource searchIndexerDataSource =
            new com.azure.search.documents.implementation.models.SearchIndexerDataSource();

        if (obj.getContainer() != null) {
            com.azure.search.documents.implementation.models.SearchIndexerDataContainer container =
                SearchIndexerDataContainerConverter.map(obj.getContainer());
            searchIndexerDataSource.setContainer(container);
        }

        if (obj.getDataChangeDetectionPolicy() != null) {
            com.azure.search.documents.implementation.models.DataChangeDetectionPolicy dataChangeDetectionPolicy =
                DataChangeDetectionPolicyConverter.map(obj.getDataChangeDetectionPolicy());
            searchIndexerDataSource.setDataChangeDetectionPolicy(dataChangeDetectionPolicy);
        }

        if (obj.getCredentials() != null) {
            com.azure.search.documents.implementation.models.DataSourceCredentials credentials =
                DataSourceCredentialsConverter.map(obj.getCredentials());
            searchIndexerDataSource.setCredentials(credentials);
        }

        String name = obj.getName();
        searchIndexerDataSource.setName(name);

        String description = obj.getDescription();
        searchIndexerDataSource.setDescription(description);

        if (obj.getDataDeletionDetectionPolicy() != null) {
            com.azure.search.documents.implementation.models.DataDeletionDetectionPolicy dataDeletionDetectionPolicy
                = DataDeletionDetectionPolicyConverter.map(obj.getDataDeletionDetectionPolicy());
            searchIndexerDataSource.setDataDeletionDetectionPolicy(dataDeletionDetectionPolicy);
        }

        String eTag = obj.getETag();
        searchIndexerDataSource.setETag(eTag);

        if (obj.getType() != null) {
            com.azure.search.documents.implementation.models.SearchIndexerDataSourceType type =
                SearchIndexerDataSourceTypeConverter.map(obj.getType());
            searchIndexerDataSource.setType(type);
        }
        return searchIndexerDataSource;
    }

    private SearchIndexerDataSourceConverter() {
    }
}
