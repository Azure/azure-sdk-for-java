// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.http;

import com.azure.cosmos.implementation.HttpConstants;
import io.netty.handler.codec.http2.DefaultHttp2Headers;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.util.AsciiString;

final class CosmosHttp2Headers extends DefaultHttp2Headers {
    private static final AsciiString SERVER_VERSION = AsciiString.cached(HttpConstants.HttpHeaders.SERVER_VERSION);
    private static final int SERVER_VERSION_HASH = SERVER_VERSION.hashCode();

    CosmosHttp2Headers(boolean validateNames, boolean validateValues, int arraySizeHint) {
        super(validateNames, validateValues, arraySizeHint);
    }

    @Override
    public Http2Headers add(CharSequence name, CharSequence value) {
        // Normalize this one service field before Netty's standard validation and insertion.
        return super.add(name, normalizeServiceVersion(name, value));
    }

    private static CharSequence normalizeServiceVersion(CharSequence name, CharSequence value) {
        if (value == null || AsciiString.hashCode(name) != SERVER_VERSION_HASH
            || !AsciiString.contentEquals(SERVER_VERSION, name)) {
            return value;
        }
        int start = 0;
        int end = value.length();
        while (start < end && isOptionalWhitespace(value.charAt(start))) {
            start++;
        }
        while (end > start && isOptionalWhitespace(value.charAt(end - 1))) {
            end--;
        }
        return start == 0 && end == value.length() ? value : value instanceof AsciiString
            ? ((AsciiString) value).subSequence(start, end, false) : value.subSequence(start, end);
    }

    private static boolean isOptionalWhitespace(char value) {
        // CR/LF/NUL must reach Netty's strict validator, not be trimmed away.
        return value == ' ' || value == '\t';
    }
}
