// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.util;

import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.instrumentation.logging.LoggingEvent;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.nio.ByteBuffer;

/**
 * Utility type exposing methods to deal with {@link Flux}.
 */
public final class FluxUtil {
    /**
     * Gets the content of the provided ByteBuffer as a byte array. This method will create a new byte array even if the
     * ByteBuffer can have optionally backing array.
     *
     * @param byteBuffer the byte buffer
     * @return the byte array
     */
    public static byte[] byteBufferToArray(ByteBuffer byteBuffer) {
        int length = byteBuffer.remaining();
        byte[] byteArray = new byte[length];
        byteBuffer.get(byteArray);
        return byteArray;
    }

    /**
     * Propagates a {@link RuntimeException} through the error channel of {@link Mono}.
     *
     * @param logger The {@link ClientLogger} to log the exception.
     * @param ex The {@link RuntimeException}.
     * @param <T> The return type.
     * @return A {@link Mono} that terminates with error wrapping the {@link RuntimeException}.
     */
    public static <T> Mono<T> monoError(ClientLogger logger, RuntimeException ex) {
        return Mono.error(logger.atError().log(Exceptions.propagate(ex)));
    }

    /**
     * Propagates a {@link RuntimeException} through the error channel of {@link Mono}.
     *
     * @param logBuilder The {@link LoggingEvent} with context to log the exception.
     * @param ex The {@link RuntimeException}.
     * @param <T> The return type.
     * @return A {@link Mono} that terminates with error wrapping the {@link RuntimeException}.
     */
    public static <T> Mono<T> monoError(LoggingEvent logBuilder, RuntimeException ex) {
        return Mono.error(logBuilder.log(Exceptions.propagate(ex)));
    }

    /**
     * Propagates a {@link RuntimeException} through the error channel of {@link Flux}.
     *
     * @param logger The {@link ClientLogger} to log the exception.
     * @param ex The {@link RuntimeException}.
     * @param <T> The return type.
     * @return A {@link Flux} that terminates with error wrapping the {@link RuntimeException}.
     */
    public static <T> Flux<T> fluxError(ClientLogger logger, RuntimeException ex) {
        return Flux.error(logger.atError().log(Exceptions.propagate(ex)));
    }

    // Private Ctr
    private FluxUtil() {
    }
}
