// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables.implementation;

import com.azure.core.util.logging.ClientLogger;

/**
 * Type representing a storage connection string.
 */
public final class StorageConnectionString {
    /**
     * The storage account name.
     */
    private final String accountName;

    /**
     * The settings for storage authentication.
     */
    private final StorageAuthenticationSettings storageAuthSettings;

    /**
     * The blob endpoint.
     */
    private final StorageEndpoint blobEndpoint;

    /**
     * The file endpoint.
     */
    private final StorageEndpoint fileEndpoint;

    /**
     * The queue endpoint.
     */
    private final StorageEndpoint queueEndpoint;

    /**
     * The table endpoint.
     */
    private final StorageEndpoint tableEndpoint;

    /**
     * @return The storage account name.
     */
    public String getAccountName() {
        return this.accountName;
    }

    /**
     * @return The {@link StorageAuthenticationSettings} associated with this connection string.
     */
    public StorageAuthenticationSettings getStorageAuthSettings() {
        return this.storageAuthSettings;
    }

    /**
     * Get the {@link StorageEndpoint endpoint} for the storage blob service.
     *
     * @return The {@link StorageEndpoint blob endpoint} associated with this connection string.
     */
    public StorageEndpoint getBlobEndpoint() {
        return this.blobEndpoint;
    }

    /**
     * Get the {@link StorageEndpoint endpoint} for the storage file service.
     *
     * @return The {@link StorageEndpoint file endpoint} associated with this connection string.
     */
    public StorageEndpoint getFileEndpoint() {
        return this.fileEndpoint;
    }

    /**
     * Get the {@link StorageEndpoint endpoint} for the storage queue service.
     *
     * @return The {@link StorageEndpoint queue endpoint} associated with this connection string.
     */
    public StorageEndpoint getQueueEndpoint() {
        return this.queueEndpoint;
    }

    /**
     * Get the {@link StorageEndpoint endpoint} for the storage table service.
     *
     * @return The {@link StorageEndpoint table endpoint} associated with this connection string.
     */
    public StorageEndpoint getTableEndpoint() {
        return this.tableEndpoint;
    }

    /**
     * Create a {@link StorageConnectionString} from the given connection string.
     *
     * @param connectionString The connection string.
     * @param logger The {@link ClientLogger}.
     *
     * @return A {@link StorageConnectionString} based on the provided connection string.
     */
    public static StorageConnectionString create(final String connectionString, final ClientLogger logger) {
        if (connectionString == null || connectionString.length() == 0) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Invalid connection string."));
        }

        ConnectionSettings settings = ConnectionSettings.fromConnectionString(connectionString, logger);
        StorageConnectionString emulatorConnString = StorageEmulatorConnectionString.tryCreate(settings, logger);

        if (emulatorConnString != null) {
            return emulatorConnString;
        }

        StorageConnectionString serviceConnString = StorageServiceConnectionString.tryCreate(settings, logger);

        if (serviceConnString != null) {
            return serviceConnString;
        }

        throw logger.logExceptionAsError(new IllegalArgumentException("Invalid connection string."));
    }

    /**
     * Creates a {@link StorageConnectionString}.
     *
     * @param storageAuthSettings The storage authentication settings.
     * @param blobEndpoint The blob service endpoint.
     * @param queueEndpoint The queue service endpoint.
     * @param tableEndpoint The table service endpoint.
     * @param fileEndpoint The file service endpoint.
     * @param accountName The storage account name.
     */
    StorageConnectionString(final StorageAuthenticationSettings storageAuthSettings, final StorageEndpoint blobEndpoint,
                            final StorageEndpoint queueEndpoint, final StorageEndpoint tableEndpoint,
                            final StorageEndpoint fileEndpoint, final String accountName) {
        this.storageAuthSettings = storageAuthSettings;
        this.blobEndpoint = blobEndpoint;
        this.fileEndpoint = fileEndpoint;
        this.queueEndpoint = queueEndpoint;
        this.tableEndpoint = tableEndpoint;
        this.accountName = accountName;
    }
}
