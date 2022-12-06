// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.options;

import com.azure.core.util.BinaryData;
import com.azure.core.util.CoreUtils;
import com.azure.storage.blob.models.PageBlobRequestConditions;
import com.azure.storage.blob.models.PageRange;
import com.azure.storage.common.implementation.StorageImplUtils;

/**
 * Extended options that may be passed when uploading pages.
 */
public class PageBlobUploadPagesOptions {
    private final PageRange pageRange;
    private final BinaryData data;
    private byte[] contentMd5;
    private PageBlobRequestConditions conditions;

    /**
     * @param pageRange The page range to upload to.
     * @param data The data to write to the block. Note that this {@code BinaryData} must have defined length
     * and must be replayable if retries are enabled (the default), see {@link BinaryData#isReplayable()}.
     */
    public PageBlobUploadPagesOptions(PageRange pageRange, BinaryData data) {
        StorageImplUtils.assertNotNull("pageRange must not be null", pageRange);
        StorageImplUtils.assertNotNull("data must not be null", data);
        this.pageRange = pageRange;
        this.data = data;
    }

    /**
     * @return The block ID to assign the new block.
     */
    public PageRange getPageRange() {
        return pageRange;
    }

    /**
     * @return The data to write to the blob.
     */
    public BinaryData getData() {
        return data;
    }

    /**
     * @return The MD5 of this instance's data.
     */
    public byte[] getContentMd5() {
        return CoreUtils.clone(contentMd5);
    }

    /**
     * @param contentMd5 MD5 of this instance's data.
     * @return The updated options.
     */
    public PageBlobUploadPagesOptions setContentMd5(byte[] contentMd5) {
        this.contentMd5 = CoreUtils.clone(contentMd5);
        return this;
    }

    /**
     * @return The request conditions for this operation.
     */
    public PageBlobRequestConditions getConditions() {
        return conditions;
    }

    /**
     * @param conditions Request conditions for this operation.
     * @return The updated options.
     */
    public PageBlobUploadPagesOptions setConditions(PageBlobRequestConditions conditions) {
        this.conditions = conditions;
        return this;
    }
}
