// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.http;

import com.azure.cosmos.implementation.HttpConstants;
import io.netty.handler.codec.http2.Http2HeadersEncoder;
import io.netty.util.AsciiString;

/**
 * HPACK sensitivity detector for HTTP/2 connections to Cosmos DB.
 * <p>
 * Marks high-cardinality headers as "sensitive" (RFC 7541 §7.1.3), which instructs
 * the HPACK encoder to use the "never indexed" representation. This prevents these
 * headers' values from being stored in the HPACK dynamic table.
 * <p>
 * Headers marked as never-indexed:
 * <ul>
 *   <li>{@code authorization} — HMAC signature unique per-request (~80-200 bytes)</li>
 *   <li>{@code x-ms-date} — RFC 1123 timestamp unique per-request (~29 bytes)</li>
 *   <li>{@code x-ms-documentdb-partitionkey} — JSON partition key value, high cardinality (~45 bytes avg)</li>
 * </ul>
 * <p>
 * <b>Why this matters for performance:</b> These headers have unique values on every request
 * (or nearly so). Without this detector, each unique value is indexed in the HPACK dynamic table,
 * evicting reusable entries (User-Agent, x-ms-version, Content-Type, etc.) that are identical
 * across all requests. By marking them as never-indexed, the dynamic table retains its useful
 * static entries, improving HPACK compression efficiency.
 */
final class CosmosHttp2SensitivityDetector implements Http2HeadersEncoder.SensitivityDetector {

    static final CosmosHttp2SensitivityDetector INSTANCE = new CosmosHttp2SensitivityDetector();

    private static final AsciiString AUTHORIZATION_HEADER = AsciiString.of(HttpConstants.HttpHeaders.AUTHORIZATION);
    private static final AsciiString X_DATE_HEADER = AsciiString.of(HttpConstants.HttpHeaders.X_DATE);
    private static final AsciiString PARTITION_KEY_HEADER = AsciiString.of(HttpConstants.HttpHeaders.PARTITION_KEY);

    private CosmosHttp2SensitivityDetector() {
    }

    @Override
    public boolean isSensitive(CharSequence name, CharSequence value) {
        return AsciiString.contentEqualsIgnoreCase(name, AUTHORIZATION_HEADER)
            || AsciiString.contentEqualsIgnoreCase(name, X_DATE_HEADER)
            || AsciiString.contentEqualsIgnoreCase(name, PARTITION_KEY_HEADER);
    }
}
