// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.policy;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.storage.common.implementation.BufferAggregator;
import com.azure.storage.common.implementation.BufferStagingArea;
import com.azure.storage.common.implementation.StorageCrc64Calculator;
import com.azure.storage.common.implementation.structuredmessage.StructuredMessageEncoder;
import com.azure.storage.common.implementation.structuredmessage.StructuredMessageFlags;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.Base64;

import static com.azure.storage.common.implementation.Constants.CONTENT_VALIDATION_BEHAVIOR_KEY;
import static com.azure.storage.common.implementation.Constants.HeaderConstants.CONTENT_CRC64_HEADER_NAME;
import static com.azure.storage.common.implementation.Constants.HeaderConstants.STRUCTURED_BODY_TYPE_HEADER_NAME;
import static com.azure.storage.common.implementation.Constants.HeaderConstants.STRUCTURED_CONTENT_LENGTH_HEADER_NAME;
import static com.azure.storage.common.implementation.Constants.USE_CRC64_CHECKSUM_HEADER_CONTEXT;
import static com.azure.storage.common.implementation.Constants.USE_STRUCTURED_MESSAGE_CONTEXT;
import static com.azure.storage.common.implementation.structuredmessage.StructuredMessageConstants.STATIC_MAXIMUM_ENCODED_DATA_LENGTH;
import static com.azure.storage.common.implementation.structuredmessage.StructuredMessageConstants.STRUCTURED_BODY_TYPE_VALUE;
import static com.azure.storage.common.implementation.structuredmessage.StructuredMessageConstants.V1_DEFAULT_SEGMENT_CONTENT_LENGTH;

/**
 * A policy that applies structured message to the body of a request, or applies the crc64 header to the request.
 * Also, can be used for the response eventually.
 */
public class StorageContentValidationPolicy implements HttpPipelinePolicy {

    /**
     * Creates a new instance of {@link StorageContentValidationPolicy}.
     */
    public StorageContentValidationPolicy() {
    }

    /**
     * stuff
     *
     * @return stuff
     */
    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        applyContentValidation(context);
        return next.processSync();
    }

    /**
     * Stuff
     *
     * @return stuff
     */
    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        applyContentValidation(context);
        return next.process();

        //        long messageCRC64 = applyContentValidation(context);
        //        return next.process().map(response -> {
        //            if (messageCRC64 != -1) {
        //                response.getHeaders().add("test_context_key", String.valueOf(messageCRC64));
        //            }
        //            return response;
        //        });
    }

    private void applyContentValidation(HttpPipelineCallContext context) {
        String contentValidationBehavior = context.getContext().getData(CONTENT_VALIDATION_BEHAVIOR_KEY).toString();
        if (contentValidationBehavior.contains(USE_CRC64_CHECKSUM_HEADER_CONTEXT)) {
            applyCRC64Header(context);
        } else if (contentValidationBehavior.contains(USE_STRUCTURED_MESSAGE_CONTEXT)) {
            applyStructuredMessage(context);
        }
    }

    private void applyCRC64Header(HttpPipelineCallContext context) {
        // Implementation for setting the crc64 header
        long contentCRC64 = StorageCrc64Calculator.compute(context.getHttpRequest().getBodyAsBinaryData().toBytes(), 0);

        // Convert the 64-bit CRC value to 8 bytes in little-endian format
        byte[] crc64Bytes = new byte[8];
        for (int i = 0; i < 8; i++) {
            crc64Bytes[i] = (byte) (contentCRC64 >>> (i * 8));
        }

        // Base64 encode the binary representation
        String encodedCRC64 = Base64.getEncoder().encodeToString(crc64Bytes);
        context.getHttpRequest().setHeader(CONTENT_CRC64_HEADER_NAME, encodedCRC64);
    }

    private void applyStructuredMessage(HttpPipelineCallContext context) {
        // Implementation for applying structured message to the request body
        int unencodedContentLength
            = Integer.parseInt(context.getHttpRequest().getHeaders().getValue(HttpHeaderName.CONTENT_LENGTH));

        StructuredMessageEncoder structuredMessageEncoder = new StructuredMessageEncoder(unencodedContentLength,
            V1_DEFAULT_SEGMENT_CONTENT_LENGTH, StructuredMessageFlags.STORAGE_CRC64);

        // Create BufferStagingArea with 4MB chunks
        BufferStagingArea stagingArea
            = new BufferStagingArea(STATIC_MAXIMUM_ENCODED_DATA_LENGTH, STATIC_MAXIMUM_ENCODED_DATA_LENGTH);

        Flux<ByteBuffer> encodedBody = context.getHttpRequest()
            .getBody()
            .flatMapSequential(stagingArea::write, 1, 1)
            .concatWith(Flux.defer(stagingArea::flush))
            .flatMap(bufferAggregator -> bufferAggregator.asFlux().flatMap(structuredMessageEncoder::encode));

        // Set the encoded body
        context.getHttpRequest().setBody(encodedBody);

        context.getHttpRequest()
            .setHeader(HttpHeaderName.CONTENT_LENGTH,
                String.valueOf(structuredMessageEncoder.getEncodedMessageLength()));
        // x-ms-structured-body
        context.getHttpRequest().setHeader(STRUCTURED_BODY_TYPE_HEADER_NAME, STRUCTURED_BODY_TYPE_VALUE);
        // x-ms-structured-content-length
        context.getHttpRequest()
            .setHeader(STRUCTURED_CONTENT_LENGTH_HEADER_NAME, String.valueOf(unencodedContentLength));
        //return structuredMessageEncoder.getMessageCRC64();
    }

}
