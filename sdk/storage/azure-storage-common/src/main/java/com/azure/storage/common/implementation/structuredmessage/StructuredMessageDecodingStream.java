// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation.structuredmessage;

import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.DownloadContentValidationOptions;
import reactor.core.publisher.Flux;

import java.nio.ByteBuffer;

/**
 * A utility class for applying structured message decoding to download streams.
 */
public final class StructuredMessageDecodingStream {
    private static final ClientLogger LOGGER = new ClientLogger(StructuredMessageDecodingStream.class);

    private StructuredMessageDecodingStream() {
        // utility class
    }

    /**
     * Wraps a download stream with structured message decoding if content validation is enabled.
     *
     * @param originalStream The original download stream.
     * @param contentLength The expected content length.
     * @param validationOptions The content validation options.
     * @return A Flux that decodes structured messages if validation is enabled, otherwise returns the original stream.
     */
    public static Flux<ByteBuffer> wrapStreamIfNeeded(Flux<ByteBuffer> originalStream, Long contentLength,
        DownloadContentValidationOptions validationOptions) {

        if (validationOptions == null || !validationOptions.isStructuredMessageValidationEnabled()) {
            return originalStream;
        }

        if (contentLength == null || contentLength <= 0) {
            LOGGER.warning("Cannot apply structured message validation without valid content length.");
            return originalStream;
        }

        return applyStructuredMessageDecoding(originalStream, contentLength);
    }

    /**
     * Applies structured message decoding to the stream.
     *
     * @param stream The stream to decode.
     * @param expectedContentLength The expected content length.
     * @return A Flux that decodes the structured message.
     */
    private static Flux<ByteBuffer> applyStructuredMessageDecoding(Flux<ByteBuffer> stream,
        long expectedContentLength) {
        return stream
            .collect(() -> new StructuredMessageDecodingCollector(expectedContentLength),
                StructuredMessageDecodingCollector::addBuffer)
            .flatMapMany(collector -> collector.getDecodedData());
    }

    /**
     * Helper class to collect and decode structured message data.
     */
    private static class StructuredMessageDecodingCollector {
        private final StructuredMessageDecoder decoder;
        private ByteBuffer accumulatedBuffer;
        private boolean completed = false;

        StructuredMessageDecodingCollector(long expectedContentLength) {
            this.decoder = new StructuredMessageDecoder(expectedContentLength);
            this.accumulatedBuffer = ByteBuffer.allocate(0);
        }

        void addBuffer(ByteBuffer buffer) {
            if (completed) {
                return;
            }

            // Accumulate the buffer
            ByteBuffer newBuffer = ByteBuffer.allocate(accumulatedBuffer.remaining() + buffer.remaining());
            newBuffer.put(accumulatedBuffer);
            newBuffer.put(buffer);
            newBuffer.flip();
            accumulatedBuffer = newBuffer;
        }

        Flux<ByteBuffer> getDecodedData() {
            try {
                if (accumulatedBuffer.remaining() == 0) {
                    return Flux.empty();
                }

                ByteBuffer decodedData = decoder.decode(accumulatedBuffer);
                decoder.finalizeDecoding();
                completed = true;

                return Flux.just(decodedData);
            } catch (Exception e) {
                LOGGER.error("Failed to decode structured message: " + e.getMessage(), e);
                return Flux.error(e);
            }
        }
    }
}
