// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.http.client;

/**
 * Enum that represents the HTTP protocol versions supported by the client.
 * <p>
 * When configuring the {@link HttpProtocolVersion} for an HTTP client, it will support the version specified and all
 * previous versions. For example, if {@link HttpProtocolVersion#HTTP_2} is specified, the client will support
 * both HTTP/2 and HTTP/1.1.
 */
public enum HttpProtocolVersion {
    /**
     * HTTP client will support HTTP/1.1.
     */
    HTTP_1_1,

    /**
     * HTTP client will support HTTP/2 and {@link #HTTP_1_1 HTTP/1.1}.
     */
    HTTP_2,
}
