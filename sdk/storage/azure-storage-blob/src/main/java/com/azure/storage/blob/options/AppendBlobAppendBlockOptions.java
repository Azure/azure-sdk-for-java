// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.options;

import com.azure.core.util.BinaryData;
import com.azure.core.util.CoreUtils;
import com.azure.storage.blob.models.AppendBlobRequestConditions;
import com.azure.storage.common.implementation.StorageImplUtils;

/**
 * Extended options that may be passed when appending a block.
 */
public class AppendBlobAppendBlockOptions {
    private final BinaryData data;
    private byte[] contentMd5;
    private AppendBlobRequestConditions conditions;

    /**
     * @param data The data to append to the blob. Note that this {@code BinaryData} must have defined length
     * and must be replayable if retries are enabled (the default), see {@link BinaryData#isReplayable()}.
     */
    public AppendBlobAppendBlockOptions(BinaryData data) {
        StorageImplUtils.assertNotNull("data must not be null", data);
        StorageImplUtils.assertNotNull("data must have defined length", data.getLength());
        this.data = data;
    }

    /**
     * @return The data to write to the blob.
     */
    public BinaryData getData() {
        return data;
    }

    /**
     * @return The precalculated MD5 for this instance's BinaryData.
     */
    public byte[] getContentMd5() {
        return CoreUtils.clone(contentMd5);
    }

    /**
     * @param contentMd5 Precalculated MD5 for this instance's BinaryData.
     * @return The updated options.
     */
    public AppendBlobAppendBlockOptions setContentMd5(byte[] contentMd5) {
        this.contentMd5 = CoreUtils.clone(contentMd5);
        return this;
    }

    /**
     * @return The request conditions for this operation.
     */
    public AppendBlobRequestConditions getConditions() {
        return conditions;
    }

    /**
     * @param conditions Request conditions for this operation.
     * @return The updated options.
     */
    public AppendBlobAppendBlockOptions setConditions(AppendBlobRequestConditions conditions) {
        this.conditions = conditions;
        return this;
    }
}
