// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.Utils;
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

public class JsonNodeStorePayload implements StorePayload<JsonNode> {
    private static final Logger logger = LoggerFactory.getLogger(JsonNodeStorePayload.class);
    private static final CharsetDecoder fallbackCharsetDecoder = getFallbackCharsetDecoder();
    private final int responsePayloadSize;
    private final JsonNode jsonValue;

    public JsonNodeStorePayload(ByteBufInputStream bufferStream, int readableBytes) {
        if (readableBytes > 0) {
            this.responsePayloadSize = readableBytes;
            this.jsonValue = fromJson(bufferStream, readableBytes);
        } else {
            this.responsePayloadSize = 0;
            this.jsonValue = null;
        }
    }

    private static JsonNode fromJson(ByteBufInputStream bufferStream, int readableBytes) {
        byte[] bytes = new byte[readableBytes];
        try {
            bufferStream.read(bytes);
            return Utils.getSimpleObjectMapper().readTree(bytes);
        } catch (IOException e) {
            if (fallbackCharsetDecoder != null) {
                logger.warn("Unable to parse JSON, fallback to use customized charset decoder.", e);
                return fromJsonWithFallbackCharsetDecoder(bytes);
            } else {

                if (Configs.isNonParseableDocumentLoggingEnabled()) {
                    String documentSample = Base64.getEncoder().encodeToString(bytes);
                    logger.error("Failed to parse JSON document. No customized charset decoder configured. Document in Base64 format: [" + documentSample + "]", e);
                } else {
                    logger.error("Failed to parse JSON document. No customized charset decoder configured.");
                }

                throw new IllegalStateException("Unable to parse JSON.", e);
            }
        }
    }

    private static JsonNode fromJsonWithFallbackCharsetDecoder(byte[] bytes) {
        try {
            String sanitizedJson = fallbackCharsetDecoder.decode(ByteBuffer.wrap(bytes)).toString();
            return Utils.getSimpleObjectMapper().readTree(sanitizedJson);
        } catch (IOException e) {

            if (Configs.isNonParseableDocumentLoggingEnabled()) {
                String documentSample = Base64.getEncoder().encodeToString(bytes);
                logger.error("Failed to parse JSON document even after applying fallback charset decoder. Document in Base64 format: [" + documentSample + "]", e);
            } else {
                logger.error("Failed to parse JSON document even after applying fallback charset decoder.");
            }

            throw new IllegalStateException(
                String.format(
                    "Unable to parse JSON with fallback charset decoder[OnMalformedInput %s, OnUnmappedCharacter %s]",
                    Configs.getCharsetDecoderErrorActionOnMalformedInput(),
                    Configs.getCharsetDecoderErrorActionOnUnmappedCharacter()),
                e);
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
}
