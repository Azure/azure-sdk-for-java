// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed.implementation.util;

import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobContainerAsyncClient;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

public class DownloadUtils {

    private static final ClientLogger LOGGER = new ClientLogger(DownloadUtils.class);

    /**
     * Reduces a Flux of ByteBuffer into a Mono of String
     */
    public static Mono<byte[]> downloadToByteArray(BlobContainerAsyncClient client, String blobPath) {
        return client.getBlobAsyncClient(blobPath)
            .download()
            .reduce(new ByteArrayOutputStream(), (os, buffer) -> {
                try {
                    os.write(FluxUtil.byteBufferToArray(buffer));
                } catch (IOException e) {
                    throw LOGGER.logExceptionAsError(new UncheckedIOException(e));
                }
                return os;
            }).map(ByteArrayOutputStream::toByteArray);
    }
}
