// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration.models;

/**
 * The base credential type for different types of authentication
 * that service uses to access the data sources {@link DataFeedSource}.
 *
 * @see DataSourceDataLakeGen2SharedKey
 * @see DataSourceServicePrincipal
 * @see DataSourceServicePrincipalInKeyVault
 * @see DataSourceSqlServerConnectionString
 */
public abstract class DataSourceCredentialEntity {
    /**
     * Gets the credential id.
     *
     * @return The credential id.
     */
    public abstract String getId();
    /**
     * Gets the credential name.
     *
     * @return The credential name.
     */
    public abstract String getName();
    /**
     * Gets the credential description.
     *
     * @return The credential description.
     */
    public abstract String getDescription();
}
