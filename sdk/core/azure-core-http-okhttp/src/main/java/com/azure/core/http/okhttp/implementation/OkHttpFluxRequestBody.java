// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.okhttp.implementation;

import com.azure.core.implementation.util.BinaryDataContent;
import com.azure.core.util.logging.ClientLogger;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okio.BufferedSink;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * An {@link okhttp3.RequestBody} subtype that sends {@link BinaryDataContent}
 * as {@link Flux} of {@link ByteBuffer} in an unbuffered manner.
 * This class accepts any {@link BinaryDataContent} as catch-all for backwards compatibility
 * but ideally should be used only with reactive payloads.
 */
public class OkHttpFluxRequestBody extends OkHttpStreamableRequestBody<BinaryDataContent> {

    private static final ClientLogger LOGGER = new ClientLogger(OkHttpFluxRequestBody.class);

    private final AtomicBoolean bodySent = new AtomicBoolean(false);
    private final OkHttpClient okHttpClient;

    public OkHttpFluxRequestBody(
        BinaryDataContent content, long effectiveContentLength, MediaType mediaType, OkHttpClient okHttpClient) {
        super(content, effectiveContentLength, mediaType);
        this.okHttpClient = Objects.requireNonNull(okHttpClient, "'okHttpClient' cannot be null.");
    }

    @Override
    public void writeTo(BufferedSink bufferedSink) throws IOException {
        if (bodySent.compareAndSet(false, true)) {
            Mono<BufferedSink> requestSendMono = content.toFluxByteBuffer()
                .publishOn(Schedulers.boundedElastic())
                .reduce(bufferedSink, (sink, buffer) -> {
                    try {
                        while (buffer.hasRemaining()) {
                            sink.write(buffer);
                        }
                        return sink;
                    } catch (IOException e) {
                        throw Exceptions.propagate(e);
                    }
                });

            // The blocking happens on OkHttp thread pool.
            int callTimeoutMillis = okHttpClient.callTimeoutMillis();
            if (callTimeoutMillis > 0) {
                /*
                 * Default call timeout (in milliseconds). By default there is no timeout for complete calls, but
                 * there is for the connect, write, and read actions within a call.
                 */
                requestSendMono.block(Duration.ofMillis(callTimeoutMillis));
            } else {
                requestSendMono.block();
            }
        } else {
            // Prevent OkHttp from potentially re-sending non-repeatable body outside of retry policies.
            throw LOGGER.logThrowableAsError(new IOException("Re-attempt to send Flux body is not supported"));
        }
    }
}
