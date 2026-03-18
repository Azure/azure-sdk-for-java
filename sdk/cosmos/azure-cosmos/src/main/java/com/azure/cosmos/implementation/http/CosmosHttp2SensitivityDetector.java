// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.http;

import com.azure.cosmos.implementation.HttpConstants;
import io.netty.handler.codec.http2.Http2HeadersEncoder;
import io.netty.util.AsciiString;

/**
 * HPACK sensitivity detector for HTTP/2 connections to Cosmos DB.
 * <p>
 * Marks the {@code authorization} header as "sensitive" (RFC 7541 §7.1.3), which instructs
 * the HPACK encoder to use the "never indexed" representation. This prevents the authorization
 * header's value from being stored in the HPACK dynamic table.
 * <p>
 * <b>Why this matters for performance:</b> Cosmos DB's authorization header has a unique value
 * per-request (HMAC signature based on method, resource, and timestamp). Without this detector,
 * each request's unique authorization value is indexed in the HPACK dynamic table, evicting
 * reusable entries (User-Agent, x-ms-version, Content-Type, etc.) that are identical across
 * all requests. By marking authorization as never-indexed, the dynamic table retains its useful
 * entries, improving HPACK compression efficiency for all other headers.
 */
final class CosmosHttp2SensitivityDetector implements Http2HeadersEncoder.SensitivityDetector {

    static final CosmosHttp2SensitivityDetector INSTANCE = new CosmosHttp2SensitivityDetector();

    private static final AsciiString AUTHORIZATION_HEADER = AsciiString.of(HttpConstants.HttpHeaders.AUTHORIZATION);

    private CosmosHttp2SensitivityDetector() {
    }

    @Override
    public boolean isSensitive(CharSequence name, CharSequence value) {
        return AsciiString.contentEqualsIgnoreCase(name, AUTHORIZATION_HEADER);
    }
}
