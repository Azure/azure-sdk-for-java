// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes;

import com.azure.core.util.CoreUtils;
import com.azure.search.documents.indexes.models.DataChangeDetectionPolicy;
import com.azure.search.documents.indexes.models.DataDeletionDetectionPolicy;
import com.azure.search.documents.indexes.models.HighWaterMarkChangeDetectionPolicy;
import com.azure.search.documents.indexes.models.SearchIndexerDataContainer;
import com.azure.search.documents.indexes.models.SearchIndexerDataSourceConnection;
import com.azure.search.documents.indexes.models.SearchIndexerDataSourceType;

/**
 * Utility class that aids in the creation of {@link SearchIndexerDataSourceConnection
 * SearchIndexerDataSourceConnections}.
 */
public final class SearchIndexerDataSources {

    /**
     * Creates a new {@link SearchIndexerDataSourceConnection} to connect to an Azure SQL database.
     *
     * @param dataSourceName The name of the data source.
     * @param sqlConnectionString The connection string for the Azure SQL database.
     * @param tableOrViewName The name of the table or view from which to read rows.
     * @param description Optional. Description of the data source.
     * @param changeDetectionPolicy The change detection policy for the data source. Note that only high watermark
     * change detection is allowed for Azure SQL when deletion detection is enabled.
     * @param deletionDetectionPolicy Optional. The data deletion detection policy for the data source.
     * @return A new Azure SQL {@link SearchIndexerDataSourceConnection} instance.
     * @throws IllegalArgumentException If {@code dataSourceName}, {@code sqlConnectionString}, or {@code
     * tableOrViewName} is null or empty.
     */
    public static SearchIndexerDataSourceConnection createFromAzureSql(String dataSourceName,
        String sqlConnectionString, String tableOrViewName, String description,
        DataChangeDetectionPolicy changeDetectionPolicy, DataDeletionDetectionPolicy deletionDetectionPolicy) {
        if (CoreUtils.isNullOrEmpty(dataSourceName)) {
            throw new IllegalArgumentException("'dataSourceName' cannot be null or empty.");
        }
        if (CoreUtils.isNullOrEmpty(sqlConnectionString)) {
            throw new IllegalArgumentException("'sqlConnectionString' cannot be null or empty.");
        }
        if (CoreUtils.isNullOrEmpty(tableOrViewName)) {
            throw new IllegalArgumentException("'tableOrViewName' cannot be null or empty.");
        }

        return createSearchIndexerDataSource(dataSourceName, SearchIndexerDataSourceType.AZURE_SQL, sqlConnectionString,
            tableOrViewName, null,
            description, changeDetectionPolicy, deletionDetectionPolicy);
    }

    /**
     * Creates a new {@link SearchIndexerDataSourceConnection} to connect to an Azure SQL database.
     *
     * @param dataSourceName The name of the data source.
     * @param sqlConnectionString The connection string for the Azure SQL database.
     * @param tableOrViewName The name of the table or view from which to read rows.
     * @return A new Azure SQL {@link SearchIndexerDataSourceConnection} instance.
     * @throws IllegalArgumentException If {@code dataSourceName}, {@code sqlConnectionString}, or {@code
     * tableOrViewName} is null or empty.
     */
    public static SearchIndexerDataSourceConnection createFromAzureSql(String dataSourceName,
        String sqlConnectionString, String tableOrViewName) {
        return createFromAzureSql(dataSourceName, sqlConnectionString, tableOrViewName, null,
            null, null);
    }

    /**
     * Creates a new {@link SearchIndexerDataSourceConnection} to connect to an Azure Blob container.
     *
     * @param dataSourceName The name of the data source.
     * @param storageConnectionString The connection string for the Azure Storage account. The Storage connection string
     * must use this format:
     * <p>
     * {@code "DefaultEndpointsProtocol=https;AccountName=[your storage account];AccountKey=[your account key]:}
     * <p>
     * <em> Note: The connection string must use HTTPS. </em>
     * @param containerName The name of the container from which to read blobs.
     * @param pathPrefix Optional. Limits the data source to only include blobs starting with the specified prefix, this
     * is useful when blobs are organized into "virtual folders".
     * @param description Optional. Description of the data source
     * @param deletionDetectionPolicy Optional. The data deletion detection policy for the data source
     * @return A new Azure Blob {@link SearchIndexerDataSourceConnection} instance.
     * @throws IllegalArgumentException If {@code dataSourceName}, {@code containerName} or {@code
     * storageConnectionString} is null or empty.
     */
    public static SearchIndexerDataSourceConnection createFromAzureBlobStorage(String dataSourceName,
        String storageConnectionString, String containerName, String pathPrefix, String description,
        DataDeletionDetectionPolicy deletionDetectionPolicy) {
        if (CoreUtils.isNullOrEmpty(dataSourceName)) {
            throw new IllegalArgumentException("'dataSourceName' cannot be null or empty.");
        }
        if (CoreUtils.isNullOrEmpty(storageConnectionString)) {
            throw new IllegalArgumentException("'storageConnectionString' cannot be null or empty.");
        }
        if (CoreUtils.isNullOrEmpty(containerName)) {
            throw new IllegalArgumentException("'containerName' cannot be null or empty.");
        }

        return createSearchIndexerDataSource(dataSourceName, SearchIndexerDataSourceType.AZURE_BLOB,
            storageConnectionString, containerName, pathPrefix, description, null, deletionDetectionPolicy);
    }

    /**
     * Creates a new {@link SearchIndexerDataSourceConnection} to connect to an Azure Blob container.
     *
     * @param dataSourceName The name of the data source.
     * @param storageConnectionString The connection string for the Azure Storage account. The Storage connection string
     * must use this format:
     * <p>
     * {@code "DefaultEndpointsProtocol=https;AccountName=[your storage account];AccountKey=[your account key]:}
     * <p>
     * <em> Note: The connection string must use HTTPS. </em>
     * @param containerName The name of the container from which to read blobs.
     * @return A new Azure Blob {@link SearchIndexerDataSourceConnection} instance.
     * @throws IllegalArgumentException If {@code dataSourceName}, {@code containerName} or {@code
     * storageConnectionString} is null or empty.
     */
    public static SearchIndexerDataSourceConnection createFromAzureBlobStorage(String dataSourceName,
        String storageConnectionString, String containerName) {
        return createFromAzureBlobStorage(dataSourceName, storageConnectionString, containerName, null, null, null);
    }

    /**
     * Creates a new {@link SearchIndexerDataSourceConnection} to connect to an Azure Table.
     *
     * @param dataSourceName The name of the data source.
     * @param storageConnectionString The connection string for the Azure Storage account. The Storage connection string
     * must use this format:
     * <p>
     * {@code "DefaultEndpointsProtocol=https;AccountName=[your storage account];AccountKey=[your account key]:}
     * <p>
     * <em> Note: The connection string must use HTTPS. </em>
     * @param tableName The name of the Azure table from which to read rows.
     * @param query Optional. A query that is applied to the table when reading rows.
     * @param description Optional. Description of the data source
     * @param deletionDetectionPolicy Optional. The data deletion detection policy for the data source.
     * @return A new Azure Table {@link SearchIndexerDataSourceConnection} instance.
     * @throws IllegalArgumentException If {@code dataSourceName}, {@code tableName}, or {@code storageConnectionString}
     * is null or empty.
     */
    public static SearchIndexerDataSourceConnection createFromAzureTableStorage(String dataSourceName,
        String storageConnectionString, String tableName, String query, String description,
        DataDeletionDetectionPolicy deletionDetectionPolicy) {
        if (CoreUtils.isNullOrEmpty(dataSourceName)) {
            throw new IllegalArgumentException("'dataSourceName' cannot be null or empty.");
        }
        if (CoreUtils.isNullOrEmpty(tableName)) {
            throw new IllegalArgumentException("'tableName' cannot be null or empty.");
        }
        if (CoreUtils.isNullOrEmpty(storageConnectionString)) {
            throw new IllegalArgumentException("'storageConnectionString' cannot be null or empty.");
        }

        return createSearchIndexerDataSource(dataSourceName, SearchIndexerDataSourceType.AZURE_TABLE,
            storageConnectionString, tableName, query, description, null, deletionDetectionPolicy);
    }

    /**
     * Creates a new {@link SearchIndexerDataSourceConnection} to connect to an Azure Table.
     *
     * @param dataSourceName The name of the data source.
     * @param storageConnectionString The connection string for the Azure Storage account. The Storage connection string
     * must use this format:
     * <p>
     * {@code "DefaultEndpointsProtocol=https;AccountName=[your storage account];AccountKey=[your account key]:}
     * <p>
     * <em> Note: The connection string must use HTTPS. </em>
     * @param tableName The name of the Azure table from which to read rows.
     * @return A new Azure Table {@link SearchIndexerDataSourceConnection} instance.
     * @throws IllegalArgumentException If {@code dataSourceName}, {@code tableName}, or {@code storageConnectionString}
     * is null or empty.
     */
    public static SearchIndexerDataSourceConnection createFromAzureTableStorage(String dataSourceName,
        String storageConnectionString, String tableName) {
        return createFromAzureTableStorage(dataSourceName, storageConnectionString, tableName, null, null, null);
    }

    /**
     * Creates a new {@link SearchIndexerDataSourceConnection} to connect to a Cosmos database.
     *
     * @param dataSourceName The name of the data source.
     * @param cosmosConnectionString The connection string for the Cosmos database. It must follow this format:
     * <p>
     * {@code AccountName|AccountEndpoint=[your account name or endpoint]; AccountKey=[your account key];Database=[your
     * database name]"}
     * @param collectionName The name of the collection from which to read documents.
     * @param query Optional. A query that is applied to the collection when reading documents.
     * @param useChangeDetection Optional. Indicates whether to use change detection when indexing. Default is true.
     * @param description Optional. Description of the data source
     * @param deletionDetectionPolicy Optional. The data deletion detection policy for the data source.
     * @return A new Cosmos {@link SearchIndexerDataSourceConnection} instance.
     * @throws IllegalArgumentException If {@code dataSourceName}, {@code collectionName}, or {@code
     * cosmosConnectionString} is null or empty.
     */
    public static SearchIndexerDataSourceConnection createFromCosmos(String dataSourceName,
        String cosmosConnectionString, String collectionName, String query, Boolean useChangeDetection,
        String description, DataDeletionDetectionPolicy deletionDetectionPolicy) {
        if (CoreUtils.isNullOrEmpty(dataSourceName)) {
            throw new IllegalArgumentException("'dataSourceName' cannot be null or empty.");
        }
        if (CoreUtils.isNullOrEmpty(collectionName)) {
            throw new IllegalArgumentException("'collectionName' cannot be null or empty.");
        }
        if (CoreUtils.isNullOrEmpty(cosmosConnectionString)) {
            throw new IllegalArgumentException("'cosmosConnectionString' cannot be null or empty.");
        }

        DataChangeDetectionPolicy changeDetectionPolicy = useChangeDetection
            ? new HighWaterMarkChangeDetectionPolicy("_ts") : null;

        return createSearchIndexerDataSource(dataSourceName, SearchIndexerDataSourceType.COSMOS_DB,
            cosmosConnectionString, collectionName, query,
            description, changeDetectionPolicy, deletionDetectionPolicy);
    }

    /**
     * Creates a new {@link SearchIndexerDataSourceConnection} to connect to a Cosmos database.
     *
     * @param dataSourceName The name of the data source.
     * @param cosmosConnectionString The connection string for the Cosmos database. It must follow this format:
     * <p>
     * {@code AccountName|AccountEndpoint=[your account name or endpoint]; AccountKey=[your account key];Database=[your
     * database name]"}
     * @param collectionName The name of the collection from which to read documents
     * @param useChangeDetection Optional. Indicates whether to use change detection when indexing. Default is true.
     * @return A new Cosmos {@link SearchIndexerDataSourceConnection} instance.
     * @throws IllegalArgumentException If {@code dataSourceName}, {@code collectionName}, or {@code
     * cosmosConnectionString} is null or empty.
     */
    public static SearchIndexerDataSourceConnection createFromCosmos(String dataSourceName,
        String cosmosConnectionString, String collectionName, Boolean useChangeDetection) {
        return createFromCosmos(dataSourceName, cosmosConnectionString, collectionName, null, useChangeDetection, null,
            null);
    }

    /**
     * Creates a new {@link SearchIndexerDataSourceConnection} to connect to a Cosmos database with change detection
     * set to true.
     *
     * @param dataSourceName The name of the data source.
     * @param cosmosConnectionString The connection string for the Cosmos database. It must follow this format:
     * <p>
     * {@code AccountName|AccountEndpoint=[your account name or endpoint]; AccountKey=[your account key];Database=[your
     * database name]"}
     * @param collectionName The name of the collection from which to read documents
     * @return A new Cosmos {@link SearchIndexerDataSourceConnection} instance.
     * @throws IllegalArgumentException If {@code dataSourceName}, {@code collectionName}, or {@code
     * cosmosConnectionString} is null or empty.
     */
    public static SearchIndexerDataSourceConnection createFromCosmos(String dataSourceName,
        String cosmosConnectionString, String collectionName) {
        return createFromCosmos(dataSourceName, cosmosConnectionString, collectionName, null,
            true, null, null);
    }

    /*
     * Helper method that creates a generic SearchIndexerDataSource.
     */
    private static SearchIndexerDataSourceConnection createSearchIndexerDataSource(String name,
        SearchIndexerDataSourceType type, String connectionString, String dataSourceName, String dataSourceQuery,
        String description, DataChangeDetectionPolicy dataChangeDetectionPolicy,
        DataDeletionDetectionPolicy dataDeletionDetectionPolicy) {
        return new SearchIndexerDataSourceConnection(name, type, connectionString,
            new SearchIndexerDataContainer(dataSourceName).setQuery(dataSourceQuery))
            .setDescription(description)
            .setDataChangeDetectionPolicy(dataChangeDetectionPolicy)
            .setDataDeletionDetectionPolicy(dataDeletionDetectionPolicy);
    }

    private SearchIndexerDataSources() {
    }
}
