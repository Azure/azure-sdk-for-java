// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.models;

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

    /**
     * Create a AzureBlobDataFeedSource instance.
     *
     * @param connectionString the Azure Blob connection string
     * @param container the container name
     * @param blobTemplate the blob template name
     */
    public AzureBlobDataFeedSource(final String connectionString, final String container, final String blobTemplate) {
        this.connectionString = connectionString;
        this.container = container;
        this.blobTemplate = blobTemplate;
    }

    /**
     * Get the Azure Blob connection string.
     *
     * @return the connectionString value.
     */
    public String getConnectionString() {
        return this.connectionString;
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


}
