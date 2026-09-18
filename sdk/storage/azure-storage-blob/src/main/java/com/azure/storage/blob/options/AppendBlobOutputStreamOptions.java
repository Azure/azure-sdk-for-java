// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.blob.models.AppendBlobRequestConditions;
import com.azure.storage.common.ContentValidationAlgorithm;

/**
 * Extended options that may be passed when opening an output stream to an append blob.
 */
@Fluent
public final class AppendBlobOutputStreamOptions {
    private AppendBlobRequestConditions requestConditions;
    private ContentValidationAlgorithm contentValidationAlgorithm;

    /**
     * Creates a new instance of {@link AppendBlobOutputStreamOptions}.
     */
    public AppendBlobOutputStreamOptions() {
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
    public AppendBlobOutputStreamOptions setRequestConditions(AppendBlobRequestConditions requestConditions) {
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
    public AppendBlobOutputStreamOptions
        setContentValidationAlgorithm(ContentValidationAlgorithm contentValidationAlgorithm) {
        this.contentValidationAlgorithm = contentValidationAlgorithm;
        return this;
    }
}
