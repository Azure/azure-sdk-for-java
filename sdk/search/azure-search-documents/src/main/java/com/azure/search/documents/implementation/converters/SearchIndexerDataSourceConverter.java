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
    private static final ClientLogger LOGGER = new ClientLogger(SearchIndexerDataSourceConverter.class);

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
            SearchIndexerDataContainer _container = SearchIndexerDataContainerConverter.map(obj.getContainer());
            searchIndexerDataSource.setContainer(_container);
        }

        if (obj.getDataChangeDetectionPolicy() != null) {
            DataChangeDetectionPolicy _dataChangeDetectionPolicy =
                DataChangeDetectionPolicyConverter.map(obj.getDataChangeDetectionPolicy());
            searchIndexerDataSource.setDataChangeDetectionPolicy(_dataChangeDetectionPolicy);
        }

        if (obj.getCredentials() != null) {
            DataSourceCredentials _credentials = DataSourceCredentialsConverter.map(obj.getCredentials());
            searchIndexerDataSource.setCredentials(_credentials);
        }

        String _name = obj.getName();
        searchIndexerDataSource.setName(_name);

        String _description = obj.getDescription();
        searchIndexerDataSource.setDescription(_description);

        if (obj.getDataDeletionDetectionPolicy() != null) {
            DataDeletionDetectionPolicy _dataDeletionDetectionPolicy =
                DataDeletionDetectionPolicyConverter.map(obj.getDataDeletionDetectionPolicy());
            searchIndexerDataSource.setDataDeletionDetectionPolicy(_dataDeletionDetectionPolicy);
        }

        String _eTag = obj.getETag();
        searchIndexerDataSource.setETag(_eTag);

        if (obj.getType() != null) {
            SearchIndexerDataSourceType _type = SearchIndexerDataSourceTypeConverter.map(obj.getType());
            searchIndexerDataSource.setType(_type);
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
            com.azure.search.documents.implementation.models.SearchIndexerDataContainer _container =
                SearchIndexerDataContainerConverter.map(obj.getContainer());
            searchIndexerDataSource.setContainer(_container);
        }

        if (obj.getDataChangeDetectionPolicy() != null) {
            com.azure.search.documents.implementation.models.DataChangeDetectionPolicy _dataChangeDetectionPolicy =
                DataChangeDetectionPolicyConverter.map(obj.getDataChangeDetectionPolicy());
            searchIndexerDataSource.setDataChangeDetectionPolicy(_dataChangeDetectionPolicy);
        }

        if (obj.getCredentials() != null) {
            com.azure.search.documents.implementation.models.DataSourceCredentials _credentials =
                DataSourceCredentialsConverter.map(obj.getCredentials());
            searchIndexerDataSource.setCredentials(_credentials);
        }

        String _name = obj.getName();
        searchIndexerDataSource.setName(_name);

        String _description = obj.getDescription();
        searchIndexerDataSource.setDescription(_description);

        if (obj.getDataDeletionDetectionPolicy() != null) {
            com.azure.search.documents.implementation.models.DataDeletionDetectionPolicy _dataDeletionDetectionPolicy = DataDeletionDetectionPolicyConverter.map(obj.getDataDeletionDetectionPolicy());
            searchIndexerDataSource.setDataDeletionDetectionPolicy(_dataDeletionDetectionPolicy);
        }

        String _eTag = obj.getETag();
        searchIndexerDataSource.setETag(_eTag);

        if (obj.getType() != null) {
            com.azure.search.documents.implementation.models.SearchIndexerDataSourceType _type =
                SearchIndexerDataSourceTypeConverter.map(obj.getType());
            searchIndexerDataSource.setType(_type);
        }
        return searchIndexerDataSource;
    }
}
