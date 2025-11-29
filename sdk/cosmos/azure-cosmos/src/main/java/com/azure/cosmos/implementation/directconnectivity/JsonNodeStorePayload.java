// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.Utils;
import com.fasterxml.jackson.core.exc.StreamConstraintsException;
import com.fasterxml.jackson.databind.JsonNode;
import io.netty.buffer.ByteBufInputStream;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class JsonNodeStorePayload implements StorePayload<JsonNode> {
    private static final Logger logger = LoggerFactory.getLogger(JsonNodeStorePayload.class);
    private static final CharsetDecoder fallbackCharsetDecoder = getFallbackCharsetDecoder();

    // Test-only interceptor for fault injection
    // Using AtomicReference for thread-safe global interceptor
    // This is a test-only feature and tests should ensure proper isolation through test orchestration
    private static final AtomicReference<TestOnlyJsonParseInterceptor> globalTestInterceptor = new AtomicReference<>();

    private final int responsePayloadSize;
    private final JsonNode jsonValue;

    public JsonNodeStorePayload(
        ByteBufInputStream bufferStream,
        int readableBytes,
        Map<String, String> responseHeaders,
        OperationType operationType,
        ResourceType resourceType) {

        if (readableBytes > 0) {
            this.responsePayloadSize = readableBytes;
            this.jsonValue = fromJson(bufferStream, readableBytes, responseHeaders, operationType, resourceType);
        } else {
            this.responsePayloadSize = 0;
            this.jsonValue = null;
        }
    }

    private static JsonNode fromJson(ByteBufInputStream bufferStream, int readableBytes, Map<String, String> responseHeaders,
                                     OperationType operationType, ResourceType resourceType) {
        byte[] bytes = new byte[readableBytes];
        try {
            bufferStream.read(bytes);

            // Allow test-only interceptor to inject faults before parsing
            TestOnlyJsonParseInterceptor interceptor = globalTestInterceptor.get();
            if (interceptor != null) {
                return interceptor.intercept(bytes, responseHeaders,
                    (b, h) -> fromJsonWithBytes(b, h), operationType, resourceType);
            }

            return fromJsonWithBytes(bytes, responseHeaders);
        } catch (IOException e) {
            // IOException from read operation
            String baseErrorMessage = "Failed to read JSON document from stream.";
            logger.error(baseErrorMessage, e);

            IllegalStateException innerException = new IllegalStateException("Unable to read JSON stream.", e);

            throw Utils.createCosmosException(
                HttpConstants.StatusCodes.BADREQUEST,
                evaluateSubStatusCode(e),
                innerException,
                responseHeaders);
        }
    }

    private static JsonNode fromJsonWithBytes(byte[] bytes, Map<String, String> responseHeaders) throws IOException {
        try {
            return Utils.getSimpleObjectMapper().readTree(bytes);
        } catch (IOException e) {
            if (fallbackCharsetDecoder != null) {
                logger.warn("Unable to parse JSON, fallback to use customized charset decoder.", e);
                return fromJsonWithFallbackCharsetDecoder(bytes, responseHeaders);
            } else {

                String baseErrorMessage = "Failed to parse JSON document. No fallback charset decoder configured.";

                if (Configs.isNonParseableDocumentLoggingEnabled()) {
                    String documentSample = Base64.getEncoder().encodeToString(bytes);
                    logger.error(baseErrorMessage + " " + "Document in Base64 format: [" + documentSample + "]", e);
                } else {
                    logger.error(baseErrorMessage);
                }

                IllegalStateException innerException = new IllegalStateException("Unable to parse JSON.", e);

                throw Utils.createCosmosException(
                    HttpConstants.StatusCodes.BADREQUEST,
                    evaluateSubStatusCode(e),
                    innerException,
                    responseHeaders);
            }
        }
    }

    private static JsonNode fromJsonWithFallbackCharsetDecoder(byte[] bytes, Map<String, String> responseHeaders) {
        try {
            String sanitizedJson = fallbackCharsetDecoder.decode(ByteBuffer.wrap(bytes)).toString();
            return Utils.getSimpleObjectMapper().readTree(sanitizedJson);
        } catch (IOException e) {

            String baseErrorMessage = "Failed to parse JSON document even after applying fallback charset decoder.";

            if (Configs.isNonParseableDocumentLoggingEnabled()) {
                String documentSample = Base64.getEncoder().encodeToString(bytes);
                logger.error(baseErrorMessage + " " + "Document in Base64 format: [" + documentSample + "]", e);
            } else {
                logger.error(baseErrorMessage);
            }

            Exception nestedException = new IllegalStateException(
                String.format(
                    "Unable to parse JSON with fallback charset decoder[OnMalformedInput %s, OnUnmappedCharacter %s]",
                    Configs.getCharsetDecoderErrorActionOnMalformedInput(),
                    Configs.getCharsetDecoderErrorActionOnUnmappedCharacter()),
                e);

            throw Utils.createCosmosException(
                HttpConstants.StatusCodes.BADREQUEST,
                evaluateSubStatusCode(e),
                nestedException,
                responseHeaders);
        }
    }

    @Override
    public int getResponsePayloadSize() {
        return responsePayloadSize;
    }

    @Override
    public JsonNode getPayload() {
        return jsonValue;
    }

    private static CharsetDecoder getFallbackCharsetDecoder() {
        if (StringUtil.isNullOrEmpty(Configs.getCharsetDecoderErrorActionOnMalformedInput())
         && StringUtil.isNullOrEmpty(Configs.getCharsetDecoderErrorActionOnUnmappedCharacter())) {
            logger.debug("No fallback charset decoder is enabled");
            return null;
        }

        CharsetDecoder charsetDecoder = StandardCharsets.UTF_8.newDecoder();
        // config coding error action for malformed input
        switch (Configs.getCharsetDecoderErrorActionOnMalformedInput().toUpperCase()) {
            case "REPLACE":
                charsetDecoder.onMalformedInput(CodingErrorAction.REPLACE);
                break;
            case "IGNORE":
                charsetDecoder.onMalformedInput(CodingErrorAction.IGNORE);
                break;
            default:
                logger.warn(
                    "Will use default error action for malformed input config {}",
                    Configs.getCharsetDecoderErrorActionOnMalformedInput());
                break;
        }

        // config coding error action for unmapped character
        switch (Configs.getCharsetDecoderErrorActionOnUnmappedCharacter().toUpperCase()) {
            case "REPLACE":
                charsetDecoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
                break;
            case "IGNORE":
                charsetDecoder.onUnmappableCharacter(CodingErrorAction.IGNORE);
                break;
            default:
                logger.warn(
                    "Will use default error action for unmapped character config {}",
                    Configs.getCharsetDecoderErrorActionOnUnmappedCharacter());
                break;
        }

        return charsetDecoder;
    }

    private static int evaluateSubStatusCode(IOException exception) {

        if (exception instanceof IOException) {

            if (exception instanceof StreamConstraintsException) {
                return HttpConstants.SubStatusCodes.JACKSON_STREAMS_CONSTRAINED;
            }

            return HttpConstants.SubStatusCodes.FAILED_TO_PARSE_SERVER_RESPONSE;
        }

        return HttpConstants.SubStatusCodes.UNKNOWN;
    }

    /**
     * Test-only interceptor interface for fault injection.
     * WARNING: This is intended for testing purposes only and should not be used in production code.
     */
    @FunctionalInterface
    public interface TestOnlyJsonParseInterceptor {
        /**
         * Intercepts JSON parsing to allow fault injection.
         *
         * @param bytes the byte array containing JSON
         * @param responseHeaders the response headers
         * @param defaultParser the default parsing logic to delegate to
         * @return the parsed JsonNode
         * @throws IOException if parsing fails or fault is injected
         */
        JsonNode intercept(
            byte[] bytes,
            Map<String, String> responseHeaders,
            DefaultJsonParser defaultParser,
            OperationType operationType,
            ResourceType resourceType
        ) throws IOException;

        /**
         * Functional interface for the default JSON parsing logic.
         */
        @FunctionalInterface
        interface DefaultJsonParser {
            JsonNode parse(byte[] bytes, Map<String, String> responseHeaders) throws IOException;
        }
    }

    /**
     * Sets a test-only interceptor for JSON parsing globally.
     * WARNING: This is intended for testing purposes only and should not be used in production code.
     *
     * <p>This sets a GLOBAL interceptor that affects all threads. Tests using this interceptor
     * should NOT run in parallel with other tests to avoid interference. Use TestNG's
     * singleThreaded = true or similar mechanisms to ensure test isolation.</p>
     *
     * <p>The interceptor will be active across all threads including thread pool workers,
     * making it suitable for testing multi-threaded components like ChangeFeedProcessor.</p>
     *
     * @param interceptor the interceptor to set (null to clear)
     */
    public static void setTestOnlyJsonParseInterceptor(TestOnlyJsonParseInterceptor interceptor) {
        globalTestInterceptor.set(interceptor);
        if (interceptor != null) {
            logger.warn("GLOBAL test-only JSON parse interceptor has been set on thread {}. " +
                "This affects ALL threads and should only be used in isolated test scenarios. " +
                "Ensure tests using this do NOT run in parallel.",
                Thread.currentThread().getName());
        }
    }

    /**
     * Clears the test-only interceptor.
     */
    public static void clearTestOnlyJsonParseInterceptor() {
        globalTestInterceptor.set(null);
    }
}
