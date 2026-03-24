// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.policy;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.implementation.contentvalidation.StorageCrc64Calculator;
import com.azure.storage.common.implementation.contentvalidation.StructuredMessageEncoder;
import com.azure.storage.common.implementation.contentvalidation.StructuredMessageFlags;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.Optional;

import static com.azure.storage.common.implementation.contentvalidation.StructuredMessageConstants.CONTENT_VALIDATION_BEHAVIOR_KEY;
import static com.azure.storage.common.implementation.contentvalidation.StructuredMessageConstants.USE_CRC64_CHECKSUM_HEADER_CONTEXT;
import static com.azure.storage.common.implementation.contentvalidation.StructuredMessageConstants.USE_STRUCTURED_MESSAGE_CONTEXT;
import static com.azure.storage.common.implementation.contentvalidation.StructuredMessageConstants.V1_DEFAULT_SEGMENT_CONTENT_LENGTH;
import static com.azure.storage.common.implementation.Constants.HeaderConstants.CONTENT_CRC64_HEADER_NAME;
import static com.azure.storage.common.implementation.Constants.HeaderConstants.STRUCTURED_BODY_TYPE_HEADER_NAME;
import static com.azure.storage.common.implementation.Constants.HeaderConstants.STRUCTURED_CONTENT_LENGTH_HEADER_NAME;
import static com.azure.storage.common.implementation.contentvalidation.StructuredMessageConstants.STRUCTURED_BODY_TYPE_VALUE;

/**
 * StorageContentValidationPolicy is a policy that applies content validation to the request body.
 */
public class StorageContentValidationPolicy implements HttpPipelinePolicy {
    private static final ClientLogger LOGGER = new ClientLogger(StorageContentValidationPolicy.class);


    /**
     * Creates a new instance of {@link StorageContentValidationPolicy}.
     */
    public StorageContentValidationPolicy() {
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        // Defer creating the next policy Mono until validation (and any required header mutations) has completed.
        // Some downstream policies may compute auth/signatures eagerly in their `process()` method.
        return applyContentValidation(context).then(Mono.defer(next::process));
    }

    /**
     * Applies content validation to the request body.
     *
     * @param context the HTTP pipeline call context
     * @return a {@link Mono} that completes when content validation has been applied to the request body.
     */
    private Mono<Void> applyContentValidation(HttpPipelineCallContext context) {
        Optional<Object> behaviorOptional = context.getContext().getData(CONTENT_VALIDATION_BEHAVIOR_KEY);
        if (!behaviorOptional.isPresent()) {
            return Mono.empty();
        }

        String contentValidationBehavior = behaviorOptional.get().toString();
        if (contentValidationBehavior.isEmpty()) {
            return Mono.empty();
        }

        Mono<Void> validation = Mono.empty();

        if (contentValidationBehavior.contains(USE_CRC64_CHECKSUM_HEADER_CONTEXT)) {
            validation = validation.then(applyCRC64Header(context));
        }
        if (contentValidationBehavior.contains(USE_STRUCTURED_MESSAGE_CONTEXT)) {
            validation = validation.then(applyStructuredMessage(context));
        }

        return validation;
    }

    /**
     * Applies the crc64 header to the request body.
     *
     * @param context the HTTP pipeline call context
     * @return a {@link Mono} that completes when the crc64 header has been applied to the request body.
     */
    private Mono<Void> applyCRC64Header(HttpPipelineCallContext context) {
        if (context.getHttpRequest().getBody() == null) {
            return Mono.empty();
        }

        // Collect request body bytes once, compute CRC64 off the reactive thread, and then restore the body
        // as a replayable Flux so downstream processing / sending still sees the expected content.
        Flux<ByteBuffer> originalBody = context.getHttpRequest().getBody();
        return FluxUtil.collectBytesInByteBufferStream(originalBody)
            .flatMap(originalBytes -> Mono.fromCallable(() -> StorageCrc64Calculator.compute(originalBytes, 0))
                .subscribeOn(Schedulers.boundedElastic())
                .doOnNext(contentCRC64 -> {
                    // Restore body for downstream consumers.
                    context.getHttpRequest().setBody(Flux.just(ByteBuffer.wrap(originalBytes)));

                    // Convert the 64-bit CRC value to 8 bytes in little-endian format.
                    byte[] crc64Bytes = new byte[8];
                    for (int i = 0; i < 8; i++) {
                        crc64Bytes[i] = (byte) (contentCRC64 >>> (i * 8));
                    }

                    // Base64 encode the binary representation.
                    String encodedCRC64 = Base64.getEncoder().encodeToString(crc64Bytes);
                    context.getHttpRequest().setHeader(CONTENT_CRC64_HEADER_NAME, encodedCRC64);
                })
                .then());
    }

    /**
     * Applies the structured message to the request body.
     *
     * @param context the HTTP pipeline call context
     */
    private Mono<Void> applyStructuredMessage(HttpPipelineCallContext context) {
        String contentLengthValue = context.getHttpRequest().getHeaders().getValue(HttpHeaderName.CONTENT_LENGTH);
        if (contentLengthValue == null || contentLengthValue.isEmpty()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Content-Length header is required to apply structured message "
                + "and CRC64 encoding, but it was not present on the request."));
        }

        long parsedContentLength;
        try {
            parsedContentLength = Long.parseLong(contentLengthValue);
        } catch (NumberFormatException ex) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                "Content-Length header value '" + contentLengthValue
                    + "' is not a valid non-negative integer value required for structured message and CRC64 encoding.",
                ex));
        }

        int unencodedContentLength = (int) parsedContentLength;

        Flux<ByteBuffer> originalBody = context.getHttpRequest().getBody();

        /*
         * Replace the request body with a structured message: raw content wrapped with headers, segment
         * boundaries, and CRC64 checksums so the service can validate integrity as it receives the stream.
         *
         * A fresh encoder is created on each subscribe (via defer) so retries re-encode correctly from the
         * original replayable body. The encoded buffers are slices of the original data, produced lazily and
         * consumed by the HTTP client without materialization.
         *
         * limitRate(1) keeps the encoder's segment boundaries aligned with buffer boundaries.
         */
        Flux<ByteBuffer> encodedBody = Flux.defer(() -> {
            StructuredMessageEncoder encoder = new StructuredMessageEncoder(unencodedContentLength,
                V1_DEFAULT_SEGMENT_CONTENT_LENGTH, StructuredMessageFlags.STORAGE_CRC64);
            return Flux.from(originalBody).limitRate(1).concatMap(encoder::encode);
        });

        context.getHttpRequest().setBody(encodedBody);

        long encodedLength = new StructuredMessageEncoder(unencodedContentLength, V1_DEFAULT_SEGMENT_CONTENT_LENGTH,
            StructuredMessageFlags.STORAGE_CRC64).getEncodedMessageLength();
        context.getHttpRequest().setHeader(HttpHeaderName.CONTENT_LENGTH, String.valueOf(encodedLength));
        context.getHttpRequest().setHeader(STRUCTURED_BODY_TYPE_HEADER_NAME, STRUCTURED_BODY_TYPE_VALUE);
        context.getHttpRequest()
            .setHeader(STRUCTURED_CONTENT_LENGTH_HEADER_NAME, String.valueOf(unencodedContentLength));

        return Mono.empty();
    }

}
