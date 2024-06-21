// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

/**
 * Represents the HTTP methods that can be used in a request.
 *
 * <p>This enum encapsulates the HTTP methods that can be used in a request, such as GET, PUT, POST, PATCH, DELETE,
 * HEAD, OPTIONS, TRACE, and CONNECT.</p>
 *
 * <p>This enum is useful when you want to specify the HTTP method of a request. For example, you can use it when
 * creating an instance of {@link HttpRequest}.</p>
 *
 * <p>Note: The HTTP methods are defined by the HTTP/1.1 specification (RFC 2616) and
 * the HTTP/2 specification (RFC 7540).</p>
 */
public enum HttpMethod {
    /**
     * The HTTP GET method.
     */
    GET,

    /**
     * The HTTP PUT method.
     */
    PUT,

    /**
     * The HTTP POST method.
     */
    POST,

    /**
     * The HTTP PATCH method.
     */
    PATCH,

    /**
     * The HTTP DELETE method.
     */
    DELETE,

    /**
     * The HTTP HEAD method.
     */
    HEAD,

    /**
     * The HTTP OPTIONS method.
     */
    OPTIONS,

    /**
     * The HTTP TRACE method.
     */
    TRACE,

    /**
     * The HTTP CONNECT method.
     */
    CONNECT
}
