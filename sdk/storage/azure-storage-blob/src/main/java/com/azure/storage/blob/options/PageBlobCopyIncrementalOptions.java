// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.blob.models.PageBlobCopyIncrementalRequestConditions;
import com.azure.storage.common.implementation.StorageImplUtils;

/**
 * Extended options that may be passed when incrementally copying a Page Blob.
 */
@Fluent
public class PageBlobCopyIncrementalOptions {

    private final String source;
    private final String snapshot;
    private PageBlobCopyIncrementalRequestConditions requestConditions;

    /**
     * Creates a new instance of {@link PageBlobCopyIncrementalOptions}.
     *
     * @param source The source page blob.
     * @param snapshot The snapshot on the copy source.
     * @throws NullPointerException If {@code source} or {@code snapshot} is null.
     */
    public PageBlobCopyIncrementalOptions(String source, String snapshot) {
        StorageImplUtils.assertNotNull("source", source);
        StorageImplUtils.assertNotNull("snapshot", snapshot);
        this.source = source;
        this.snapshot = snapshot;
    }

    /**
     * Gets the source page blob.
     *
     * @return The source page blob.
     */
    public String getSource() {
        return source;
    }

    /**
     * Gets the snapshot on the copy source.
     *
     * @return The snapshot on the copy source.
     */
    public String getSnapshot() {
        return snapshot;
    }

    /**
     * Gets the {@link PageBlobCopyIncrementalRequestConditions} for the destination.
     *
     * @return {@link PageBlobCopyIncrementalRequestConditions} for the destination.
     */
    public PageBlobCopyIncrementalRequestConditions getRequestConditions() {
        return requestConditions;
    }

    /**
     * Sets the {@link PageBlobCopyIncrementalRequestConditions} for the blob.
     *
     * @param requestConditions {@link PageBlobCopyIncrementalRequestConditions} for the blob.
     * @return The updated options.
     */
    public PageBlobCopyIncrementalOptions
        setRequestConditions(PageBlobCopyIncrementalRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }
}
