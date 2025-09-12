// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package com.azure.monitor.opentelemetry.autoconfigure.implementation.semconv.incubating;

import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;

import io.opentelemetry.api.common.AttributeKey;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.semconv.AttributeKeyTemplate;
import java.util.List;

// this is a copy of io.opentelemetry.semconv.incubating.HttpIncubatingAttributes (1.37.0)

// DO NOT EDIT, this is an Auto-generated file from
// buildscripts/templates/registry/incubating_java/IncubatingSemanticAttributes.java.j2
@SuppressWarnings("unused")
public final class HttpIncubatingAttributes {
    /**
     * Deprecated, use {@code client.address} instead.
     *
     * @deprecated Replaced by {@code client.address}.
     */
    @Deprecated
    public static final AttributeKey<String> HTTP_CLIENT_IP = stringKey("http.client_ip");

    /** State of the HTTP connection in the HTTP connection pool. */
    public static final AttributeKey<String> HTTP_CONNECTION_STATE = stringKey("http.connection.state");

    /**
     * Deprecated, use {@code network.protocol.name} instead.
     *
     * @deprecated Replaced by {@code network.protocol.name}.
     */
    @Deprecated
    public static final AttributeKey<String> HTTP_FLAVOR = stringKey("http.flavor");

    /**
     * Deprecated, use one of {@code server.address}, {@code client.address} or {@code
     * http.request.header.host} instead, depending on the usage.
     *
     * @deprecated Replaced by one of {@code server.address}, {@code client.address} or {@code
     *     http.request.header.host}, depending on the usage.
     */
    @Deprecated
    public static final AttributeKey<String> HTTP_HOST = stringKey("http.host");

    /**
     * Deprecated, use {@code http.request.method} instead.
     *
     * @deprecated Replaced by {@code http.request.method}.
     */
    @Deprecated
    public static final AttributeKey<String> HTTP_METHOD = stringKey("http.method");

    /**
     * The size of the request payload body in bytes. This is the number of bytes transferred
     * excluding headers and is often, but not always, present as the <a
     * href="https://www.rfc-editor.org/rfc/rfc9110.html#field.content-length">Content-Length</a>
     * header. For requests using transport encoding, this should be the compressed size.
     */
    public static final AttributeKey<Long> HTTP_REQUEST_BODY_SIZE = longKey("http.request.body.size");

    /**
     * HTTP request headers, {@code <key>} being the normalized HTTP Header name (lowercase), the
     * value being the header values.
     *
     * <p>Notes:
     *
     * <p>Instrumentations SHOULD require an explicit configuration of which headers are to be
     * captured. Including all request headers can be a security risk - explicit configuration helps
     * avoid leaking sensitive information.
     *
     * <p>The {@code User-Agent} header is already captured in the {@code user_agent.original}
     * attribute. Users MAY explicitly configure instrumentations to capture them even though it is
     * not recommended.
     *
     * <p>The attribute value MUST consist of either multiple header values as an array of strings or
     * a single-item array containing a possibly comma-concatenated string, depending on the way the
     * HTTP library provides access to headers.
     *
     * <p>Examples:
     *
     * <ul>
     *   <li>A header {@code Content-Type: application/json} SHOULD be recorded as the {@code
     *       http.request.header.content-type} attribute with value {@code ["application/json"]}.
     *   <li>A header {@code X-Forwarded-For: 1.2.3.4, 1.2.3.5} SHOULD be recorded as the {@code
     *       http.request.header.x-forwarded-for} attribute with value {@code ["1.2.3.4", "1.2.3.5"]}
     *       or {@code ["1.2.3.4, 1.2.3.5"]} depending on the HTTP library.
     * </ul>
     *
     * @deprecated deprecated in favor of stable {@link
     *     io.opentelemetry.semconv.HttpAttributes#HTTP_REQUEST_HEADER} attribute.
     */
    @Deprecated
    public static final AttributeKeyTemplate<List<String>> HTTP_REQUEST_HEADER
        = AttributeKeyTemplate.stringArrayKeyTemplate("http.request.header");

    /**
     * HTTP request method.
     *
     * <p>Notes:
     *
     * <p>HTTP request method value SHOULD be "known" to the instrumentation. By default, this
     * convention defines "known" methods as the ones listed in <a
     * href="https://www.rfc-editor.org/rfc/rfc9110.html#name-methods">RFC9110</a> and the PATCH
     * method defined in <a href="https://www.rfc-editor.org/rfc/rfc5789.html">RFC5789</a>.
     *
     * <p>If the HTTP request method is not known to instrumentation, it MUST set the {@code
     * http.request.method} attribute to {@code _OTHER}.
     *
     * <p>If the HTTP instrumentation could end up converting valid HTTP request methods to {@code
     * _OTHER}, then it MUST provide a way to override the list of known HTTP methods. If this
     * override is done via environment variable, then the environment variable MUST be named
     * OTEL_INSTRUMENTATION_HTTP_KNOWN_METHODS and support a comma-separated list of case-sensitive
     * known HTTP methods (this list MUST be a full override of the default known method, it is not a
     * list of known methods in addition to the defaults).
     *
     * <p>HTTP method names are case-sensitive and {@code http.request.method} attribute value MUST
     * match a known HTTP method name exactly. Instrumentations for specific web frameworks that
     * consider HTTP methods to be case insensitive, SHOULD populate a canonical equivalent. Tracing
     * instrumentations that do so, MUST also set {@code http.request.method_original} to the original
     * value.
     *
     * @deprecated deprecated in favor of stable {@link
     *     io.opentelemetry.semconv.HttpAttributes#HTTP_REQUEST_METHOD} attribute.
     */
    @Deprecated
    public static final AttributeKey<String> HTTP_REQUEST_METHOD = stringKey("http.request.method");

    /**
     * Original HTTP method sent by the client in the request line.
     *
     * @deprecated deprecated in favor of stable {@link
     *     io.opentelemetry.semconv.HttpAttributes#HTTP_REQUEST_METHOD_ORIGINAL} attribute.
     */
    @Deprecated
    public static final AttributeKey<String> HTTP_REQUEST_METHOD_ORIGINAL = stringKey("http.request.method_original");

    /**
     * The ordinal number of request resending attempt (for any reason, including redirects).
     *
     * <p>Notes:
     *
     * <p>The resend count SHOULD be updated each time an HTTP request gets resent by the client,
     * regardless of what was the cause of the resending (e.g. redirection, authorization failure, 503
     * Server Unavailable, network issues, or any other).
     *
     * @deprecated deprecated in favor of stable {@link
     *     io.opentelemetry.semconv.HttpAttributes#HTTP_REQUEST_RESEND_COUNT} attribute.
     */
    @Deprecated
    public static final AttributeKey<Long> HTTP_REQUEST_RESEND_COUNT = longKey("http.request.resend_count");

    /**
     * The total size of the request in bytes. This should be the total number of bytes sent over the
     * wire, including the request line (HTTP/1.1), framing (HTTP/2 and HTTP/3), headers, and request
     * body if any.
     */
    public static final AttributeKey<Long> HTTP_REQUEST_SIZE = longKey("http.request.size");

    /**
     * Deprecated, use {@code http.request.header.content-length} instead.
     *
     * @deprecated Replaced by {@code http.request.header.content-length}.
     */
    @Deprecated
    public static final AttributeKey<Long> HTTP_REQUEST_CONTENT_LENGTH = longKey("http.request_content_length");

    /**
     * Deprecated, use {@code http.request.body.size} instead.
     *
     * @deprecated Replaced by {@code http.request.body.size}.
     */
    @Deprecated
    public static final AttributeKey<Long> HTTP_REQUEST_CONTENT_LENGTH_UNCOMPRESSED
        = longKey("http.request_content_length_uncompressed");

    /**
     * The size of the response payload body in bytes. This is the number of bytes transferred
     * excluding headers and is often, but not always, present as the <a
     * href="https://www.rfc-editor.org/rfc/rfc9110.html#field.content-length">Content-Length</a>
     * header. For requests using transport encoding, this should be the compressed size.
     */
    public static final AttributeKey<Long> HTTP_RESPONSE_BODY_SIZE = longKey("http.response.body.size");

    /**
     * HTTP response headers, {@code <key>} being the normalized HTTP Header name (lowercase), the
     * value being the header values.
     *
     * <p>Notes:
     *
     * <p>Instrumentations SHOULD require an explicit configuration of which headers are to be
     * captured. Including all response headers can be a security risk - explicit configuration helps
     * avoid leaking sensitive information.
     *
     * <p>Users MAY explicitly configure instrumentations to capture them even though it is not
     * recommended.
     *
     * <p>The attribute value MUST consist of either multiple header values as an array of strings or
     * a single-item array containing a possibly comma-concatenated string, depending on the way the
     * HTTP library provides access to headers.
     *
     * <p>Examples:
     *
     * <ul>
     *   <li>A header {@code Content-Type: application/json} header SHOULD be recorded as the {@code
     *       http.request.response.content-type} attribute with value {@code ["application/json"]}.
     *   <li>A header {@code My-custom-header: abc, def} header SHOULD be recorded as the {@code
     *       http.response.header.my-custom-header} attribute with value {@code ["abc", "def"]} or
     *       {@code ["abc, def"]} depending on the HTTP library.
     * </ul>
     *
     * @deprecated deprecated in favor of stable {@link
     *     io.opentelemetry.semconv.HttpAttributes#HTTP_RESPONSE_HEADER} attribute.
     */
    @Deprecated
    public static final AttributeKeyTemplate<List<String>> HTTP_RESPONSE_HEADER
        = AttributeKeyTemplate.stringArrayKeyTemplate("http.response.header");

    /**
     * The total size of the response in bytes. This should be the total number of bytes sent over the
     * wire, including the status line (HTTP/1.1), framing (HTTP/2 and HTTP/3), headers, and response
     * body and trailers if any.
     */
    public static final AttributeKey<Long> HTTP_RESPONSE_SIZE = longKey("http.response.size");

    /**
     * <a href="https://tools.ietf.org/html/rfc7231#section-6">HTTP response status code</a>.
     *
     * @deprecated deprecated in favor of stable {@link
     *     io.opentelemetry.semconv.HttpAttributes#HTTP_RESPONSE_STATUS_CODE} attribute.
     */
    @Deprecated
    public static final AttributeKey<Long> HTTP_RESPONSE_STATUS_CODE = longKey("http.response.status_code");

    /**
     * Deprecated, use {@code http.response.header.content-length} instead.
     *
     * @deprecated Replaced by {@code http.response.header.content-length}.
     */
    @Deprecated
    public static final AttributeKey<Long> HTTP_RESPONSE_CONTENT_LENGTH = longKey("http.response_content_length");

    /**
     * Deprecated, use {@code http.response.body.size} instead.
     *
     * @deprecated Replaced by {@code http.response.body.size}.
     */
    @Deprecated
    public static final AttributeKey<Long> HTTP_RESPONSE_CONTENT_LENGTH_UNCOMPRESSED
        = longKey("http.response_content_length_uncompressed");

    /**
     * The matched route, that is, the path template in the format used by the respective server
     * framework.
     *
     * <p>Notes:
     *
     * <p>MUST NOT be populated when this is not supported by the HTTP server framework as the route
     * attribute should have low-cardinality and the URI path can NOT substitute it. SHOULD include
     * the <a href="/docs/http/http-spans.md#http-server-definitions">application root</a> if there is
     * one.
     *
     * @deprecated deprecated in favor of stable {@link
     *     io.opentelemetry.semconv.HttpAttributes#HTTP_ROUTE} attribute.
     */
    @Deprecated
    public static final AttributeKey<String> HTTP_ROUTE = stringKey("http.route");

    /**
     * Deprecated, use {@code url.scheme} instead.
     *
     * @deprecated Replaced by {@code url.scheme}.
     */
    @Deprecated
    public static final AttributeKey<String> HTTP_SCHEME = stringKey("http.scheme");

    /**
     * Deprecated, use {@code server.address} instead.
     *
     * @deprecated Replaced by {@code server.address}.
     */
    @Deprecated
    public static final AttributeKey<String> HTTP_SERVER_NAME = stringKey("http.server_name");

    /**
     * Deprecated, use {@code http.response.status_code} instead.
     *
     * @deprecated Replaced by {@code http.response.status_code}.
     */
    @Deprecated
    public static final AttributeKey<Long> HTTP_STATUS_CODE = longKey("http.status_code");

    /**
     * Deprecated, use {@code url.path} and {@code url.query} instead.
     *
     * @deprecated Split to {@code url.path} and {@code url.query}.
     */
    @Deprecated
    public static final AttributeKey<String> HTTP_TARGET = stringKey("http.target");

    /**
     * Deprecated, use {@code url.full} instead.
     *
     * @deprecated Replaced by {@code url.full}.
     */
    @Deprecated
    public static final AttributeKey<String> HTTP_URL = stringKey("http.url");

    /**
     * Deprecated, use {@code user_agent.original} instead.
     *
     * @deprecated Replaced by {@code user_agent.original}.
     */
    @Deprecated
    public static final AttributeKey<String> HTTP_USER_AGENT = stringKey("http.user_agent");

    // Enum definitions

    /** Values for {@link #HTTP_CONNECTION_STATE}. */
    public static final class HttpConnectionStateIncubatingValues {
        /** active state. */
        public static final String ACTIVE = "active";

        /** idle state. */
        public static final String IDLE = "idle";

        private HttpConnectionStateIncubatingValues() {
        }
    }

    /**
     * Values for {@link #HTTP_FLAVOR}
     *
     * @deprecated Replaced by {@code network.protocol.name}.
     */
    @Deprecated
    public static final class HttpFlavorIncubatingValues {
        /** HTTP/1.0 */
        public static final String HTTP_1_0 = "1.0";

        /** HTTP/1.1 */
        public static final String HTTP_1_1 = "1.1";

        /** HTTP/2 */
        public static final String HTTP_2_0 = "2.0";

        /** HTTP/3 */
        public static final String HTTP_3_0 = "3.0";

        /** SPDY protocol. */
        public static final String SPDY = "SPDY";

        /** QUIC protocol. */
        public static final String QUIC = "QUIC";

        private HttpFlavorIncubatingValues() {
        }
    }

    /**
     * Values for {@link #HTTP_REQUEST_METHOD}.
     *
     * @deprecated deprecated in favor of stable {@link
     *     io.opentelemetry.semconv.HttpAttributes.HttpRequestMethodValues}.
     */
    @Deprecated
    public static final class HttpRequestMethodIncubatingValues {
        /** CONNECT method. */
        public static final String CONNECT = "CONNECT";

        /** DELETE method. */
        public static final String DELETE = "DELETE";

        /** GET method. */
        public static final String GET = "GET";

        /** HEAD method. */
        public static final String HEAD = "HEAD";

        /** OPTIONS method. */
        public static final String OPTIONS = "OPTIONS";

        /** PATCH method. */
        public static final String PATCH = "PATCH";

        /** POST method. */
        public static final String POST = "POST";

        /** PUT method. */
        public static final String PUT = "PUT";

        /** TRACE method. */
        public static final String TRACE = "TRACE";

        /** Any HTTP method that the instrumentation has no prior knowledge of. */
        public static final String OTHER = "_OTHER";

        private HttpRequestMethodIncubatingValues() {
        }
    }

    private HttpIncubatingAttributes() {
    }
}
