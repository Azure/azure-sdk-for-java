// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.options;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.CoreUtils;
import com.azure.storage.blob.models.AppendBlobRequestConditions;
import com.azure.storage.common.ContentValidationAlgorithm;

/**
 * Extended options that may be passed when appending a block to an append blob.
 */
@Fluent
public final class AppendBlobAppendBlockOptions {
    private byte[] contentMd5;
    private AppendBlobRequestConditions requestConditions;
    private ContentValidationAlgorithm contentValidationAlgorithm;

    /**
     * Creates a new instance of {@link AppendBlobAppendBlockOptions}.
     */
    public AppendBlobAppendBlockOptions() {
    }

    /**
     * Gets the MD5 hash of the block content.
     *
     * @return An MD5 hash of the content, or null.
     */
    public byte[] getContentMd5() {
        return CoreUtils.clone(contentMd5);
    }

    /**
     * Sets the MD5 hash of the block content for transactional verification.
     *
     * @param contentMd5 An MD5 hash of the block content.
     * @return The updated options.
     */
    public AppendBlobAppendBlockOptions setContentMd5(byte[] contentMd5) {
        this.contentMd5 = CoreUtils.clone(contentMd5);
        return this;
    }

    /**
     * Gets the {@link AppendBlobRequestConditions}.
     *
     * @return The request conditions.
     */
    public AppendBlobRequestConditions getRequestConditions() {
        return requestConditions;
    }

    /**
     * Sets the {@link AppendBlobRequestConditions}.
     *
     * @param requestConditions The request conditions.
     * @return The updated options.
     */
    public AppendBlobAppendBlockOptions setRequestConditions(AppendBlobRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }

    /**
     * Gets the algorithm to use for transfer content validation. See {@link ContentValidationAlgorithm} for more details.
     *
     * @return The transfer validation checksum algorithm.
     */
    public ContentValidationAlgorithm getContentValidationAlgorithm() {
        return contentValidationAlgorithm;
    }

    /**
     * Sets the algorithm to use for transfer content validation. See {@link ContentValidationAlgorithm} for more details.
     *
     * @param contentValidationAlgorithm The transfer validation checksum algorithm.
     * @return The updated options.
     */
    public AppendBlobAppendBlockOptions
        setContentValidationAlgorithm(ContentValidationAlgorithm contentValidationAlgorithm) {
        this.contentValidationAlgorithm = contentValidationAlgorithm;
        return this;
    }
}
