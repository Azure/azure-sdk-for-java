package com.azure.storage.file.share;

import com.azure.storage.common.StorageSeekableByteChannel;
import com.azure.storage.file.share.models.ShareErrorCode;
import com.azure.storage.file.share.models.ShareFileDownloadResponse;
import com.azure.storage.file.share.models.ShareFileRange;
import com.azure.storage.file.share.models.ShareRequestConditions;
import com.azure.storage.file.share.models.ShareStorageException;
import com.azure.storage.file.share.options.ShareFileDownloadOptions;
import com.fasterxml.jackson.databind.util.ByteBufferBackedOutputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class StorageSeekableByteChannelShareFileReadBehavior implements StorageSeekableByteChannel.ReadBehavior {
    private final ShareFileClient client;
    private final ShareRequestConditions conditions;

    private Long lastKnownResourceLength;

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
        int initialPosition = dst.position();

        /* Optimistically assume cached resource length has not changed, but don't take that for truth. If read strategy
         * uses etag locking, conditions will catch changes and cause an etag error. If read strategy uses versioning,
         * changes are impossible. Otherwise, assume the file length may have changed.
         * If, based on cached length, the download range contains _some_ bytes but will still cause an invalid range
         * exception: trim download range to ensure success.
         * If, based on cached length, the download range would return zero bytes (offset is past cached length): make
         * the full range call and deal with potential invalid range. The error will return Content-Range to confirm
         * whether we are at end of file.
         */
        int actualLength = getCachedLength() == null
            ? dst.remaining()
            : (int) Math.min(dst.remaining(), getCachedLength() - sourceOffset);
        if (actualLength <= 0) {
            actualLength = dst.remaining();
        }

        try (ByteBufferBackedOutputStream dstStream = new ByteBufferBackedOutputStream(dst)) {
            ShareFileDownloadResponse response =  client.downloadWithResponse(dstStream,
                new ShareFileDownloadOptions()
                    .setRange(new ShareFileRange(sourceOffset, sourceOffset + actualLength - 1))
                    .setRequestConditions(conditions),
                null, null);
            lastKnownResourceLength = getResourceLengthFromContentRange(
                response.getDeserializedHeaders().getContentRange());
            return dst.position() - initialPosition;
        } catch (ShareStorageException e) {
            if (e.getErrorCode() == ShareErrorCode.INVALID_RANGE) {
                String contentRange = e.getResponse().getHeaderValue("Content-Range");
                if (contentRange != null) {
                    lastKnownResourceLength = getResourceLengthFromContentRange(contentRange);
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

    private void updateResourceCachedState(ShareFileDownloadResponse response) {
        if (response == null) {
            return;
        }
        String contentRange = response.getDeserializedHeaders().getContentRange();
        lastKnownResourceLength = getResourceLengthFromContentRange(contentRange);
    }

    @Override
    public Long getCachedLength() {
        return lastKnownResourceLength;
    }

    private static long getResourceLengthFromContentRange(String contentRange) {
        return Long.parseLong(contentRange.split("/")[1]);
    }
}
