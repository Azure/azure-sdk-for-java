// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.administration.models;

import com.azure.core.annotation.Fluent;

/** Azure Blob Storage content. */
@Fluent
public final class AzureBlobContentSource {
    /*
     * Azure Blob Storage container URL.
     */
    private final String containerUrl;

    /*
     * Blob name prefix.
     */
    private String prefix;

    /**
     * Creates an instance of AzureBlobContentSource class.
     *
     * @param containerUrl the containerUrl value to set.
     */
    public AzureBlobContentSource(String containerUrl) {
        this.containerUrl = containerUrl;
    }

    /**
     * Get the containerUrl property: Azure Blob Storage container URL.
     *
     * @return the containerUrl value.
     */
    public String getContainerUrl() {
        return this.containerUrl;
    }

    /**
     * Get the prefix property: Blob name prefix.
     *
     * @return the prefix value.
     */
    public String getPrefix() {
        return this.prefix;
    }

    /**
     * Set the prefix property: Blob name prefix.
     *
     * @param prefix the prefix value to set.
     * @return the AzureBlobContentSource object itself.
     */
    public AzureBlobContentSource setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }
}
