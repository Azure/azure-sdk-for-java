// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration.models;

import com.azure.ai.metricsadvisor.implementation.util.AzureBlobDataFeedSourceAccessor;
import com.azure.core.annotation.Immutable;

/**
 * The AzureBlobDataFeedSource model.
 */
@Immutable
public final class AzureBlobDataFeedSource extends DataFeedSource {
    /*
     * Azure Blob connection string
     */
    private final String connectionString;

    /*
     * Container
     */
    private final String container;

    /*
     * Blob Template
     */
    private final String blobTemplate;

    /*
     * The authentication type to access the data source.
     */
    private final DataSourceAuthenticationType authType;

    static {
        AzureBlobDataFeedSourceAccessor.setAccessor(
            new AzureBlobDataFeedSourceAccessor.Accessor() {
                @Override
                public String getConnectionString(AzureBlobDataFeedSource feedSource) {
                    return feedSource.getConnectionString();
                }
            });
    }

    /**
     * Create a AzureBlobDataFeedSource instance.
     *
     * @param connectionString the Azure Blob connection string
     * @param container the container name
     * @param blobTemplate the blob template name
     */
    private AzureBlobDataFeedSource(final String connectionString,
                                    final String container,
                                    final String blobTemplate,
                                    final DataSourceAuthenticationType authType) {
        this.connectionString = connectionString;
        this.container = container;
        this.blobTemplate = blobTemplate;
        this.authType = authType;
    }

    /**
     * Create a AzureBlobDataFeedSource with credential included in the {@code connectionString} as plain text.
     *
     * @param connectionString the Azure Blob connection string
     * @param container the container name
     * @param blobTemplate the blob template name
     *
     * @return The AzureBlobDataFeedSource.
     */
    public static AzureBlobDataFeedSource fromBasicCredential(final String connectionString,
                                                              final String container,
                                                              final String blobTemplate) {
        return new AzureBlobDataFeedSource(connectionString, container, blobTemplate, DataSourceAuthenticationType.BASIC);
    }

    /**
     * Create a SQLServerDataFeedSource with the {@code connectionString} containing the resource
     * id of the SQL server on which metrics advisor has MSI access.
     *
     * @param connectionString the Azure Blob connection string
     * @param container the container name
     * @param blobTemplate the blob template name
     *
     * @return The AzureBlobDataFeedSource.
     */
    public static AzureBlobDataFeedSource fromManagedIdentityCredential(final String connectionString,
                                                                        final String container,
                                                                        final String blobTemplate) {
        return new AzureBlobDataFeedSource(connectionString,
            container,
            blobTemplate,
            DataSourceAuthenticationType.MANAGED_IDENTITY);
    }

    /**
     * Get the container name.
     *
     * @return the container value.
     */
    public String getContainer() {
        return this.container;
    }

    /**
     * Get the blob template name.
     *
     * @return the blobTemplate value.
     */
    public String getBlobTemplate() {
        return this.blobTemplate;
    }

    /**
     * Gets the authentication type to access the data source.
     *
     * @return The authentication type.
     */
    public DataSourceAuthenticationType getAuthenticationType() {
        return this.authType;
    }

    private String getConnectionString() {
        return this.connectionString;
    }
}
