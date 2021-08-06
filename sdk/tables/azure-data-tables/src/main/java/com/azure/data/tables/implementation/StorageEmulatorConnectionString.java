// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables.implementation;

import com.azure.core.util.logging.ClientLogger;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Type to parse a connection string and creates a {@link StorageConnectionString} for emulator usage.
 */
final class StorageEmulatorConnectionString {
    /**
     * Try parsing the connection string and create {@link StorageConnectionString} from it.
     *
     * @param settings The {@link ConnectionSettings}.
     * @param logger The {@link ClientLogger}.
     *
     * @return The {@link StorageConnectionString} for emulator.
     *
     * @throws IllegalArgumentException If the {@link ConnectionSettings} are invalid for emulator usage.
     */
    static StorageConnectionString tryCreate(final ConnectionSettings settings, final ClientLogger logger) {
        if (ConnectionSettingsFilter.matchesSpecification(settings, requireUseEmulatorFlag(), optionalProxyUri())) {
            if (!parseUseEmulatorFlag(settings)) {
                throw logger.logExceptionAsError(
                    new IllegalArgumentException("Invalid connection string, the 'UseDevelopmentStorage' key must "
                        + "always have the value 'true'. Remove the flag entirely otherwise."));
            }

            String scheme;
            String host;

            if (settings.hasSetting(StorageConstants.ConnectionStringConstants.EMULATOR_STORAGE_PROXY_URI_NAME)) {
                try {
                    URI devStoreProxyUri = new URI(settings.getSettingValue(
                        StorageConstants.ConnectionStringConstants.EMULATOR_STORAGE_PROXY_URI_NAME));
                    scheme = devStoreProxyUri.getScheme();
                    host = devStoreProxyUri.getHost();
                } catch (URISyntaxException use) {
                    throw logger.logExceptionAsError(new RuntimeException(use));
                }
            } else {
                scheme = "http";
                host = "127.0.0.1";
            }

            StorageConnectionString storageConnectionString;

            try {
                URI blobPrimaryEndpoint =
                    new URI(String.format(StorageConstants.ConnectionStringConstants.EMULATOR_PRIMARY_ENDPOINT_FORMAT,
                        scheme,
                        host,
                        "10000"));
                URI queuePrimaryEndpoint =
                    new URI(String.format(StorageConstants.ConnectionStringConstants.EMULATOR_PRIMARY_ENDPOINT_FORMAT,
                        scheme,
                        host,
                        "10001"));
                URI tablePrimaryEndpoint =
                    new URI(String.format(StorageConstants.ConnectionStringConstants.EMULATOR_PRIMARY_ENDPOINT_FORMAT,
                        scheme,
                        host,
                        "10002"));
                URI blobSecondaryEndpoint =
                    new URI(String.format(StorageConstants.ConnectionStringConstants.EMULATOR_SECONDARY_ENDPOINT_FORMAT,
                        scheme,
                        host,
                        "10000"));
                URI queueSecondaryEndpoint =
                    new URI(String.format(StorageConstants.ConnectionStringConstants.EMULATOR_SECONDARY_ENDPOINT_FORMAT,
                        scheme,
                        host,
                        "10001"));
                URI tableSecondaryEndpoint =
                    new URI(String.format(StorageConstants.ConnectionStringConstants.EMULATOR_SECONDARY_ENDPOINT_FORMAT,
                        scheme,
                        host,
                        "10002"));

                storageConnectionString = new StorageConnectionString(
                    StorageAuthenticationSettings.forEmulator(),
                    new StorageEndpoint(blobPrimaryEndpoint, blobSecondaryEndpoint),
                    new StorageEndpoint(queuePrimaryEndpoint, queueSecondaryEndpoint),
                    new StorageEndpoint(tablePrimaryEndpoint, tableSecondaryEndpoint),
                    null,
                    null);
            } catch (URISyntaxException use) {
                throw logger.logExceptionAsError(new RuntimeException(use));
            }

            return storageConnectionString;
        } else {
            return null;
        }
    }

    private static ConnectionSettingsFilter requireUseEmulatorFlag() {
        return ConnectionSettingsFilter.allRequired(
            StorageConstants.ConnectionStringConstants.USE_EMULATOR_STORAGE_NAME);
    }

    private static ConnectionSettingsFilter optionalProxyUri() {
        return ConnectionSettingsFilter.optional(
            StorageConstants.ConnectionStringConstants.EMULATOR_STORAGE_PROXY_URI_NAME);
    }

    private static Boolean parseUseEmulatorFlag(final ConnectionSettings settings) {
        return Boolean.parseBoolean(settings.getSettingValue(
            StorageConstants.ConnectionStringConstants.USE_EMULATOR_STORAGE_NAME));
    }
}
