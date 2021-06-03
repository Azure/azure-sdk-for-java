// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration.models;

import com.azure.ai.metricsadvisor.implementation.util.AzureDataExplorerDataFeedSourceAccessor;
import com.azure.core.annotation.Immutable;

/**
 * The AzureDataExplorerDataFeedSource model.
 */
@Immutable
public final class AzureDataExplorerDataFeedSource extends DataFeedSource {
    /*
     * Database connection string
     */
    private final String connectionString;

    /*
     * Query script
     */
    private final String query;

    /*
     * The id of the credential resource to authenticate the data source.
     */
    private final String credentialId;

    /*
     * The authentication type to access the data source.
     */
    private final DatasourceAuthenticationType authType;

    static {
        AzureDataExplorerDataFeedSourceAccessor.setAccessor(
            new AzureDataExplorerDataFeedSourceAccessor.Accessor() {
                @Override
                public String getConnectionString(AzureDataExplorerDataFeedSource feedSource) {
                    return feedSource.getConnectionString();
                }
            });
    }

    private AzureDataExplorerDataFeedSource(final String connectionString,
                                    final String query,
                                    final String credentialId,
                                    final DatasourceAuthenticationType authType) {
        this.connectionString = connectionString;
        this.query = query;
        this.credentialId = credentialId;
        this.authType = authType;
    }

    /**
     * Create a AzureDataExplorerDataFeedSource with credential included in the {@code connectionString} as plain text.
     *
     * @param connectionString The connection string.
     * @param query The query that retrieves the values to be analyzed for anomalies.
     *
     * @return The AzureDataExplorerDataFeedSource.
     */
    public static AzureDataExplorerDataFeedSource fromBasicCredential(final String connectionString,
                                                                      final String query) {
        return new AzureDataExplorerDataFeedSource(connectionString, query, null, DatasourceAuthenticationType.BASIC);
    }

    /**
     * Create a AzureDataExplorerDataFeedSource with the {@code connectionString} containing the resource
     * id of the SQL server on which metrics advisor has MSI access.
     *
     * @param connectionString The connection string.
     * @param query The query that retrieves the values to be analyzed for anomalies.
     *
     * @return The AzureDataExplorerDataFeedSource.
     */
    public static AzureDataExplorerDataFeedSource fromManagedIdentityCredential(final String connectionString,
                                                                                final String query) {
        return new AzureDataExplorerDataFeedSource(connectionString,
            query,
            null,
            DatasourceAuthenticationType.MANAGED_IDENTITY);
    }

    /**
     * Create a AzureDataExplorerDataFeedSource with the {@code credentialId} identifying a credential
     * entity of type {@link DatasourceServicePrincipal}, the entity contains
     * Service Principal to access the SQL Server.
     *
     * @param connectionString The connection string.
     * @param query The query that retrieves the values to be analyzed for anomalies.
     * @param credentialId The unique id of a credential entity of type
     * {@link DatasourceServicePrincipal}.
     *
     * @return The SQLServerDataFeedSource.
     */
    public static AzureDataExplorerDataFeedSource fromServicePrincipalCredential(final String connectionString,
                                                                                 final String query,
                                                                                 final String credentialId) {
        return new AzureDataExplorerDataFeedSource(connectionString,
            query,
            credentialId,
            DatasourceAuthenticationType.SERVICE_PRINCIPAL);
    }

    /**
     * Create a AzureDataExplorerDataFeedSource with the {@code credentialId} identifying a credential
     * entity of type {@link DatasourceServicePrincipalInKeyVault}, the entity contains
     * details of the KeyVault holding the Service Principal to access the SQL Server.
     *
     * @param connectionString The connection string.
     * @param query The query that retrieves the values to be analyzed for anomalies.
     * @param credentialId The unique id of a credential entity of type
     * {@link DatasourceServicePrincipalInKeyVault}.
     *
     * @return The AzureDataExplorerDataFeedSource.
     */
    public static AzureDataExplorerDataFeedSource fromServicePrincipalInKeyVaultCredential(
        final String connectionString,
        final String query,
        final String credentialId) {
        return new AzureDataExplorerDataFeedSource(connectionString,
            query,
            credentialId,
            DatasourceAuthenticationType.SERVICE_PRINCIPAL_IN_KV);
    }

    /**
     * Get the query property: Query script.
     *
     * @return the query value.
     */
    public String getQuery() {
        return this.query;
    }

    /**
     * Gets the id of the {@link DatasourceCredentialEntity credential resource} to authenticate the data source.
     *
     * @return The credential resource id.
     */
    public String getCredentialId() {
        return this.credentialId;
    }

    /**
     * Gets the authentication type to access the data source.
     *
     * @return The authentication type.
     */
    public DatasourceAuthenticationType getAuthenticationType() {
        return this.authType;
    }

    private String getConnectionString() {
        return this.connectionString;
    }
}
