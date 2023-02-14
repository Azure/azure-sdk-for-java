package com.azure.storage.file.share;

import com.azure.storage.common.StorageSeekableByteChannel;
import com.azure.storage.file.share.models.ShareFileDownloadResponse;
import com.azure.storage.file.share.models.ShareFileRange;
import com.azure.storage.file.share.models.ShareRequestConditions;
import com.azure.storage.file.share.options.ShareFileDownloadOptions;
import com.fasterxml.jackson.databind.util.ByteBufferBackedOutputStream;

import java.io.IOException;
import java.nio.ByteBuffer;

public class StorageSeekableBytechannelShareFileReadBehavior implements StorageSeekableByteChannel.ReadBehavior {
    private final ShareFileClient client;
    private final ShareRequestConditions conditions;

    private long _lastKnownResourceLength;

    public StorageSeekableBytechannelShareFileReadBehavior(ShareFileClient client, ShareRequestConditions conditions) {
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
            int prevPosition = dst.position();
            ShareFileDownloadResponse response = client.downloadWithResponse(
                dstStream,
                new ShareFileDownloadOptions().setRange(new ShareFileRange(sourceOffset, sourceOffset + dst.remaining() - 1))
                    .setRequestConditions(conditions),
                null, null);

            updateResourceCachedState(response);

            return dst.position() - prevPosition;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateResourceCachedState(ShareFileDownloadResponse response) {
        String contentRange = response.getDeserializedHeaders().getContentRange();
        _lastKnownResourceLength = Long.parseLong(contentRange.split("/")[1]);
    }

    @Override
    public long getCachedLength() {
        return _lastKnownResourceLength;
    }
}
