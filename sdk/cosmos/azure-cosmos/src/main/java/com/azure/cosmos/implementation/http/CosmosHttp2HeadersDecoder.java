// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.http;

import com.azure.cosmos.implementation.HttpConstants;
import io.netty.handler.codec.http2.DefaultHttp2Headers;
import io.netty.handler.codec.http2.DefaultHttp2HeadersDecoder;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.util.AsciiString;

final class CosmosHttp2HeadersDecoder extends DefaultHttp2HeadersDecoder {
    private static final AsciiString SERVER_VERSION = AsciiString.cached(HttpConstants.HttpHeaders.SERVER_VERSION);
    private static final int SERVER_VERSION_HASH = SERVER_VERSION.hashCode();

    CosmosHttp2HeadersDecoder(long maxHeaderListSize) {
        super(true, true, maxHeaderListSize);
    }

    @Override
    protected Http2Headers newHeaders() {
        return new CosmosHttp2Headers(numberOfHeadersGuess());
    }

    private static final class CosmosHttp2Headers extends DefaultHttp2Headers {
        private CosmosHttp2Headers(int arraySizeHint) {
            super(true, true, arraySizeHint);
        }

        @Override
        public Http2Headers add(CharSequence name, CharSequence value) {
            if (value != null && AsciiString.hashCode(name) == SERVER_VERSION_HASH
                && AsciiString.contentEquals(SERVER_VERSION, name)) {
                int start = 0;
                int end = value.length();
                while (start < end && isOptionalWhitespace(value.charAt(start))) {
                    start++;
                }
                while (end > start && isOptionalWhitespace(value.charAt(end - 1))) {
                    end--;
                }
                if (start != 0 || end != value.length()) {
                    value = value instanceof AsciiString
                        ? ((AsciiString) value).subSequence(start, end, false)
                        : value.subSequence(start, end);
                }
            }
            return super.add(name, value);
        }

        private static boolean isOptionalWhitespace(char value) {
            // CR/LF/NUL must reach Netty's strict validator, not be trimmed away.
            return value == ' ' || value == '\t';
        }
    }
}
