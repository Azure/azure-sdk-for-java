// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.implementation.util.ByteBufferBackedOutputStreamUtil;
import com.azure.storage.blob.models.BlobDownloadResponse;
import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.common.implementation.StorageSeekableByteChannel;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Objects;

class StorageSeekableByteChannelBlobReadBehavior implements StorageSeekableByteChannel.ReadBehavior {
    private static final ClientLogger LOGGER = new ClientLogger(StorageSeekableByteChannelBlobReadBehavior.class);

    private final BlobClientBase client;
    private final BlobRequestConditions requestConditions;

    private long resourceLength;

    private ByteBuffer initialBuffer;
    private Long initialBufferPosition;

    /**
     * Constructs new read behavior for a target blob. Blob clients return blob properties with the channel. They do
     * this by fetching an initial range upfront and getting the properties off the response. This class allows that
     * range to be cached for an initial call to {@link #read(ByteBuffer, long)} from the
     * {@link StorageSeekableByteChannel} this instance powers.
     *
     * @param client The target blob to read from.
     * @param requestConditions Request conditions to use for reading.
     * @param initialBuffer An initially downloaded buffer to cache for first call to {@link #read(ByteBuffer, long)}.
     * @param initialBufferPosition Position of initialBuffer in the target resource.
     * @param resourceLength Length of the target resource.
     */
    StorageSeekableByteChannelBlobReadBehavior(BlobClientBase client, ByteBuffer initialBuffer,
        long initialBufferPosition, long resourceLength, BlobRequestConditions requestConditions) {
        this.client = Objects.requireNonNull(client);
        this.initialBuffer = Objects.requireNonNull(initialBuffer);
        this.initialBufferPosition = initialBufferPosition;
        this.resourceLength = resourceLength;
        this.requestConditions = requestConditions;
    }

    BlobClientBase getClient() {
        return this.client;
    }

    BlobRequestConditions getRequestConditions() {
        return this.requestConditions;
    }

    @Override
    public int read(ByteBuffer dst, long sourceOffset) throws IOException {
        if (dst.remaining() <= 0) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'dst.remaining()' must be positive."));
        }
        if (initialBufferPosition != null && initialBuffer != null && initialBufferPosition == sourceOffset) {
            return readFromCache(dst);
        } else {
            // don't hold onto cache when getting new data, wipe it
            initialBuffer = null;
            initialBufferPosition = null;
        }

        int initialPosition = dst.position();

        try (ByteBufferBackedOutputStreamUtil dstStream = new ByteBufferBackedOutputStreamUtil(dst)) {
            BlobDownloadResponse response =  client.downloadStreamWithResponse(dstStream,
                new BlobRange(sourceOffset, (long) dst.remaining()), null /*downloadRetryOptions*/, requestConditions,
                false, null, null);
            resourceLength = CoreUtils.extractSizeFromContentRange(
                response.getDeserializedHeaders().getContentRange());
            return dst.position() - initialPosition;
        } catch (BlobStorageException e) {
            if (e.getErrorCode() == BlobErrorCode.INVALID_RANGE) {
                String contentRange = e.getResponse().getHeaderValue("Content-Range");
                if (contentRange != null) {
                    resourceLength = CoreUtils.extractSizeFromContentRange(contentRange);
                }
                // if requested offset is past updated end of file, then signal end of file. Otherwise, only signal
                // that zero bytes were read
                return sourceOffset < resourceLength ? 0 : -1;
            }
            throw LOGGER.logExceptionAsError(e);
        }
    }

    /**
     * Reads the cached byte buffer into the provided byte buffer and clears the cache.
     * @param dst Destination for read contents.
     * @return Number of bytes read.
     */
    private int readFromCache(ByteBuffer dst) {
        int read = Math.min(dst.remaining(), initialBuffer.remaining());
        if (read > 0) {
            ByteBuffer temp = initialBuffer.duplicate();
            temp.limit(temp.position() + read);
            dst.put(temp);
        }
        initialBuffer = null;
        initialBufferPosition = null;
        return read;
    }

    @Override
    public long getResourceLength() {
        return resourceLength;
    }
}
