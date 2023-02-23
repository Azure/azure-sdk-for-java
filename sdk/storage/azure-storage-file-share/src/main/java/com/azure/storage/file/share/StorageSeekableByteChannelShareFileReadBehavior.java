package com.azure.storage.file.share;

import com.azure.storage.common.StorageSeekableByteChannel;
import com.azure.storage.common.Utility;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.file.share.models.ShareErrorCode;
import com.azure.storage.file.share.models.ShareFileDownloadResponse;
import com.azure.storage.file.share.models.ShareFileRange;
import com.azure.storage.file.share.models.ShareRequestConditions;
import com.azure.storage.file.share.models.ShareStorageException;
import com.azure.storage.file.share.options.ShareFileDownloadOptions;
import com.fasterxml.jackson.databind.util.ByteBufferBackedOutputStream;

import java.io.IOException;
import java.nio.ByteBuffer;

class StorageSeekableByteChannelShareFileReadBehavior implements StorageSeekableByteChannel.ReadBehavior {
    private final ShareFileClient client;
    private final ShareRequestConditions conditions;

    private Long lastKnownResourceLength;

    private ByteBuffer cachedRead;
    private Long cachedReadOffset;

    public StorageSeekableByteChannelShareFileReadBehavior(ShareFileClient client, ShareRequestConditions conditions) {
        this.client = client;
        this.conditions = conditions;
    }

    public ShareFileClient getClient() {
        return this.client;
    }

    public ShareRequestConditions getRequestConditions() {
        return this.conditions;
    }

    @Override
    public int read(ByteBuffer dst, long sourceOffset) {
        if (dst.remaining() <= 0) {
            throw new IllegalArgumentException("'dst.remaining()' must be positive.");
        }

        int initialPosition = dst.position();

        // if the cached read is being requested, give it
        if (cachedReadOffset != null && cachedReadOffset == sourceOffset) {
            int read = Utility.byteBufferCopyAvailable(cachedRead, dst);
            cachedRead = null;
            cachedReadOffset = null;
            return read;
        // if it isn't, but we have one, don't hold onto it, just invalidate
        } else {
            cachedRead = null;
            cachedReadOffset = null;
        }

        try (ByteBufferBackedOutputStream dstStream = new ByteBufferBackedOutputStream(dst)) {
            ShareFileDownloadResponse response =  client.downloadWithResponse(dstStream,
                new ShareFileDownloadOptions()
                    .setRange(new ShareFileRange(sourceOffset, sourceOffset + dst.remaining() - 1))
                    .setRequestConditions(conditions),
                null, null);
            lastKnownResourceLength = StorageImplUtils.parseContentLengthFromContentRangeHeader(
                response.getDeserializedHeaders().getContentRange());
            return dst.position() - initialPosition;
        } catch (ShareStorageException e) {
            if (e.getErrorCode() == ShareErrorCode.INVALID_RANGE) {
                String contentRange = e.getResponse().getHeaderValue("Content-Range");
                if (contentRange != null) {
                    lastKnownResourceLength = StorageImplUtils.parseContentLengthFromContentRangeHeader(contentRange);
                }
                // if requested offset is past updated end of file, then signal end of file. Otherwise, only signal
                // that zero bytes were read
                return sourceOffset < lastKnownResourceLength ? 0 : -1;
            }
            throw e;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long getCachedLength() {
        if (lastKnownResourceLength == null) {
            lastKnownResourceLength = client.getProperties().getContentLength();
        }
        return lastKnownResourceLength;
    }

    void setCachedLength(Long length) {
        lastKnownResourceLength = length;
    }

    public void setCachedReadValue(ByteBuffer data, Long offset) {
        if ((data != null && offset == null) ||
            (data == null && offset != null)) {
            throw new IllegalArgumentException("Cannot supply only one of 'data' and 'offset'.");
        }
        this.cachedRead = data;
        this.cachedReadOffset = offset;
    }
}
