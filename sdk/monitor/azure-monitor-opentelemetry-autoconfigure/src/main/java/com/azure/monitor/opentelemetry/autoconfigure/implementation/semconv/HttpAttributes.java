// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package com.azure.monitor.opentelemetry.autoconfigure.implementation.semconv;

import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static com.azure.monitor.opentelemetry.autoconfigure.implementation.semconv.AttributeKeyTemplate.stringArrayKeyTemplate;

import io.opentelemetry.api.common.AttributeKey;
import java.util.List;

// this is a copy of io.opentelemetry.semconv.HttpAttributes (1.37.0)

// DO NOT EDIT, this is an Auto-generated file from
// buildscripts/templates/registry/java/SemanticAttributes.java.j2
@SuppressWarnings("unused")
public final class HttpAttributes {
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
     */
    public static final AttributeKeyTemplate<List<String>> HTTP_REQUEST_HEADER
        = stringArrayKeyTemplate("http.request.header");

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
     */
    public static final AttributeKey<String> HTTP_REQUEST_METHOD = stringKey("http.request.method");

    /** Original HTTP method sent by the client in the request line. */
    public static final AttributeKey<String> HTTP_REQUEST_METHOD_ORIGINAL = stringKey("http.request.method_original");

    /**
     * The ordinal number of request resending attempt (for any reason, including redirects).
     *
     * <p>Notes:
     *
     * <p>The resend count SHOULD be updated each time an HTTP request gets resent by the client,
     * regardless of what was the cause of the resending (e.g. redirection, authorization failure, 503
     * Server Unavailable, network issues, or any other).
     */
    public static final AttributeKey<Long> HTTP_REQUEST_RESEND_COUNT = longKey("http.request.resend_count");

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
     */
    public static final AttributeKeyTemplate<List<String>> HTTP_RESPONSE_HEADER
        = stringArrayKeyTemplate("http.response.header");

    /** <a href="https://tools.ietf.org/html/rfc7231#section-6">HTTP response status code</a>. */
    public static final AttributeKey<Long> HTTP_RESPONSE_STATUS_CODE = longKey("http.response.status_code");

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
     */
    public static final AttributeKey<String> HTTP_ROUTE = stringKey("http.route");

    // Enum definition
    /** Values for {@link #HTTP_REQUEST_METHOD}. */
    public static final class HttpRequestMethodValues {
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

        private HttpRequestMethodValues() {
        }
    }

    private HttpAttributes() {
    }
}
