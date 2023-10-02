// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.util.serializer;

import com.typespec.core.http.HttpHeaderName;
import com.typespec.core.http.HttpHeaders;
import com.typespec.core.util.CoreUtils;
import com.typespec.core.util.logging.ClientLogger;

import java.util.Map;
import java.util.TreeMap;

/**
 * Supported serialization encoding formats.
 */
public enum SerializerEncoding {
    /**
     * JavaScript Object Notation.
     */
    JSON,

    /**
     * Extensible Markup Language.
     */
    XML,

    /**
     * Text.
     */
    TEXT;

    private static final ClientLogger LOGGER = new ClientLogger(SerializerEncoding.class);
    private static final String CONTENT_TYPE = "Content-Type";
    private static final Map<String, SerializerEncoding> SUPPORTED_MIME_TYPES;
    private static final SerializerEncoding DEFAULT_ENCODING = JSON;


    static {
        // Encodings and suffixes from: https://tools.ietf.org/html/rfc6838
        SUPPORTED_MIME_TYPES = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        SUPPORTED_MIME_TYPES.put("text/xml", XML);
        SUPPORTED_MIME_TYPES.put("application/xml", XML);
        SUPPORTED_MIME_TYPES.put("application/json", JSON);
        SUPPORTED_MIME_TYPES.put("text/css", TEXT);
        SUPPORTED_MIME_TYPES.put("text/csv", TEXT);
        SUPPORTED_MIME_TYPES.put("text/html", TEXT);
        SUPPORTED_MIME_TYPES.put("text/javascript", TEXT);
        SUPPORTED_MIME_TYPES.put("text/plain", TEXT);
    }

    /**
     * Determines the serializer encoding to use based on the Content-Type header.
     *
     * @param headers the headers to check the encoding for.
     * @return the serializer encoding to use for the body. {@link #JSON} if there is no Content-Type header or an
     * unrecognized Content-Type encoding is returned.
     */
    public static SerializerEncoding fromHeaders(HttpHeaders headers) {
        final String mimeContentType = headers.getValue(HttpHeaderName.CONTENT_TYPE);
        if (CoreUtils.isNullOrEmpty(mimeContentType)) {
            LOGGER.warning("'{}' not found. Returning default encoding: {}", CONTENT_TYPE, DEFAULT_ENCODING);
            return DEFAULT_ENCODING;
        }

        int contentTypeEnd = mimeContentType.indexOf(';');
        String contentType = (contentTypeEnd == -1) ? mimeContentType : mimeContentType.substring(0, contentTypeEnd);
        final SerializerEncoding encoding = SUPPORTED_MIME_TYPES.get(contentType);
        if (encoding != null) {
            return encoding;
        }

        int contentTypeTypeSplit = contentType.indexOf('/');
        if (contentTypeTypeSplit == -1) {
            LOGGER.warning("Content-Type '{}' does not match mime-type formatting 'type'/'subtype'. "
                + "Returning default: {}", contentType, DEFAULT_ENCODING);
            return DEFAULT_ENCODING;
        }

        // Check the suffix if it does not match the full types.
        // Suffixes are defined by the Structured Syntax Suffix Registry
        // https://www.rfc-editor.org/rfc/rfc6839
        final String subtype = contentType.substring(contentTypeTypeSplit + 1);
        final int lastIndex = subtype.lastIndexOf('+');
        if (lastIndex == -1) {
            return DEFAULT_ENCODING;
        }

        // Only XML and JSON are supported suffixes, there is no suffix for TEXT.
        final String mimeTypeSuffix = subtype.substring(lastIndex + 1);
        if ("xml".equalsIgnoreCase(mimeTypeSuffix)) {
            return XML;
        } else if ("json".equalsIgnoreCase(mimeTypeSuffix)) {
            return JSON;
        }

        LOGGER.warning("Content-Type '{}' does not match any supported one. Returning default: {}",
            mimeContentType, DEFAULT_ENCODING);

        return DEFAULT_ENCODING;
    }
}
