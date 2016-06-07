/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;


/**
 * Http logs configuration.
 */
public class HttpLogsConfig {
    /**
     * Http logs to file system configuration.
     */
    private FileSystemHttpLogsConfig fileSystem;

    /**
     * Http logs to azure blob storage configuration.
     */
    private AzureBlobStorageHttpLogsConfig azureBlobStorage;

    /**
     * Get the fileSystem value.
     *
     * @return the fileSystem value
     */
    public FileSystemHttpLogsConfig fileSystem() {
        return this.fileSystem;
    }

    /**
     * Set the fileSystem value.
     *
     * @param fileSystem the fileSystem value to set
     * @return the HttpLogsConfig object itself.
     */
    public HttpLogsConfig withFileSystem(FileSystemHttpLogsConfig fileSystem) {
        this.fileSystem = fileSystem;
        return this;
    }

    /**
     * Get the azureBlobStorage value.
     *
     * @return the azureBlobStorage value
     */
    public AzureBlobStorageHttpLogsConfig azureBlobStorage() {
        return this.azureBlobStorage;
    }

    /**
     * Set the azureBlobStorage value.
     *
     * @param azureBlobStorage the azureBlobStorage value to set
     * @return the HttpLogsConfig object itself.
     */
    public HttpLogsConfig withAzureBlobStorage(AzureBlobStorageHttpLogsConfig azureBlobStorage) {
        this.azureBlobStorage = azureBlobStorage;
        return this;
    }

}
