// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.implementation.StorageSeekableByteChannel;
import com.azure.storage.file.share.models.ShareErrorCode;
import com.azure.storage.file.share.models.ShareFileDownloadResponse;
import com.azure.storage.file.share.models.ShareFileRange;
import com.azure.storage.file.share.models.ShareRequestConditions;
import com.azure.storage.file.share.models.ShareStorageException;
import com.azure.storage.file.share.options.ShareFileDownloadOptions;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

class StorageSeekableByteChannelShareFileReadBehavior implements StorageSeekableByteChannel.ReadBehavior {
    private static final ClientLogger LOGGER = new ClientLogger(StorageSeekableByteChannelShareFileReadBehavior.class);
    private static final long UNKNOWN_LENGTH = -1;

    private final ShareFileClient client;
    private final ShareRequestConditions conditions;

    private long lastKnownResourceLength;

    StorageSeekableByteChannelShareFileReadBehavior(ShareFileClient client, ShareRequestConditions conditions) {
        this.client = client;
        this.conditions = conditions;
        this.lastKnownResourceLength = UNKNOWN_LENGTH;
    }

    ShareFileClient getClient() {
        return this.client;
    }

    ShareRequestConditions getRequestConditions() {
        return this.conditions;
    }

    @Override
    public int read(ByteBuffer dst, long sourceOffset) throws IOException {
        if (dst.remaining() <= 0) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'dst.remaining()' must be positive."));
        }

        int initialPosition = dst.position();

        try (ByteBufferBackedOutputStream dstStream = new ByteBufferBackedOutputStream(dst)) {
            ShareFileDownloadResponse response =  client.downloadWithResponse(dstStream,
                new ShareFileDownloadOptions()
                    .setRange(new ShareFileRange(sourceOffset, sourceOffset + dst.remaining() - 1))
                    .setRequestConditions(conditions),
                null, null);
            lastKnownResourceLength = CoreUtils.extractSizeFromContentRange(
                response.getDeserializedHeaders().getContentRange());
            return dst.position() - initialPosition;
        } catch (ShareStorageException e) {
            if (e.getErrorCode() == ShareErrorCode.INVALID_RANGE) {
                String contentRange = e.getResponse().getHeaderValue("Content-Range");
                if (contentRange != null) {
                    lastKnownResourceLength = CoreUtils.extractSizeFromContentRange(contentRange);
                }
                // if requested offset is past updated end of file, then signal end of file. Otherwise, only signal
                // that zero bytes were read
                return sourceOffset < lastKnownResourceLength ? 0 : -1;
            }
            throw LOGGER.logExceptionAsError(e);
        }
    }

    @Override
    public long getResourceLength() {
        if (lastKnownResourceLength == UNKNOWN_LENGTH) {
            lastKnownResourceLength = client.getProperties().getContentLength();
        }
        return lastKnownResourceLength;
    }

    private static final class ByteBufferBackedOutputStream extends OutputStream {
        private final ByteBuffer dst;

        ByteBufferBackedOutputStream(ByteBuffer dst) {
            this.dst = dst;
        }

        @Override
        public void write(int b) {
            dst.put((byte) b);
        }

        @Override
        public void write(byte[] b) {
            dst.put(b);
        }

        @Override
        public void write(byte[] b, int off, int len) {
            dst.put(b, off, len);
        }
    }
}
