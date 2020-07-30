// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.blob.models.BlobDestinationRequestConditions;
import com.azure.storage.common.implementation.StorageImplUtils;

/**
 * Extended options that may be passed when incrementally copying a Page Blob.
 */
@Fluent
public class PageBlobCopyIncrementalOptions {

    private final String source;
    private final String snapshot;
    private BlobDestinationRequestConditions destinationRequestConditions;

    /**
     * @param source The source page blob.
     * @param snapshot The snapshot on the copy source.
     */
    public PageBlobCopyIncrementalOptions(String source, String snapshot) {
        StorageImplUtils.assertNotNull("source", source);
        StorageImplUtils.assertNotNull("snapshot", snapshot);
        this.source = source;
        this.snapshot = snapshot;
    }

    /**
     * @return The source page blob.
     */
    public String getSource() {
        return source;
    }

    /**
     * @return The snapshot on the copy source.
     */
    public String getSnapshot() {
        return snapshot;
    }

    /**
     * @return {@link BlobDestinationRequestConditions} for the destination.
     */
    public BlobDestinationRequestConditions getDestinationRequestConditions() {
        return destinationRequestConditions;
    }

    /**
     * @param destinationRequestConditions {@link BlobDestinationRequestConditions} for the destination.
     * @return The updated options.
     */
    public PageBlobCopyIncrementalOptions setDestinationRequestConditions(
        BlobDestinationRequestConditions destinationRequestConditions) {
        this.destinationRequestConditions = destinationRequestConditions;
        return this;
    }
}
