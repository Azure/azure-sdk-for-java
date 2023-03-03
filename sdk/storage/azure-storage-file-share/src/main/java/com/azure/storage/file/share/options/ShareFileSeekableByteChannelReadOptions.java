// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.options;

import com.azure.storage.file.share.models.ShareRequestConditions;

public class ShareFileSeekableByteChannelReadOptions {
    private ShareRequestConditions requestConditions;
    private Long chunkSize;

    /**
     * @return The size of individual writes to the service.
     */
    public Long getChunkSize() {
        return chunkSize;
    }

    /**
     * @param chunkSize The size of individual writes to the service.
     * @return The updated instance.
     */
    public ShareFileSeekableByteChannelReadOptions setChunkSize(Long chunkSize) {
        this.chunkSize = chunkSize;
        return this;
    }

    /**
     * @return Request conditions to be used by the resulting channel.
     */
    public ShareRequestConditions getRequestConditions() {
        return requestConditions;
    }

    /**
     * @param requestConditions Request conditions to be used by the resulting channel.
     * @return The updated instance.
     */
    public ShareFileSeekableByteChannelReadOptions setRequestConditions(ShareRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }
}
