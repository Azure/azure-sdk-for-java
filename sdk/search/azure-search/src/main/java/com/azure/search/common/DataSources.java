// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.common;

import com.azure.search.models.DataChangeDetectionPolicy;
import com.azure.search.models.DataContainer;
import com.azure.search.models.DataDeletionDetectionPolicy;
import com.azure.search.models.DataSource;
import com.azure.search.models.DataSourceCredentials;
import com.azure.search.models.DataSourceType;
import com.azure.search.models.HighWaterMarkChangeDetectionPolicy;
import org.apache.commons.lang3.StringUtils;

public class DataSources {

    /**
     * Creates a new DataSource to connect to an Azure SQL database.
     *
     * @param name The name of the datasource.
     * @param sqlConnectionString The connection string for the Azure SQL database.
     * @param tableOrViewName The name of the table or view from which to read rows.
     * @param description Optional. Description of the datasource.
     * @param changeDetectionPolicy The change detection policy for the datasource.
     * Note that only high watermark change detection
     * is allowed for Azure SQL when deletion detection is enabled.
     * @param deletionDetectionPolicy Optional. The data deletion detection policy for the datasource.
     * @return A new DataSource instance.
     */
    public static DataSource azureSql(
        String name,
        String sqlConnectionString,
        String tableOrViewName,
        String description,
        DataChangeDetectionPolicy changeDetectionPolicy,
        DataDeletionDetectionPolicy deletionDetectionPolicy) {
        return new DataSource()
            .setName(name)
            .setType(DataSourceType.AZURESQL)
            .setCredentials(new DataSourceCredentials().setConnectionString(sqlConnectionString))
            .setContainer(new DataContainer().setName(tableOrViewName))
            .setDescription(description)
            .setDataDeletionDetectionPolicy(deletionDetectionPolicy)
            .setDataChangeDetectionPolicy(changeDetectionPolicy);
    }

    /**
     * Creates a new DataSource to connect to an Azure Blob container.
     *
     * @param name The name of the datasource.
     * @param storageConnectionString The connection string for the Azure Storage account.
     * It must follow this format: "DefaultEndpointsProtocol=https;AccountName=[your storage account];
     * AccountKey=[your account key];" Note that HTTPS is required.
     * @param containerName The name of the container from which to read blobs.
     * @param pathPrefix Optional. If specified, the datasource will include only blobs
     * with names starting with this prefix. This is useful when blobs are
     * organized into "virtual folders", for example.
     * @param deletionDetectionPolicy Optional. The data deletion detection policy for the datasource.
     * @param description Optional. Description of the datasource.
     * @return A new Azure Blob DataSource instance.
     */
    public static DataSource azureBlobStorage(
        String name,
        String storageConnectionString,
        String containerName,
        String pathPrefix,
        DataDeletionDetectionPolicy deletionDetectionPolicy,
        String description) {
        return new DataSource()
            .setName(name)
            .setType(DataSourceType.AZUREBLOB)
            .setCredentials(new DataSourceCredentials()
                .setConnectionString(storageConnectionString))
            .setContainer(new DataContainer()
                .setName(containerName)
                .setQuery(pathPrefix))
            .setDescription(description)
            .setDataDeletionDetectionPolicy(deletionDetectionPolicy);
    }

    /**
     * Creates a new DataSource to connect to a CosmosDb database.
     *
     * @param name The name of the datasource.
     * @param cosmosDbConnectionString The connection string for the CosmosDb database. It must follow this format:
     * AccountName|AccountEndpoint=[your account name or endpoint];
     * AccountKey=[your account key];Database=[your database name]"
     * @param collectionName The name of the collection from which to read documents.
     * @param query Optional. A query that is applied to the collection when reading documents.
     * @param useChangeDetection Optional. Indicates whether to use change detection when indexing. Default is true.
     * @param deletionDetectionPolicy Optional. The data deletion detection policy for the datasource.
     * @param description Optional. Description of the datasource.
     * @return A new DataSource instance.
     * @throws IllegalArgumentException if name, collectionName or cosmosDbConnectionString are null or empty.
     */
    public static DataSource cosmosDb(
        String name,
        String cosmosDbConnectionString,
        String collectionName,
        String query,
        Boolean useChangeDetection,
        DataDeletionDetectionPolicy deletionDetectionPolicy,
        String description) {
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("name cannot be null or empty");
        }
        if (StringUtils.isEmpty(collectionName)) {
            throw new IllegalArgumentException("collectionName cannot be null or empty");
        }
        if (StringUtils.isEmpty(cosmosDbConnectionString)) {
            throw new IllegalArgumentException("cosmosDbConnectionString cannot be null or empty");
        }
        return new DataSource()
            .setName(name)
            .setType(DataSourceType.COSMOSDB)
            .setCredentials(new DataSourceCredentials()
                .setConnectionString(cosmosDbConnectionString))
            .setContainer(new DataContainer()
                .setName(collectionName)
                .setQuery(query))
            .setDescription(description)
            .setDataDeletionDetectionPolicy(deletionDetectionPolicy)
            .setDataChangeDetectionPolicy(
                useChangeDetection
                    ? new HighWaterMarkChangeDetectionPolicy().setHighWaterMarkColumnName("_ts")
                    : null);
    }
}
