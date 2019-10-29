// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.common;

import com.azure.search.models.DataChangeDetectionPolicy;
import com.azure.search.models.DataContainer;
import com.azure.search.models.DataDeletionDetectionPolicy;
import com.azure.search.models.DataSource;
import com.azure.search.models.DataSourceCredentials;
import com.azure.search.models.DataSourceType;

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
    public static DataSource sqlDataSource(
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
}
