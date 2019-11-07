// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation.connectionstring;

import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.implementation.Constants;

/**
 * Type to parse a connection string and creates {@link StorageConnectionString} for storage service.
 */
final class StorageServiceConnectionString {
    /**
     * Try parsing the connection string and create {@link StorageConnectionString}
     * from it.
     *
     * @param settings the connection settings
     * @param logger the logger
     * @return the StorageConnectionString for emulator
     * @throws IllegalArgumentException If settings is invalid for storage service.
     */
    static StorageConnectionString tryCreate(final ConnectionSettings settings,
                                             final ClientLogger logger) {
        ConnectionSettingsFilter automaticEndpointsMatchSpec = automaticEndpointsMatchSpec();
        ConnectionSettingsFilter explicitEndpointsMatchSpec = explicitEndpointsMatchSpec();

        boolean matchesAutomaticEndpointsSpec = ConnectionSettingsFilter.matchesSpecification(settings,
                automaticEndpointsMatchSpec);
        boolean matchesExplicitEndpointsSpec = ConnectionSettingsFilter.matchesSpecification(settings,
                explicitEndpointsMatchSpec);

        if (matchesAutomaticEndpointsSpec || matchesExplicitEndpointsSpec) {
            if (matchesAutomaticEndpointsSpec
                    && !settings.hasSetting(Constants.ConnectionStringConstants.DEFAULT_ENDPOINTS_PROTOCOL_NAME)) {
                settings.setSetting(Constants.ConnectionStringConstants.DEFAULT_ENDPOINTS_PROTOCOL_NAME, "https");
            }
            // If the settings BlobEndpoint, FileEndpoint, QueueEndpoint, TableEndpoint presents
            // it will be a well formed Uri, including 'http' or 'https' prefix.
            String blobEndpoint =
                    settings.getSettingValue(Constants.ConnectionStringConstants.BLOB_ENDPOINT_NAME);
            String queueEndpoint =
                    settings.getSettingValue(Constants.ConnectionStringConstants.QUEUE_ENDPOINT_NAME);
            String tableEndpoint =
                    settings.getSettingValue(Constants.ConnectionStringConstants.TABLE_ENDPOINT_NAME);
            String fileEndpoint =
                    settings.getSettingValue(Constants.ConnectionStringConstants.FILE_ENDPOINT_NAME);
            String blobSecondaryEndpoint =
                    settings.getSettingValue(Constants.ConnectionStringConstants.BLOB_SECONDARY_ENDPOINT_NAME);
            String queueSecondaryEndpoint =
                    settings.getSettingValue(Constants.ConnectionStringConstants.QUEUE_SECONDARY_ENDPOINT_NAME);
            String tableSecondaryEndpoint =
                    settings.getSettingValue(Constants.ConnectionStringConstants.TABLE_SECONDARY_ENDPOINT_NAME);
            String fileSecondaryEndpoint =
                    settings.getSettingValue(Constants.ConnectionStringConstants.FILE_SECONDARY_ENDPOINT_NAME);

            if (isValidPrimarySecondaryPair(blobEndpoint, blobSecondaryEndpoint)
                    && isValidPrimarySecondaryPair(queueEndpoint, queueSecondaryEndpoint)
                    && isValidPrimarySecondaryPair(tableEndpoint, tableSecondaryEndpoint)
                    && isValidPrimarySecondaryPair(fileEndpoint, fileSecondaryEndpoint)) {
                return new StorageConnectionString(
                        StorageAuthenticationSettings.fromConnectionSettings(settings),
                        StorageEndpoint.fromStorageSettings(settings,
                                "blob",
                                Constants.ConnectionStringConstants.BLOB_ENDPOINT_NAME,
                                Constants.ConnectionStringConstants.BLOB_SECONDARY_ENDPOINT_NAME,
                                matchesAutomaticEndpointsSpec,
                                logger),
                        StorageEndpoint.fromStorageSettings(settings,
                                "queue",
                                Constants.ConnectionStringConstants.QUEUE_ENDPOINT_NAME,
                                Constants.ConnectionStringConstants.QUEUE_SECONDARY_ENDPOINT_NAME,
                                matchesAutomaticEndpointsSpec,
                                logger),
                        StorageEndpoint.fromStorageSettings(settings,
                                "table",
                                Constants.ConnectionStringConstants.TABLE_ENDPOINT_NAME,
                                Constants.ConnectionStringConstants.TABLE_SECONDARY_ENDPOINT_NAME,
                                matchesAutomaticEndpointsSpec,
                                logger),
                        StorageEndpoint.fromStorageSettings(settings,
                                "file",
                                Constants.ConnectionStringConstants.FILE_ENDPOINT_NAME,
                                Constants.ConnectionStringConstants.FILE_SECONDARY_ENDPOINT_NAME,
                                matchesAutomaticEndpointsSpec,
                                logger),
                        settings.getSettingValue(Constants.ConnectionStringConstants.ACCOUNT_NAME));
            }
        }
        return null;
    }

    private static ConnectionSettingsFilter automaticEndpointsMatchSpec() {
        return ConnectionSettingsFilter.matchesExactly(
                ConnectionSettingsFilter.matchesAll(
                        ConnectionSettingsFilter.matchesOne(
                                ConnectionSettingsFilter.matchesAll(requireAccountKey()),
                                requireSas()),
                        requireAccountName(),
                        optionalEndpoints(),
                        optionalEndpointProtocolAndSuffix())
        );
    }

    private static ConnectionSettingsFilter explicitEndpointsMatchSpec() {
        ConnectionSettingsFilter validCredentials = ConnectionSettingsFilter.matchesOne(
                requireAccountNameAndKeyNoSas(),
                requireSasOptionalAccountNameNoAccountKey(),
                noAccountNameNoAccountKeyNoSas());

        return ConnectionSettingsFilter.matchesExactly(ConnectionSettingsFilter.matchesAll(validCredentials,
                requireAtLeastOnePrimaryEndpoint(),
                optionalSecondaryEndpoints()));
    }

    private static ConnectionSettingsFilter requireAccountName() {
        return ConnectionSettingsFilter.allRequired(Constants.ConnectionStringConstants.ACCOUNT_NAME);
    }

    private static ConnectionSettingsFilter requireAccountKey() {
        return ConnectionSettingsFilter.allRequired(Constants.ConnectionStringConstants.ACCOUNT_KEY_NAME);
    }

    private static ConnectionSettingsFilter requireSas() {
        return ConnectionSettingsFilter.allRequired(Constants.ConnectionStringConstants.SHARED_ACCESS_SIGNATURE_NAME);
    }

    private static ConnectionSettingsFilter requireAccountNameAndKey() {
        return ConnectionSettingsFilter.allRequired(Constants.ConnectionStringConstants.ACCOUNT_NAME,
                Constants.ConnectionStringConstants.ACCOUNT_KEY_NAME);
    }

    private static ConnectionSettingsFilter optionalAccountName() {
        return ConnectionSettingsFilter.optional(Constants.ConnectionStringConstants.ACCOUNT_NAME);
    }

    private static ConnectionSettingsFilter noAccountKey() {
        return ConnectionSettingsFilter.none(Constants.ConnectionStringConstants.ACCOUNT_KEY_NAME);
    }

    private static ConnectionSettingsFilter noSas() {
        return ConnectionSettingsFilter.none(Constants.ConnectionStringConstants.SHARED_ACCESS_SIGNATURE_NAME);
    }

    private static ConnectionSettingsFilter requireAccountNameAndKeyNoSas() {
        return ConnectionSettingsFilter.matchesAll(
                requireAccountNameAndKey(),
                noSas());
    }

    private static ConnectionSettingsFilter requireSasOptionalAccountNameNoAccountKey() {
        return ConnectionSettingsFilter.matchesAll(
                requireSas(),
                optionalAccountName(),
                noAccountKey());
    }

    private static ConnectionSettingsFilter noAccountNameNoAccountKeyNoSas() {
        return ConnectionSettingsFilter.none(Constants.ConnectionStringConstants.ACCOUNT_NAME,
                Constants.ConnectionStringConstants.ACCOUNT_KEY_NAME,
                Constants.ConnectionStringConstants.SHARED_ACCESS_SIGNATURE_NAME);
    }

    private static ConnectionSettingsFilter optionalEndpointProtocolAndSuffix() {
        return ConnectionSettingsFilter.optional(Constants.ConnectionStringConstants.DEFAULT_ENDPOINTS_PROTOCOL_NAME,
                Constants.ConnectionStringConstants.ENDPOINT_SUFFIX_NAME);
    }

    private static ConnectionSettingsFilter optionalEndpoints() {
        return ConnectionSettingsFilter.optional(
                Constants.ConnectionStringConstants.BLOB_ENDPOINT_NAME,
                Constants.ConnectionStringConstants.BLOB_SECONDARY_ENDPOINT_NAME,
                Constants.ConnectionStringConstants.QUEUE_ENDPOINT_NAME,
                Constants.ConnectionStringConstants.QUEUE_SECONDARY_ENDPOINT_NAME,
                Constants.ConnectionStringConstants.TABLE_ENDPOINT_NAME,
                Constants.ConnectionStringConstants.TABLE_SECONDARY_ENDPOINT_NAME,
                Constants.ConnectionStringConstants.FILE_ENDPOINT_NAME,
                Constants.ConnectionStringConstants.FILE_SECONDARY_ENDPOINT_NAME);
    }

    private static ConnectionSettingsFilter requireAtLeastOnePrimaryEndpoint() {
        return ConnectionSettingsFilter.atLeastOne(Constants.ConnectionStringConstants.BLOB_ENDPOINT_NAME,
                Constants.ConnectionStringConstants.QUEUE_ENDPOINT_NAME,
                Constants.ConnectionStringConstants.TABLE_ENDPOINT_NAME,
                Constants.ConnectionStringConstants.FILE_ENDPOINT_NAME);
    }

    private static ConnectionSettingsFilter optionalSecondaryEndpoints() {
        return ConnectionSettingsFilter.optional(Constants.ConnectionStringConstants.BLOB_SECONDARY_ENDPOINT_NAME,
                Constants.ConnectionStringConstants.QUEUE_SECONDARY_ENDPOINT_NAME,
                Constants.ConnectionStringConstants.TABLE_SECONDARY_ENDPOINT_NAME,
                Constants.ConnectionStringConstants.FILE_SECONDARY_ENDPOINT_NAME);
    }

    private static Boolean isValidPrimarySecondaryPair(String primary, String secondary) {
        if (primary != null) {
            return true;
        }

        return secondary == null;
    }
}
