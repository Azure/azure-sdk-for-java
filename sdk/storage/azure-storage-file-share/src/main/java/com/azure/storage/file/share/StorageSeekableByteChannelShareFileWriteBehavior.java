// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;

import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.implementation.StorageSeekableByteChannel;
import com.azure.storage.common.implementation.ByteBufferMarkableInputStream;
import com.azure.storage.file.share.models.FileLastWrittenMode;
import com.azure.storage.file.share.models.ShareFileUploadRangeOptions;
import com.azure.storage.file.share.models.ShareRequestConditions;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Objects;

class StorageSeekableByteChannelShareFileWriteBehavior implements StorageSeekableByteChannel.WriteBehavior {
    private static final ClientLogger LOGGER = new ClientLogger(StorageSeekableByteChannelShareFileWriteBehavior.class);

    private final ShareFileClient client;
    private final ShareRequestConditions conditions;
    private final FileLastWrittenMode lastWrittenMode;

    private Long fileSize;

    StorageSeekableByteChannelShareFileWriteBehavior(ShareFileClient client, ShareRequestConditions conditions,
        FileLastWrittenMode lastWrittenMode) {
        this.client = Objects.requireNonNull(client, "'client' cannot be null.");
        this.conditions = conditions;
        this.lastWrittenMode = lastWrittenMode;
    }

    ShareFileClient getClient() {
        return this.client;
    }

    ShareRequestConditions getRequestConditions() {
        return this.conditions;
    }

    FileLastWrittenMode getLastWrittenMode() {
        return this.lastWrittenMode;
    }

    @Override
    public void write(ByteBuffer src, long destOffset) throws IOException {
        InputStream uploadStream = new ByteBufferMarkableInputStream(src);
        client.uploadRangeWithResponse(
            new ShareFileUploadRangeOptions(uploadStream, src.remaining())
                .setOffset(destOffset).setRequestConditions(conditions).setLastWrittenMode(lastWrittenMode),
            null, null);
    }

    @Override
    public void commit(long totalLength) {
        // no-op
    }

    @Override
    public void assertCanSeek(long position) {
        if (fileSize == null) {
            fileSize = client.getProperties().getContentLength();
        }
        if (0 <= position && position <= fileSize) {
            return;
        }
        throw new UnsupportedOperationException("Cannot seek beyond bounds of file.");
    }

    @Override
    public void resize(long newSize) {
        throw LOGGER.logExceptionAsError(new UnsupportedOperationException(
            "Setting share file size not supported through SeekableByteChannel interface."));
    }
}
