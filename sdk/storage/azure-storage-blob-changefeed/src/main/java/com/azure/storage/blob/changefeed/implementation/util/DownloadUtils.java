// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed.implementation.util;

import com.azure.core.util.FluxUtil;
import com.azure.storage.blob.BlobContainerAsyncClient;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

public class DownloadUtils {

    /**
     * Reduces a Flux of ByteBuffer into a Mono of String
     */
    public static Mono<String> downloadToString(BlobContainerAsyncClient client, String blobPath) {
        return client.getBlobAsyncClient(blobPath)
            .download()
            .reduce(new StringBuilder(), (sb, buffer) -> {
                sb.append(new String(FluxUtil.byteBufferToArray(buffer), StandardCharsets.UTF_8));
                return sb;
            }).map(StringBuilder::toString);
    }
}
