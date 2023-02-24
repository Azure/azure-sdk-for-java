// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;

import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.StorageSeekableByteChannel;
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
    public void write(ByteBuffer src, long destOffset) {
        try (InputStream uploadStream = new ByteBufferMarkableInputStream(src)) {
            client.uploadRangeWithResponse(
                new ShareFileUploadRangeOptions(uploadStream, src.remaining())
                    .setOffset(destOffset).setRequestConditions(conditions).setLastWrittenMode(lastWrittenMode),
                null, null);
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
    }

    @Override
    public void commit(long totalLength) {
        // no-op
    }

    @Override
    public boolean canSeek(long position) {
        if (fileSize == null) {
            fileSize = client.getProperties().getContentLength();
        }
        return 0 <= position && position <= fileSize;
    }

    @Override
    public void resize(long newSize) {
        throw LOGGER.logExceptionAsError(new UnsupportedOperationException());
    }
}
