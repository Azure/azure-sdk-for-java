// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.agents.implementation.utils;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.FluxUtil;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;

/**
 * Internal utilities for file operations.
 */
public class FileUtils {

    /**
     * Writes to file.
     *
     * @param content response content to write to file.
     * @param destinationFile destination file path.
     * @return a {@link Mono} that completes when the file has been written.
     */
    public static Mono<Void> writeToFile(BinaryData content, Path destinationFile) {
        // file handling is done asynchronously. Errors either opening or closing the file are bubbled up to the subscriber
        return Mono.usingWhen(
            Mono.fromCallable(() -> AsynchronousFileChannel.open(destinationFile)),
            channel -> FluxUtil.writeFile(
                content.toFluxByteBuffer(),
                channel),
            channel -> Mono.defer(() -> {
                try {
                    channel.close();
                    return Mono.empty();
                } catch (IOException e) {
                    return Mono.error(e);
                }
            })
        );
    }

    /**
     * Writes to file and returns a simplified response.
     *
     * @param response request response and its content.
     * @param destinationFile destination file path
     * @return a {@link Mono} that completes when the file has been written and the request {@link Response}
     */
    public static Mono<Response<Void>> writeToFileWithResponse(Response<BinaryData> response, Path destinationFile) {
        return writeToFile(response.getValue(), destinationFile)
            .thenReturn(new SimpleResponse<>(
                response.getRequest(),
                response.getStatusCode(),
                response.getHeaders(),
                null)
        );
    }
}
