// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed.implementation.util;

import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.storage.blob.BlobContainerAsyncClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;

public class DownloadUtils {

    private static final ClientLogger LOGGER = new ClientLogger(DownloadUtils.class);

    /**
     * Reduces a Flux of ByteBuffer into a Mono of String
     */
    public static Mono<byte[]> downloadToByteArray(BlobContainerAsyncClient client, String blobPath) {
        return FluxUtil.collectBytesInByteBufferStream(client.getBlobAsyncClient(blobPath).download());
    }

    public static Mono<Map<String, Object>> parseJson(byte[] json) {
        try (JsonReader jsonReader = JsonProviders.createReader(json)) {
            return Mono.just(jsonReader.readMap(JsonReader::readUntyped));
        } catch (IOException e) {
            return FluxUtil.monoError(LOGGER, new UncheckedIOException(e));
        }
    }
}
