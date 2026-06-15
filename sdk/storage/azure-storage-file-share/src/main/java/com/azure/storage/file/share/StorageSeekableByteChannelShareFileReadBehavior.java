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
            ShareFileDownloadResponse response = client.downloadWithResponse(dstStream,
                new ShareFileDownloadOptions()
                    .setRange(new ShareFileRange(sourceOffset, sourceOffset + dst.remaining() - 1))
                    .setRequestConditions(conditions),
                null, null);
            lastKnownResourceLength
                = CoreUtils.extractSizeFromContentRange(response.getDeserializedHeaders().getContentRange());
            return dst.position() - initialPosition;
        } catch (RuntimeException e) {
            // Non-storage failure path. The stress-test scenario observed in issue #38070 is a connection reset
            // while the 416 error-body is being streamed back: downloadWithResponse surfaces it as a
            // ReactiveException wrapping a NativeIoException rather than the ShareStorageException handled above.
            // If we already have all the bytes the caller asked for (sourceOffset is at or past the cached
            // resource length), the error is irrelevant -- log it and report EOF.
            Throwable cause = e;
            while (cause != null && !(cause instanceof IOException) && cause.getCause() != cause) {
                cause = cause.getCause();
            }
            if (cause instanceof IOException
                && lastKnownResourceLength != UNKNOWN_LENGTH
                && sourceOffset >= lastKnownResourceLength) {
                LOGGER.warning(
                    "Ignoring error from past-EOF range read (offset={}, length={}); treating as end of stream.",
                    sourceOffset, lastKnownResourceLength, e);
                return -1;
            }
            throw LOGGER.logExceptionAsError(e);
        }
    }

    @Override
    public long getResourceLength() {
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
