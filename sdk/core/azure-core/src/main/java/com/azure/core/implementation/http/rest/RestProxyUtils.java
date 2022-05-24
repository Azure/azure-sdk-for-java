// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.http.rest;

import com.azure.core.exception.UnexpectedLengthException;
import com.azure.core.http.HttpRequest;
import reactor.core.publisher.Flux;

import java.nio.ByteBuffer;

/**
 * Utility methods that aid processing in RestProxy.
 */
public final class RestProxyUtils {

    private static final ByteBuffer VALIDATION_BUFFER = ByteBuffer.allocate(0);
    private static final String BODY_TOO_LARGE = "Request body emitted %d bytes, more than the expected %d bytes.";
    private static final String BODY_TOO_SMALL = "Request body emitted %d bytes, less than the expected %d bytes.";

    private RestProxyUtils() {
    }

    public static void validateLength(final HttpRequest request) {
        final Flux<ByteBuffer> bbFlux = request.getBody();
        if (bbFlux != null) {
            final long expectedLength = Long.parseLong(request.getHeaders().getValue("Content-Length"));
            request.setBody(validateFluxLength(bbFlux, expectedLength));
        }
    }

    private static Flux<ByteBuffer> validateFluxLength(Flux<ByteBuffer> bbFlux, long expectedLength) {
        if (bbFlux == null) {
            return Flux.empty();
        }

        return Flux.defer(() -> {
            final long[] currentTotalLength = new long[1];
            return Flux.concat(bbFlux, Flux.just(VALIDATION_BUFFER)).handle((buffer, sink) -> {
                if (buffer == null) {
                    return;
                }

                if (buffer == VALIDATION_BUFFER) {
                    if (expectedLength != currentTotalLength[0]) {
                        sink.error(new UnexpectedLengthException(String.format(BODY_TOO_SMALL,
                            currentTotalLength[0], expectedLength), currentTotalLength[0], expectedLength));
                    } else {
                        sink.complete();
                    }
                    return;
                }

                currentTotalLength[0] += buffer.remaining();
                if (currentTotalLength[0] > expectedLength) {
                    sink.error(new UnexpectedLengthException(String.format(BODY_TOO_LARGE,
                        currentTotalLength[0], expectedLength), currentTotalLength[0], expectedLength));
                    return;
                }

                sink.next(buffer);
            });
        });
    }
}
