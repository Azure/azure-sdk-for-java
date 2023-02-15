package com.azure.storage.file.share;

import com.azure.storage.common.StorageSeekableByteChannel;
import com.azure.storage.file.share.models.ShareFileDownloadResponse;
import com.azure.storage.file.share.models.ShareFileRange;
import com.azure.storage.file.share.models.ShareRequestConditions;
import com.azure.storage.file.share.options.ShareFileDownloadOptions;
import com.fasterxml.jackson.databind.util.ByteBufferBackedOutputStream;

import java.io.IOException;
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
        try (ByteBufferBackedOutputStream dstStream = new ByteBufferBackedOutputStream(dst)) {
            int initialPosition = dst.position();
            int actualLength = getCachedLength() == null
                ? dst.remaining()
                : (int) Math.min(dst.remaining(), getCachedLength() - sourceOffset);
            if (actualLength <= 0) {
                // TODO (jaschrep): If not etag/version locked, resource may have grown
                return -1;
            }

            // TODO (jaschrep): If not etag/version locked, resource may have shrunk. Recover from bad range.
            ShareFileDownloadResponse response = client.downloadWithResponse(dstStream, new ShareFileDownloadOptions()
                    .setRange(new ShareFileRange(sourceOffset, sourceOffset + actualLength - 1))
                    .setRequestConditions(conditions),
                null, null);
            updateResourceCachedState(response);

            // the actual amount read may differ from the amount we calculated we'd read, ensure we return actual read
            return dst.position() - initialPosition;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private void updateResourceCachedState(ShareFileDownloadResponse response) {
        if (response == null) {
            return;
        }
        String contentRange = response.getDeserializedHeaders().getContentRange();
        lastKnownResourceLength = Long.parseLong(contentRange.split("/")[1]);
    }

    @Override
    public Long getCachedLength() {
        return lastKnownResourceLength;
    }
}
