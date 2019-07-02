package com.azure.storage.blob;

import com.azure.storage.blob.models.ReliableDownloadOptions;
import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.io.OutputStream;

public class DownloadResponse {
    private final DownloadAsyncResponse asyncResponse;

    DownloadResponse(DownloadAsyncResponse asyncResponse) {
        this.asyncResponse = asyncResponse;
    }

    public void body(OutputStream outputStream, ReliableDownloadOptions options) throws IOException {
        for (ByteBuf buffer : this.asyncResponse.body(options).toIterable()) {
            buffer.readBytes(outputStream, buffer.readableBytes());
            buffer.release();
        }
    }
}
