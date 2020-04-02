package com.azure.storage.blob.changefeed.implementation.util;

import com.azure.storage.blob.BlobContainerAsyncClient;
import reactor.core.publisher.Mono;

public class DownloadUtils {

    /**
     * Reduces a Flux of ByteBuffer into a Mono<String>
     */
    public static Mono<String> downloadToString(BlobContainerAsyncClient client, String blobPath) {
        return client.getBlobAsyncClient(blobPath)
            .download()
            .reduce(new StringBuilder(), (sb, buffer) -> {
                sb.append(new String(buffer.array()));
                return sb;
            }).map(StringBuilder::toString);
    }
}
