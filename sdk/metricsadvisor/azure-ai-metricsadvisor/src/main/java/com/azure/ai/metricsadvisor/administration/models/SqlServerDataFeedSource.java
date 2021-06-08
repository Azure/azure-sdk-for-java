// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration.models;

import com.azure.ai.metricsadvisor.implementation.util.SqlServerDataFeedSourceAccessor;
import com.azure.core.annotation.Immutable;

/**
 * The SQLServerDataFeedSource model.
 */
@Immutable
public final class SqlServerDataFeedSource extends DataFeedSource {
    /*
     * SQL Server database connection string
     */
    private final String connectionString;

    /*
     * Get the query that retrieves the values to be analyzed for anomalies.
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
        SqlServerDataFeedSourceAccessor.setAccessor(
            new SqlServerDataFeedSourceAccessor.Accessor() {
                @Override
                public String getConnectionString(SqlServerDataFeedSource feedSource) {
                    return feedSource.getConnectionString();
                }
            });
    }

    private SqlServerDataFeedSource(final String connectionString,
                                    final String query,
                                    final String credentialId,
                                    final DatasourceAuthenticationType authType) {
        this.connectionString = connectionString;
        this.query = query;
        this.credentialId = credentialId;
        this.authType = authType;
    }

    /**
     * Create a SQLServerDataFeedSource with credential included in the {@code connectionString} as plain text.
     *
     * @param connectionString The SQL server connection string.
     * @param query The query that retrieves the values to be analyzed for anomalies.
     *
     * @return The SQLServerDataFeedSource.
     */
    public static SqlServerDataFeedSource fromBasicCredential(final String connectionString,
                                                              final String query) {
        return new SqlServerDataFeedSource(connectionString, query, null, DatasourceAuthenticationType.BASIC);
    }

    /**
     * Create a SQLServerDataFeedSource with the {@code connectionString} containing the resource
     * id of the SQL server on which metrics advisor has MSI access.
     *
     * @param connectionString The SQL server connection string.
     * @param query The query that retrieves the values to be analyzed for anomalies.
     * @return The SQLServerDataFeedSource.
     */
    public static SqlServerDataFeedSource fromManagedIdentityCredential(final String connectionString,
                                                                        final String query) {
        return new SqlServerDataFeedSource(connectionString,
            query,
            null,
            DatasourceAuthenticationType.MANAGED_IDENTITY);
    }

    /**
     * Create a SQLServerDataFeedSource with the {@code credentialId} identifying a credential
     * entity of type {@link DatasourceSqlServerConnectionString} that contains the SQL
     * connection string.
     *
     * @param query The query that retrieves the values to be analyzed for anomalies.
     * @param credentialId The unique id of a credential entity of type
     * {@link DatasourceSqlServerConnectionString}.
     * @return The SQLServerDataFeedSource.
     */
    public static SqlServerDataFeedSource fromConnectionStringCredential(final String query,
                                                                         final String credentialId) {
        return new SqlServerDataFeedSource(null,
            query,
            credentialId,
            DatasourceAuthenticationType.AZURE_SQL_CONNECTION_STRING);
    }

    /**
     * Create a SQLServerDataFeedSource with the {@code credentialId} identifying a credential
     * entity of type {@link DatasourceServicePrincipal}, the entity contains
     * Service Principal to access the SQL Server.
     *
     * @param connectionString The SQL server connection string.
     * @param query The query that retrieves the values to be analyzed for anomalies.
     * @param credentialId The unique id of a credential entity of type
     * {@link DatasourceServicePrincipal}.
     *
     * @return The SQLServerDataFeedSource.
     */
    public static SqlServerDataFeedSource fromServicePrincipalCredential(final String connectionString,
                                                                         final String query,
                                                                         final String credentialId) {
        return new SqlServerDataFeedSource(connectionString,
            query,
            credentialId,
            DatasourceAuthenticationType.SERVICE_PRINCIPAL);
    }

    /**
     * Create a SQLServerDataFeedSource with the {@code credentialId} identifying a credential
     * entity of type {@link DatasourceServicePrincipalInKeyVault}, the entity contains
     * details of the KeyVault holding the Service Principal to access the SQL Server.
     *
     * @param connectionString The SQL server connection string.
     * @param query The query that retrieves the values to be analyzed for anomalies.
     * @param credentialId The unique id of a credential entity of type
     * {@link DatasourceServicePrincipalInKeyVault}.
     *
     * @return The SQLServerDataFeedSource.
     */
    public static SqlServerDataFeedSource fromServicePrincipalInKeyVaultCredential(final String connectionString,
                                                                                   final String query,
                                                                                   final String credentialId) {
        return new SqlServerDataFeedSource(connectionString,
            query,
            credentialId,
            DatasourceAuthenticationType.SERVICE_PRINCIPAL_IN_KV);
    }

    /**
     * Get the query that retrieves the values to be analyzed for anomalies.
     *
     * @return the query.
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
