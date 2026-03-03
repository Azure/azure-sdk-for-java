// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.blob.models.AppendBlobRequestConditions;
import com.azure.storage.common.implementation.contentvalidation.StorageChecksumAlgorithm;

/**
 * Extended options that may be passed when opening an output stream to an append blob.
 */
@Fluent
public final class AppendBlobOutputStreamOptions {
    private AppendBlobRequestConditions requestConditions;
    private StorageChecksumAlgorithm requestChecksumAlgorithm;

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
     * Gets the algorithm to use for request content validation. Default is {@link StorageChecksumAlgorithm#NONE}.
     *
     * @return The request checksum algorithm.
     */
    public StorageChecksumAlgorithm getRequestChecksumAlgorithm() {
        return requestChecksumAlgorithm;
    }

    /**
     * Sets the algorithm to use for request content validation. When set to {@link StorageChecksumAlgorithm#AUTO},
     * {@link StorageChecksumAlgorithm#CRC64}, or {@link StorageChecksumAlgorithm#MD5}, the SDK will compute and send
     * checksums for upload validation. Default is {@link StorageChecksumAlgorithm#NONE}.
     *
     * @param requestChecksumAlgorithm The request checksum algorithm.
     * @return The updated options.
     */
    public AppendBlobOutputStreamOptions
        setRequestChecksumAlgorithm(StorageChecksumAlgorithm requestChecksumAlgorithm) {
        this.requestChecksumAlgorithm = requestChecksumAlgorithm;
        return this;
    }
}
