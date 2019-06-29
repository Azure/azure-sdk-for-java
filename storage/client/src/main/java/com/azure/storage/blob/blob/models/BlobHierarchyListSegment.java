// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.blob.models;

import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobPrefix;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * The BlobHierarchyListSegment model.
 */
@JacksonXmlRootElement(localName = "Blobs")
public final class BlobHierarchyListSegment {
    /*
     * The blobPrefixes property.
     */
    @JsonProperty("BlobPrefix")
    private List<com.azure.storage.blob.models.BlobPrefix> blobPrefixes = new ArrayList<>();

    /*
     * The blobItems property.
     */
    @JsonProperty("Blob")
    private List<com.azure.storage.blob.models.BlobItem> blobItems = new ArrayList<>();

    /**
     * Get the blobPrefixes property: The blobPrefixes property.
     *
     * @return the blobPrefixes value.
     */
    public List<com.azure.storage.blob.models.BlobPrefix> blobPrefixes() {
        return this.blobPrefixes;
    }

    /**
     * Set the blobPrefixes property: The blobPrefixes property.
     *
     * @param blobPrefixes the blobPrefixes value to set.
     * @return the BlobHierarchyListSegment object itself.
     */
    public BlobHierarchyListSegment blobPrefixes(List<BlobPrefix> blobPrefixes) {
        this.blobPrefixes = blobPrefixes;
        return this;
    }

    /**
     * Get the blobItems property: The blobItems property.
     *
     * @return the blobItems value.
     */
    public List<com.azure.storage.blob.models.BlobItem> blobItems() {
        return this.blobItems;
    }

    /**
     * Set the blobItems property: The blobItems property.
     *
     * @param blobItems the blobItems value to set.
     * @return the BlobHierarchyListSegment object itself.
     */
    public BlobHierarchyListSegment blobItems(List<BlobItem> blobItems) {
        this.blobItems = blobItems;
        return this;
    }
}
