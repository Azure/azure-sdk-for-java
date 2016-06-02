/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;


/**
 * Application logs configuration.
 */
public class ApplicationLogsConfig {
    /**
     * Application logs to file system configuration.
     */
    private FileSystemApplicationLogsConfig fileSystem;

    /**
     * Application logs to azure table storage configuration.
     */
    private AzureTableStorageApplicationLogsConfig azureTableStorage;

    /**
     * Application logs to blob storage configuration.
     */
    private AzureBlobStorageApplicationLogsConfig azureBlobStorage;

    /**
     * Get the fileSystem value.
     *
     * @return the fileSystem value
     */
    public FileSystemApplicationLogsConfig fileSystem() {
        return this.fileSystem;
    }

    /**
     * Set the fileSystem value.
     *
     * @param fileSystem the fileSystem value to set
     * @return the ApplicationLogsConfig object itself.
     */
    public ApplicationLogsConfig withFileSystem(FileSystemApplicationLogsConfig fileSystem) {
        this.fileSystem = fileSystem;
        return this;
    }

    /**
     * Get the azureTableStorage value.
     *
     * @return the azureTableStorage value
     */
    public AzureTableStorageApplicationLogsConfig azureTableStorage() {
        return this.azureTableStorage;
    }

    /**
     * Set the azureTableStorage value.
     *
     * @param azureTableStorage the azureTableStorage value to set
     * @return the ApplicationLogsConfig object itself.
     */
    public ApplicationLogsConfig withAzureTableStorage(AzureTableStorageApplicationLogsConfig azureTableStorage) {
        this.azureTableStorage = azureTableStorage;
        return this;
    }

    /**
     * Get the azureBlobStorage value.
     *
     * @return the azureBlobStorage value
     */
    public AzureBlobStorageApplicationLogsConfig azureBlobStorage() {
        return this.azureBlobStorage;
    }

    /**
     * Set the azureBlobStorage value.
     *
     * @param azureBlobStorage the azureBlobStorage value to set
     * @return the ApplicationLogsConfig object itself.
     */
    public ApplicationLogsConfig withAzureBlobStorage(AzureBlobStorageApplicationLogsConfig azureBlobStorage) {
        this.azureBlobStorage = azureBlobStorage;
        return this;
    }

}
