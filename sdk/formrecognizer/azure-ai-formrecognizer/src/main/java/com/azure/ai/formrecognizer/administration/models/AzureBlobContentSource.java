// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.administration.models;

/** Azure Blob Storage content. */
public class AzureBlobContentSource extends ContentSource {
    /*
     * Azure Blob Storage container URL.
     */
    private final String containerUrl;

    /*
     * Blob name prefix.
     */
    private String prefix;

    /**
     * Creates a AzureBlobContentSource instance.
     * @param containerUrl Azure Blob Storage container URL.
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
     * @return the ContentSource object itself.
     */
    public ContentSource setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }
}
