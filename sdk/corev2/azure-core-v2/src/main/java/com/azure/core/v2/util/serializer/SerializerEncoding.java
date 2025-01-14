// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.util.serializer;

import com.azure.core.v2.util.CoreUtils;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.implementation.util.ImplUtils;
import io.clientcore.core.util.ClientLogger;

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
            LOGGER.atVerbose().log("'Content-Type' not found. Returning default encoding: JSON");
            return JSON;
        }

        int contentTypeEnd = mimeContentType.indexOf(';');
        String contentType = (contentTypeEnd == -1) ? mimeContentType : mimeContentType.substring(0, contentTypeEnd);
        SerializerEncoding encoding = checkForKnownEncoding(contentType);
        if (encoding != null) {
            return encoding;
        }

        int contentTypeTypeSplit = contentType.indexOf('/');
        if (contentTypeTypeSplit == -1) {
            LOGGER.atVerbose()
                .log("Content-Type '" + contentType + "' does not match mime-type formatting "
                    + "'type'/'subtype'. Returning default: JSON");
            return JSON;
        }

        // Check the suffix if it does not match the full types.
        // Suffixes are defined by the Structured Syntax Suffix Registry
        // https://www.rfc-editor.org/rfc/rfc6839
        final String subtype = contentType.substring(contentTypeTypeSplit + 1);
        final int lastIndex = subtype.lastIndexOf('+');
        if (lastIndex == -1) {
            return JSON;
        }

        // Only XML and JSON are supported suffixes, there is no suffix for TEXT.
        final String mimeTypeSuffix = subtype.substring(lastIndex + 1);
        if ("xml".equalsIgnoreCase(mimeTypeSuffix)) {
            return XML;
        } else if ("json".equalsIgnoreCase(mimeTypeSuffix)) {
            return JSON;
        }

        LOGGER.atVerbose()
            .log("Content-Type '" + mimeTypeSuffix + "' does not match any supported one. Returning default: JSON");

        return JSON;
    }

    /*
     * There is a limited set of serialization encodings that are known ahead of time. Instead of using a TreeMap with
     * a case-insensitive comparator, use an optimized search specifically for the known encodings.
     */
    private static SerializerEncoding checkForKnownEncoding(String contentType) {
        int length = contentType.length();

        // Check the length of the content type first as it is a quick check.
        if (length != 8 && length != 9 && length != 10 && length != 15 && length != 16) {
            return null;
        }

        if ("text/".regionMatches(true, 0, contentType, 0, 5)) {
            if (length == 8) {
                if ("xml".regionMatches(true, 0, contentType, 5, 3)) {
                    return XML;
                } else if ("csv".regionMatches(true, 0, contentType, 5, 3)) {
                    return TEXT;
                } else if ("css".regionMatches(true, 0, contentType, 5, 3)) {
                    return TEXT;
                }
            } else if (length == 9 && "html".regionMatches(true, 0, contentType, 5, 4)) {
                return TEXT;
            } else if (length == 10 && "plain".regionMatches(true, 0, contentType, 5, 5)) {
                return TEXT;
            } else if (length == 15 && "javascript".regionMatches(true, 0, contentType, 5, 10)) {
                return TEXT;
            }
        } else if ("application/".regionMatches(true, 0, contentType, 0, 12)) {
            if (length == 16 && "json".regionMatches(true, 0, contentType, 12, 4)) {
                return JSON;
            } else if (length == 15 && "xml".regionMatches(true, 0, contentType, 12, 3)) {
                return XML;
            }
        }

        return null;
    }
}
