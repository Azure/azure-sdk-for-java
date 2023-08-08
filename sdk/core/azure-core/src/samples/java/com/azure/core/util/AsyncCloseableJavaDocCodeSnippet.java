// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

/**
 * Code snippets for {@link AsyncCloseable}.
 */
public class AsyncCloseableJavaDocCodeSnippet {
    public void asyncResource() throws IOException {
        // BEGIN: com.azure.core.util.AsyncCloseable.closeAsync
        NetworkResource resource = new NetworkResource();
        resource.longRunningDownload("https://longdownload.com")
            .subscribe(
                byteBuffer -> System.out.println("Buffer received: " + byteBuffer),
                error -> System.err.printf("Error occurred while downloading: %s%n", error),
                () -> System.out.println("Completed download operation."));

        System.out.println("Press enter to stop downloading.");
        System.in.read();

        // We block here because it is the end of the main Program function. A real-life program may chain this
        // with some other close operations like save download/program state, etc.
        resource.closeAsync().block();
        // END: com.azure.core.util.AsyncCloseable.closeAsync
    }

    /**
     * A long lived network resource.
     */
    static class NetworkResource implements AsyncCloseable {
        private final AtomicBoolean isClosed = new AtomicBoolean();
        private final Sinks.Empty<Void> closeMono = Sinks.empty();

        /**
         * Downloads a resource.
         *
         * @param url URL for the download.
         *
         * @return A stream of bytes.
         */
        Flux<ByteBuffer> longRunningDownload(String url) {
            final byte[] bytes = url.getBytes(StandardCharsets.UTF_8);

            // Does nothing real but it represents taking from this possibly infinite Flux until
            // the closeMono emits a signal.
            return Flux.fromStream(IntStream.range(0, bytes.length)
                .mapToObj(index -> ByteBuffer.wrap(bytes)))
                .takeUntilOther(closeMono.asMono());
        }

        @Override
        public Mono<Void> closeAsync() {
            // If the close operation has started, then
            if (isClosed.getAndSet(true)) {
                return closeMono.asMono();
            }

            return startAsyncClose().then(closeMono.asMono());
        }

        private Mono<Void> startAsyncClose() {
            return Mono.delay(Duration.ofSeconds(10)).then()
                .doOnError(error -> closeMono.emitError(error, Sinks.EmitFailureHandler.FAIL_FAST))
                .doOnSuccess(unused -> closeMono.emitEmpty(Sinks.EmitFailureHandler.FAIL_FAST));
        }
    }
}
