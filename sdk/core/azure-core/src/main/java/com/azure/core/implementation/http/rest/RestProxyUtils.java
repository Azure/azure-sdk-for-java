// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.http.rest;

import com.azure.core.exception.UnexpectedLengthException;
import com.azure.core.http.HttpRequest;
import com.azure.core.implementation.util.BinaryDataContent;
import com.azure.core.implementation.util.BinaryDataHelper;
import com.azure.core.implementation.util.FluxByteBufferContent;
import com.azure.core.implementation.util.InputStreamContent;
import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Utility methods that aid processing in RestProxy.
 */
public final class RestProxyUtils {
    private static final ClientLogger LOGGER = new ClientLogger(RestProxyUtils.class);
    private static final ByteBuffer VALIDATION_BUFFER = ByteBuffer.allocate(0);
    public static final String BODY_TOO_LARGE = "Request body emitted %d bytes, more than the expected %d bytes.";
    public static final String BODY_TOO_SMALL = "Request body emitted %d bytes, less than the expected %d bytes.";

    private RestProxyUtils() {
    }

    public static Mono<HttpRequest> validateLengthAsync(final HttpRequest request) {
        final BinaryData body = request.getBodyAsBinaryData();

        if (body == null) {
            return Mono.just(request);
        }

        return Mono.fromCallable(() -> {
            BinaryDataContent content = BinaryDataHelper.getContent(body);
            long expectedLength = Long.parseLong(request.getHeaders().getValue("Content-Length"));
            if (content instanceof InputStreamContent) {
                InputStream validatingInputStream = new LengthValidatingInputStream(
                    content.toStream(), expectedLength);
                request.setBody(BinaryData.fromStream(validatingInputStream));
            } else if (content instanceof FluxByteBufferContent) {
                request.setBody(validateFluxLength(body.toFluxByteBuffer(), expectedLength));
            } else {
                Long bodyLength = body.getLength();
                if (bodyLength != null) {
                    if (bodyLength < expectedLength) {
                        throw new UnexpectedLengthException(String.format(BODY_TOO_SMALL,
                            bodyLength, expectedLength), bodyLength, expectedLength);
                    } else if (bodyLength > expectedLength) {
                        throw new UnexpectedLengthException(String.format(BODY_TOO_LARGE,
                            bodyLength, expectedLength), bodyLength, expectedLength);
                    }
                } else  {
                    request.setBody(validateFluxLength(body.toFluxByteBuffer(), expectedLength));
                }
            }

            return request;
        });
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
