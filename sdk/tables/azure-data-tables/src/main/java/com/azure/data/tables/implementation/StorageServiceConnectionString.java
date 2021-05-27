// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables.implementation;

import com.azure.core.util.logging.ClientLogger;

/**
 * Type to parse a connection string and create a {@link StorageConnectionString} for the storage service.
 */
final class StorageServiceConnectionString {
    /**
     * Try parsing the connection string and create a {@link StorageConnectionString} from it.
     *
     * @param settings The {@link ConnectionSettings}.
     * @param logger The {@link ClientLogger}
     *
     * @return A {@link StorageConnectionString}.
     *
     * @throws IllegalArgumentException If settings is invalid for the storage service.
     */
    static StorageConnectionString tryCreate(final ConnectionSettings settings, final ClientLogger logger) {
        ConnectionSettingsFilter automaticEndpointsMatchSpec = automaticEndpointsMatchSpec();
        ConnectionSettingsFilter explicitEndpointsMatchSpec = explicitEndpointsMatchSpec();

        boolean matchesAutomaticEndpointsSpec =
            ConnectionSettingsFilter.matchesSpecification(settings, automaticEndpointsMatchSpec);
        boolean matchesExplicitEndpointsSpec =
            ConnectionSettingsFilter.matchesSpecification(settings, explicitEndpointsMatchSpec);

        if (matchesAutomaticEndpointsSpec || matchesExplicitEndpointsSpec) {
            if (matchesAutomaticEndpointsSpec
                && !settings.hasSetting(StorageConstants.ConnectionStringConstants.DEFAULT_ENDPOINTS_PROTOCOL_NAME)) {

                settings.setSetting(
                    StorageConstants.ConnectionStringConstants.DEFAULT_ENDPOINTS_PROTOCOL_NAME, "https");
            }

            // If the settings BlobEndpoint, FileEndpoint, QueueEndpoint, TableEndpoint presents it will be a well
            // formed Uri, including 'http' or 'https' prefix.
            String blobEndpoint =
                settings.getSettingValue(StorageConstants.ConnectionStringConstants.BLOB_ENDPOINT_NAME);
            String queueEndpoint =
                settings.getSettingValue(StorageConstants.ConnectionStringConstants.QUEUE_ENDPOINT_NAME);
            String tableEndpoint =
                settings.getSettingValue(StorageConstants.ConnectionStringConstants.TABLE_ENDPOINT_NAME);
            String fileEndpoint =
                settings.getSettingValue(StorageConstants.ConnectionStringConstants.FILE_ENDPOINT_NAME);
            String blobSecondaryEndpoint =
                settings.getSettingValue(StorageConstants.ConnectionStringConstants.BLOB_SECONDARY_ENDPOINT_NAME);
            String queueSecondaryEndpoint =
                settings.getSettingValue(StorageConstants.ConnectionStringConstants.QUEUE_SECONDARY_ENDPOINT_NAME);
            String tableSecondaryEndpoint =
                settings.getSettingValue(StorageConstants.ConnectionStringConstants.TABLE_SECONDARY_ENDPOINT_NAME);
            String fileSecondaryEndpoint =
                settings.getSettingValue(StorageConstants.ConnectionStringConstants.FILE_SECONDARY_ENDPOINT_NAME);

            if (isValidPrimarySecondaryPair(blobEndpoint, blobSecondaryEndpoint)
                && isValidPrimarySecondaryPair(queueEndpoint, queueSecondaryEndpoint)
                && isValidPrimarySecondaryPair(tableEndpoint, tableSecondaryEndpoint)
                && isValidPrimarySecondaryPair(fileEndpoint, fileSecondaryEndpoint)) {

                return new StorageConnectionString(
                    StorageAuthenticationSettings.fromConnectionSettings(settings),
                    StorageEndpoint.fromStorageSettings(settings, "blob",
                        StorageConstants.ConnectionStringConstants.BLOB_ENDPOINT_NAME,
                        StorageConstants.ConnectionStringConstants.BLOB_SECONDARY_ENDPOINT_NAME,
                        matchesAutomaticEndpointsSpec, logger),
                    StorageEndpoint.fromStorageSettings(settings, "queue",
                        StorageConstants.ConnectionStringConstants.QUEUE_ENDPOINT_NAME,
                        StorageConstants.ConnectionStringConstants.QUEUE_SECONDARY_ENDPOINT_NAME,
                        matchesAutomaticEndpointsSpec, logger),
                    StorageEndpoint.fromStorageSettings(settings, "table",
                        StorageConstants.ConnectionStringConstants.TABLE_ENDPOINT_NAME,
                        StorageConstants.ConnectionStringConstants.TABLE_SECONDARY_ENDPOINT_NAME,
                        matchesAutomaticEndpointsSpec, logger),
                    StorageEndpoint.fromStorageSettings(settings, "file",
                        StorageConstants.ConnectionStringConstants.FILE_ENDPOINT_NAME,
                        StorageConstants.ConnectionStringConstants.FILE_SECONDARY_ENDPOINT_NAME,
                        matchesAutomaticEndpointsSpec, logger),
                    settings.getSettingValue(StorageConstants.ConnectionStringConstants.ACCOUNT_NAME));
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
        ConnectionSettingsFilter validCredentials =
            ConnectionSettingsFilter.matchesOne(
                requireAccountNameAndKeyNoSas(),
                requireSasOptionalAccountNameNoAccountKey(),
                noAccountNameNoAccountKeyNoSas());

        return ConnectionSettingsFilter.matchesExactly(
            ConnectionSettingsFilter.matchesAll(validCredentials,
                requireAtLeastOnePrimaryEndpoint(),
                optionalSecondaryEndpoints()));
    }

    private static ConnectionSettingsFilter requireAccountName() {
        return ConnectionSettingsFilter.allRequired(StorageConstants.ConnectionStringConstants.ACCOUNT_NAME);
    }

    private static ConnectionSettingsFilter requireAccountKey() {
        return ConnectionSettingsFilter.allRequired(StorageConstants.ConnectionStringConstants.ACCOUNT_KEY_NAME);
    }

    private static ConnectionSettingsFilter requireSas() {
        return ConnectionSettingsFilter.allRequired(
            StorageConstants.ConnectionStringConstants.SHARED_ACCESS_SIGNATURE_NAME);
    }

    private static ConnectionSettingsFilter requireAccountNameAndKey() {
        return ConnectionSettingsFilter.allRequired(StorageConstants.ConnectionStringConstants.ACCOUNT_NAME,
            StorageConstants.ConnectionStringConstants.ACCOUNT_KEY_NAME);
    }

    private static ConnectionSettingsFilter optionalAccountName() {
        return ConnectionSettingsFilter.optional(StorageConstants.ConnectionStringConstants.ACCOUNT_NAME);
    }

    private static ConnectionSettingsFilter noAccountKey() {
        return ConnectionSettingsFilter.none(StorageConstants.ConnectionStringConstants.ACCOUNT_KEY_NAME);
    }

    private static ConnectionSettingsFilter noSas() {
        return ConnectionSettingsFilter.none(StorageConstants.ConnectionStringConstants.SHARED_ACCESS_SIGNATURE_NAME);
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
        return ConnectionSettingsFilter.none(
            StorageConstants.ConnectionStringConstants.ACCOUNT_NAME,
            StorageConstants.ConnectionStringConstants.ACCOUNT_KEY_NAME,
            StorageConstants.ConnectionStringConstants.SHARED_ACCESS_SIGNATURE_NAME);
    }

    private static ConnectionSettingsFilter optionalEndpointProtocolAndSuffix() {
        return ConnectionSettingsFilter.optional(
            StorageConstants.ConnectionStringConstants.DEFAULT_ENDPOINTS_PROTOCOL_NAME,
            StorageConstants.ConnectionStringConstants.ENDPOINT_SUFFIX_NAME);
    }

    private static ConnectionSettingsFilter optionalEndpoints() {
        return ConnectionSettingsFilter.optional(
            StorageConstants.ConnectionStringConstants.BLOB_ENDPOINT_NAME,
            StorageConstants.ConnectionStringConstants.BLOB_SECONDARY_ENDPOINT_NAME,
            StorageConstants.ConnectionStringConstants.QUEUE_ENDPOINT_NAME,
            StorageConstants.ConnectionStringConstants.QUEUE_SECONDARY_ENDPOINT_NAME,
            StorageConstants.ConnectionStringConstants.TABLE_ENDPOINT_NAME,
            StorageConstants.ConnectionStringConstants.TABLE_SECONDARY_ENDPOINT_NAME,
            StorageConstants.ConnectionStringConstants.FILE_ENDPOINT_NAME,
            StorageConstants.ConnectionStringConstants.FILE_SECONDARY_ENDPOINT_NAME);
    }

    private static ConnectionSettingsFilter requireAtLeastOnePrimaryEndpoint() {
        return ConnectionSettingsFilter.atLeastOne(
            StorageConstants.ConnectionStringConstants.BLOB_ENDPOINT_NAME,
            StorageConstants.ConnectionStringConstants.QUEUE_ENDPOINT_NAME,
            StorageConstants.ConnectionStringConstants.TABLE_ENDPOINT_NAME,
            StorageConstants.ConnectionStringConstants.FILE_ENDPOINT_NAME);
    }

    private static ConnectionSettingsFilter optionalSecondaryEndpoints() {
        return ConnectionSettingsFilter.optional(
            StorageConstants.ConnectionStringConstants.BLOB_SECONDARY_ENDPOINT_NAME,
            StorageConstants.ConnectionStringConstants.QUEUE_SECONDARY_ENDPOINT_NAME,
            StorageConstants.ConnectionStringConstants.TABLE_SECONDARY_ENDPOINT_NAME,
            StorageConstants.ConnectionStringConstants.FILE_SECONDARY_ENDPOINT_NAME);
    }

    private static Boolean isValidPrimarySecondaryPair(String primary, String secondary) {
        if (primary != null) {
            return true;
        }

        return secondary == null;
    }
}
