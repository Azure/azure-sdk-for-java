// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables.implementation;

import com.azure.core.util.logging.ClientLogger;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

/**
 * Type representing storage service (blob, queue, table, file) endpoint.
 */
public class StorageEndpoint {
    private final String primaryUri;
    private final String secondaryUri;

    /**
     * Creates a {@link StorageEndpoint} with a primary endpoint.
     *
     * @param primaryUri The primary endpoint {@link URI}.
     */
    public StorageEndpoint(URI primaryUri) {
        Objects.requireNonNull(primaryUri);

        this.primaryUri = primaryUri.toString();
        this.secondaryUri = null;
    }

    /**
     * Creates a {@link StorageEndpoint} with a primary and a secondary endpoint.
     *
     * @param primaryUri The primary endpoint {@link URI}.
     * @param secondaryUri The secondary endpoint {@link URI}.
     */
    public StorageEndpoint(URI primaryUri, URI secondaryUri) {
        Objects.requireNonNull(primaryUri);
        Objects.requireNonNull(secondaryUri);

        this.primaryUri = primaryUri.toString();
        this.secondaryUri = secondaryUri.toString();
    }

    /**
     * @return The primary endpoint {@link URI}.
     */
    public String getPrimaryUri() {
        return this.primaryUri;
    }

    /**
     * @return The secondary endpoint {@link URI}.
     */
    public String getSecondaryUri() {
        return this.secondaryUri;
    }

    /**
     * Creates a {@link StorageEndpoint} from the given connection settings.
     *
     * @param settings The {@link ConnectionSettings settings} derived from storage connection string.
     * @param service The storage service. Possible values are blob, queue, table and file.
     * @param serviceEndpointName The name of the entry in the settings representing a well formed primary URI to the
     * service. Possible values are BlobEndpoint, QueueEndpoint, FileEndpoint and TableEndpoint.
     * @param serviceSecondaryEndpointName The name of the entry in the settings representing a well formed secondary
     * URI to the service. Possible values are BlobSecondaryEndpoint, QueueSecondaryEndpoint, FileSecondaryEndpoint
     * and TableSecondaryEndpoint.
     * @param matchesAutomaticEndpointsSpec {@code true} indicates that the settings has entries from which
     * endpoint to the service can be build. Possible values are DefaultEndpointsProtocol, AccountName, AccountKey
     * and EndpointSuffix.
     * @param logger The {@link ClientLogger} to log any exception while processing the settings.
     *
     * @return A {@link StorageEndpoint} if the required settings exist, null otherwise.
     */
    public static StorageEndpoint fromStorageSettings(final ConnectionSettings settings, final String service,
                                                      final String serviceEndpointName,
                                                      final String serviceSecondaryEndpointName,
                                                      final Boolean matchesAutomaticEndpointsSpec,
                                                      final ClientLogger logger) {
        String serviceEndpoint = settings.getSettingValue(serviceEndpointName);
        String serviceSecondaryEndpoint = settings.getSettingValue(serviceSecondaryEndpointName);

        if (serviceEndpoint != null && serviceSecondaryEndpoint != null) {
            try {
                return new StorageEndpoint(new URI(serviceEndpoint), new URI(serviceSecondaryEndpoint));
            } catch (URISyntaxException use) {
                throw logger.logExceptionAsError(new RuntimeException(use));
            }
        }

        if (serviceEndpoint != null) {
            try {
                return new StorageEndpoint(new URI(serviceEndpoint));
            } catch (URISyntaxException use) {
                throw logger.logExceptionAsError(new RuntimeException(use));
            }
        }

        if (matchesAutomaticEndpointsSpec) {
            // Derive URI from relevant settings
            final String protocol =
                settings.getSettingValue(StorageConstants.ConnectionStringConstants.DEFAULT_ENDPOINTS_PROTOCOL_NAME);

            if (isNullOrEmpty(protocol)) {
                throw logger.logExceptionAsError(
                    new IllegalArgumentException("'DefaultEndpointsProtocol' is required, specify whether to use http"
                        + " or https."));
            }

            final String accountName =
                settings.getSettingValue(StorageConstants.ConnectionStringConstants.ACCOUNT_NAME);

            if (isNullOrEmpty(accountName)) {
                throw logger.logExceptionAsError(new IllegalArgumentException("'AccountName' is required."));
            }

            String endpointSuffix =
                settings.getSettingValue(StorageConstants.ConnectionStringConstants.ENDPOINT_SUFFIX_NAME);

            if (endpointSuffix == null) {
                // default: core.windows.net
                endpointSuffix = StorageConstants.ConnectionStringConstants.DEFAULT_DNS;
            }

            final URI primaryUri;
            final URI secondaryUri;

            try {
                primaryUri = new URI(String.format("%s://%s.%s.%s", protocol, accountName, service, endpointSuffix));
            } catch (URISyntaxException use) {
                throw logger.logExceptionAsError(new RuntimeException(use));
            }

            try {
                secondaryUri = new URI(String.format("%s://%s-secondary.%s.%s", protocol, accountName, service,
                    endpointSuffix));
            } catch (URISyntaxException use) {
                throw logger.logExceptionAsError(new RuntimeException(use));
            }

            return new StorageEndpoint(primaryUri, secondaryUri);
        }

        return null;
    }

    private static boolean isNullOrEmpty(final String value) {
        return value == null || value.length() == 0;
    }
}
