// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

/**
 * The HTTP protocol versions supported when configuring an HTTP client's maximum protocol version.
 * <p>
 * Selecting {@link #HTTP_2} permits both HTTP/2 and HTTP/1.1, allowing the client to negotiate HTTP/2 and fall back to
 * HTTP/1.1 when the server does not support HTTP/2. The underlying HTTP client determines how negotiation is performed.
 */
public enum HttpProtocolVersion {
    /**
     * The HTTP client supports HTTP/1.1 only.
     */
    HTTP_1_1,

    /**
     * The HTTP client supports HTTP/2 with HTTP/1.1 fallback.
     */
    HTTP_2
}
