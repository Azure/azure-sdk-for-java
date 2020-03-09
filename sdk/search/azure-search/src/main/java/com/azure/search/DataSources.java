// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import com.azure.core.util.CoreUtils;
import com.azure.search.models.DataChangeDetectionPolicy;
import com.azure.search.models.DataContainer;
import com.azure.search.models.DataDeletionDetectionPolicy;
import com.azure.search.models.DataSource;
import com.azure.search.models.DataSourceCredentials;
import com.azure.search.models.DataSourceType;
import com.azure.search.models.HighWaterMarkChangeDetectionPolicy;

/**
 * Utility class generating DataSource object per DataSourceType
 */
public final class DataSources {

    /**
     * Creates a new DataSource to connect to an Azure SQL database.
     *
     * @param dataSourceName The name of the data source.
     * @param sqlConnectionString The connection string for the Azure SQL database.
     * @param tableOrViewName The name of the table or view from which to read rows.
     * @param description Optional. Description of the data source.
     * @param changeDetectionPolicy The change detection policy for the data source. Note that only high watermark
     * change detection is allowed for Azure SQL when deletion detection is enabled.
     * @param deletionDetectionPolicy Optional. The data deletion detection policy for the data source.
     * @return A new DataSource instance.
     * @throws IllegalArgumentException if name, tableName or ConnectionString are null or empty.
     */
    public static DataSource createFromAzureSql(String dataSourceName, String sqlConnectionString,
        String tableOrViewName, String description, DataChangeDetectionPolicy changeDetectionPolicy,
        DataDeletionDetectionPolicy deletionDetectionPolicy) {
        if (CoreUtils.isNullOrEmpty(dataSourceName)) {
            throw new IllegalArgumentException("dataSourceName cannot be null or empty");
        }
        if (CoreUtils.isNullOrEmpty(sqlConnectionString)) {
            throw new IllegalArgumentException("sqlConnectionString cannot be null or empty");
        }
        if (CoreUtils.isNullOrEmpty(tableOrViewName)) {
            throw new IllegalArgumentException("tableOrViewName cannot be null or empty");
        }

        return new DataSource()
            .setName(dataSourceName)
            .setType(DataSourceType.AZURE_SQL)
            .setCredentials(new DataSourceCredentials().setConnectionString(sqlConnectionString))
            .setContainer(new DataContainer().setName(tableOrViewName))
            .setDescription(description)
            .setDataChangeDetectionPolicy(changeDetectionPolicy)
            .setDataDeletionDetectionPolicy(deletionDetectionPolicy);
    }

    /**
     * Creates a new DataSource to connect to an Azure SQL database.
     *
     * @param dataSourceName The name of the data source.
     * @param sqlConnectionString The connection string for the Azure SQL database.
     * @param tableOrViewName The name of the table or view from which to read rows.
     * @return A new DataSource instance.
     */
    public static DataSource createFromAzureSql(String dataSourceName, String sqlConnectionString,
        String tableOrViewName) {
        return DataSources.createFromAzureSql(dataSourceName, sqlConnectionString, tableOrViewName, null, null, null);
    }

    /**
     * Creates a new DataSource to connect to an Azure Blob container.
     *
     * @param dataSourceName The name of the data source.
     * @param storageConnectionString The connection string for the Azure Storage account.
     * The Storage connection string must use this format:
     * <p>
     * {@code "DefaultEndpointsProtocol=https;AccountName=[your storage account];AccountKey=[your account key]:}
     * <p>
     * <em> Note: The connection string must use HTTPS. </em>
     * @param containerName The name of the container from which to read blobs.
     * @param pathPrefix Optional. Limits the data source to only include blobs starting with the specified prefix,
     * this is useful when blobs are organized into "virtual folders".
     * @param description Optional. Description of the data source
     * @param deletionDetectionPolicy Optional. The data deletion detection policy for the data source
     * @throws IllegalArgumentException If {@code dataSourceName}, {@code containerName} or
     * {@code storageConnectionString} are null or empty.
     * @return A new Azure Blob DataSource instance.
     */
    public static DataSource createFromAzureBlobStorage(String dataSourceName, String storageConnectionString,
        String containerName, String pathPrefix, String description,
        DataDeletionDetectionPolicy deletionDetectionPolicy) {
        if (CoreUtils.isNullOrEmpty(dataSourceName)) {
            throw new IllegalArgumentException("dataSourceName cannot be null or empty");
        }
        if (CoreUtils.isNullOrEmpty(storageConnectionString)) {
            throw new IllegalArgumentException("storageConnectionString cannot be null or empty");
        }
        if (CoreUtils.isNullOrEmpty(containerName)) {
            throw new IllegalArgumentException("containerName cannot be null or empty");
        }
        return new DataSource()
            .setName(dataSourceName)
            .setType(DataSourceType.AZURE_BLOB)
            .setCredentials(new DataSourceCredentials()
                .setConnectionString(storageConnectionString))
            .setContainer(new DataContainer()
                .setName(containerName)
                .setQuery(pathPrefix))
            .setDescription(description)
            .setDataDeletionDetectionPolicy(deletionDetectionPolicy);
    }

    /**
     * Creates a new DataSource to connect to an Azure Blob container.
     *
     * @param dataSourceName The name of the data source.
     * @param storageConnectionString The connection string for the Azure Storage account.
     * The Storage connection string must use this format:
     * <p>
     * {@code "DefaultEndpointsProtocol=https;AccountName=[your storage account];AccountKey=[your account key]:}
     * <p>
     * <em> Note: The connection string must use HTTPS. </em>
     * @param containerName The name of the container from which to read blobs.
     * @return A new Azure Blob DataSource instance.
     */
    public static DataSource createFromAzureBlobStorage(String dataSourceName, String storageConnectionString,
        String containerName) {
        return DataSources.createFromAzureBlobStorage(dataSourceName, storageConnectionString, containerName,
            null, null, null);
    }

    /**
     * Creates a new DataSource to connect to an Azure Table.
     *
     * @param dataSourceName The name of the data source.
     * @param storageConnectionString The connection string for the Azure Storage account. It must follow this format:
     * "DefaultEndpointsProtocol=https; AccountName=[your storage account];AccountKey=[your account key];" Note that
     * HTTPS is required.
     * @param tableName The name of the Azure table from which to read rows.
     * @param query Optional. A query that is applied to the table when reading rows.
     * @param description Optional. Description of the data source
     * @param deletionDetectionPolicy Optional. The data deletion detection policy for the data source.
     * @return A new DataSource instance.
     * @throws IllegalArgumentException if name, tableName or storageConnectionString are null or empty.
     */
    public static DataSource createFromAzureTableStorage(String dataSourceName, String storageConnectionString,
        String tableName, String query, String description,
        DataDeletionDetectionPolicy deletionDetectionPolicy) {
        if (CoreUtils.isNullOrEmpty(dataSourceName)) {
            throw new IllegalArgumentException("dataSourceName cannot be null or empty");
        }
        if (CoreUtils.isNullOrEmpty(tableName)) {
            throw new IllegalArgumentException("tableName cannot be null or empty");
        }
        if (CoreUtils.isNullOrEmpty(storageConnectionString)) {
            throw new IllegalArgumentException("storageConnectionString cannot be null or empty");
        }
        return new DataSource()
            .setName(dataSourceName)
            .setType(DataSourceType.AZURE_TABLE)
            .setCredentials(new DataSourceCredentials()
                .setConnectionString(storageConnectionString))
            .setContainer(new DataContainer()
                .setName(tableName)
                .setQuery(query))
            .setDescription(description)
            .setDataDeletionDetectionPolicy(deletionDetectionPolicy);
    }

    /**
     * Creates a new DataSource to connect to an Azure Table.
     *
     * @param dataSourceName The name of the data source.
     * @param storageConnectionString The connection string for the Azure Storage account. It must follow this format:
     * "DefaultEndpointsProtocol=https; AccountName=[your storage account];AccountKey=[your account key];" Note that
     * HTTPS is required.
     * @param tableName The name of the Azure table from which to read rows.
     * @return A new DataSource instance.
     * @throws IllegalArgumentException if name, tableName or storageConnectionString are null or empty.
     */
    public static DataSource createFromAzureTableStorage(String dataSourceName, String storageConnectionString,
        String tableName) {
        return DataSources.createFromAzureTableStorage(dataSourceName, storageConnectionString, tableName, null,
            null, null);
    }

    /**
     * Creates a new DataSource to connect to a Cosmos database.
     *
     * @param dataSourceName The name of the data source.
     * @param cosmosConnectionString The connection string for the Cosmos database. It must follow this format:
     * AccountName|AccountEndpoint=[your account name or endpoint]; AccountKey=[your account key];Database=[your
     * database name]"
     * @param collectionName The name of the collection from which to read documents.
     * @param query Optional. A query that is applied to the collection when reading documents.
     * @param useChangeDetection Optional. Indicates whether to use change detection when indexing. Default is true.
     * @param description Optional. Description of the data source
     * @param deletionDetectionPolicy Optional. The data deletion detection policy for the data source.
     * @return A new DataSource instance.
     * @throws IllegalArgumentException if dataSourceName, collectionName or cosmosConnectionString are null or empty.
     */
    public static DataSource createFromCosmos(String dataSourceName, String cosmosConnectionString,
        String collectionName, String query, Boolean useChangeDetection, String description,
        DataDeletionDetectionPolicy deletionDetectionPolicy) {
        if (CoreUtils.isNullOrEmpty(dataSourceName)) {
            throw new IllegalArgumentException("dataSourceName cannot be null or empty");
        }
        if (CoreUtils.isNullOrEmpty(collectionName)) {
            throw new IllegalArgumentException("collectionName cannot be null or empty");
        }
        if (CoreUtils.isNullOrEmpty(cosmosConnectionString)) {
            throw new IllegalArgumentException("cosmosConnectionString cannot be null or empty");
        }
        return new DataSource()
            .setName(dataSourceName)
            .setType(DataSourceType.COSMOS)
            .setCredentials(new DataSourceCredentials()
                .setConnectionString(cosmosConnectionString))
            .setContainer(new DataContainer()
                .setName(collectionName)
                .setQuery(query))
            .setDataChangeDetectionPolicy(
                useChangeDetection
                    ? new HighWaterMarkChangeDetectionPolicy().setHighWaterMarkColumnName("_ts")
                    : null)
            .setDescription(description)
            .setDataDeletionDetectionPolicy(deletionDetectionPolicy);
    }

    /**
     * Creates a new DataSource to connect to a CosmosDb database with change detection set to true
     *
     * @param dataSourceName The name of the data source.
     * @param cosmosDbConnectionString The connection string for the CosmosDb database. It must follow this format:
     * AccountName|AccountEndpoint=[your account name or endpoint]; AccountKey=[your account key];Database=[your
     * database name]"
     * @param collectionName The name of the collection from which to read documents
     * @param useChangeDetection Optional. Indicates whether to use change detection when indexing. Default is true.
     * @return A new DataSource instance.
     * @throws IllegalArgumentException if name, collectionName or cosmosDbConnectionString are null or empty.
     */
    public static DataSource createFromCosmos(String dataSourceName, String cosmosDbConnectionString,
        String collectionName, Boolean useChangeDetection) {
        return DataSources.createFromCosmos(
            dataSourceName, cosmosDbConnectionString, collectionName, null, useChangeDetection, null, null);
    }

    /**
     * Creates a new DataSource to connect to a CosmosDb database with change detection set to true
     *
     * @param dataSourceName The name of the data source.
     * @param cosmosDbConnectionString The connection string for the CosmosDb database. It must follow this format:
     * AccountName|AccountEndpoint=[your account name or endpoint]; AccountKey=[your account key];Database=[your
     * database name]"
     * @param collectionName The name of the collection from which to read documents
     * @return A new DataSource instance.
     * @throws IllegalArgumentException if name, collectionName or cosmosDbConnectionString are null or empty.
     */
    public static DataSource createFromCosmos(String dataSourceName, String cosmosDbConnectionString,
        String collectionName) {
        return DataSources.createFromCosmos(dataSourceName, cosmosDbConnectionString, collectionName, null,
            true, null, null);
    }
}
