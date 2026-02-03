// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package com.azure.monitor.opentelemetry.autoconfigure.implementation.semconv.incubating;

import static io.opentelemetry.api.common.AttributeKey.stringKey;

import io.opentelemetry.api.common.AttributeKey;

// this is a copy of io.opentelemetry.semconv.incubating.UrlIncubatingAttributes (1.37.0)

// DO NOT EDIT, this is an Auto-generated file from
// buildscripts/templates/registry/incubating_java/IncubatingSemanticAttributes.java.j2
@SuppressWarnings("unused")
public final class UrlIncubatingAttributes {
    /**
     * Domain extracted from the {@code url.full}, such as "opentelemetry.io".
     *
     * <p>Notes:
     *
     * <ul>
     *   <li>In some cases a URL may refer to an IP and/or port directly, without a domain name. In
     *       this case, the IP address would go to the domain field. If the URL contains a <a
     *       href="https://www.rfc-editor.org/rfc/rfc3986#section-3.2.2">literal IPv6 address</a>
     *       enclosed by {@code [} and {@code ]}, the {@code [} and {@code ]} characters should also
     *       be captured in the domain field.
     * </ul>
     */
    public static final AttributeKey<String> URL_DOMAIN = stringKey("url.domain");

    /**
     * The file extension extracted from the {@code url.full}, excluding the leading dot.
     *
     * <p>Notes:
     *
     * <ul>
     *   <li>The file extension is only set if it exists, as not every url has a file extension. When
     *       the file name has multiple extensions {@code example.tar.gz}, only the last one should
     *       be captured {@code gz}, not {@code tar.gz}.
     * </ul>
     */
    public static final AttributeKey<String> URL_EXTENSION = stringKey("url.extension");

    /**
     * Absolute URL describing a network resource according to <a
     * href="https://www.rfc-editor.org/rfc/rfc3986">RFC3986</a>
     *
     * <p>Notes:
     *
     * <ul>
     *   <li>For network calls, URL usually has {@code scheme://host[:port][path][?query][#fragment]}
     *       format, where the fragment is not transmitted over HTTP, but if it is known, it SHOULD be
     *       included nevertheless. {@code url.full} MUST NOT contain credentials passed via URL in
     *       form of {@code https://username:password@www.example.com/}. In such case username and
     *       password SHOULD be redacted and attribute's value SHOULD be {@code
     *       https://REDACTED:REDACTED@www.example.com/}. {@code url.full} SHOULD capture the absolute
     *       URL when it is available (or can be reconstructed). Sensitive content provided in {@code
     *       url.full} SHOULD be scrubbed when instrumentations can identify it.
     * </ul>
     */
    public static final AttributeKey<String> URL_FULL = stringKey("url.full");

    /**
     * The <a href="https://www.rfc-editor.org/rfc/rfc3986#section-3.3">URI path</a> component
     *
     * <p>Notes:
     *
     * <ul>
     *   <li>Sensitive content provided in {@code url.path} SHOULD be scrubbed when instrumentations
     *       can identify it.
     * </ul>
     */
    public static final AttributeKey<String> URL_PATH = stringKey("url.path");

    /**
     * Port extracted from the {@code url.full}
     *
     * <p>Notes:
     *
     * <ul>
     *   <li>When the port is not specified in the URL, or if it's the default port for the URL's
     *       scheme, the value should be omitted.
     * </ul>
     */
    public static final AttributeKey<Long> URL_PORT = io.opentelemetry.api.common.AttributeKey.longKey("url.port");

    /**
     * The <a href="https://www.rfc-editor.org/rfc/rfc3986#section-3.4">URI query</a> component
     *
     * <p>Notes:
     *
     * <ul>
     *   <li>Sensitive content provided in {@code url.query} SHOULD be scrubbed when instrumentations
     *       can identify it.
     * </ul>
     */
    public static final AttributeKey<String> URL_QUERY = stringKey("url.query");

    /**
     * The highest registered url domain, stripped of the subdomain.
     *
     * <p>Notes:
     *
     * <ul>
     *   <li>This value can be determined precisely with the <a
     *       href="http://publicsuffix.org">public suffix list</a>. For example, the registered domain
     *       for {@code foo.example.com} is {@code example.com}. Trying to approximate this by simply
     *       taking the last two labels will not work well for TLDs such as {@code co.uk}.
     * </ul>
     */
    public static final AttributeKey<String> URL_REGISTERED_DOMAIN = stringKey("url.registered_domain");

    /**
     * The <a href="https://www.rfc-editor.org/rfc/rfc3986#section-3.1">URI scheme</a> component
     * identifying the used protocol.
     */
    public static final AttributeKey<String> URL_SCHEME = stringKey("url.scheme");

    /**
     * The subdomain portion of a fully qualified domain name includes all of the names except the
     * host name under the registered_domain. In a partially qualified domain, or if the
     * qualification level of the full name cannot be determined, subdomain contains all of the names
     * below the registered domain.
     *
     * <p>Notes:
     *
     * <ul>
     *   <li>The subdomain portion of {@code www.east.mydomain.com} is {@code east}. If the domain
     *       has multiple levels of subdomain, such as {@code sub2.sub1.example.com}, the subdomain
     *       field should contain {@code sub2.sub1}, with no trailing period.
     * </ul>
     */
    public static final AttributeKey<String> URL_SUBDOMAIN = stringKey("url.subdomain");

    /**
     * The low-cardinality template of an <a href="#url">absolute path reference</a>.
     *
     * <p>Notes:
     *
     * <ul>
     *   <li>The {@code url.template} MUST have low cardinality. It is not usually available on HTTP
     *       clients, but may be known by the application or specialized HTTP instrumentation that can
     *       provide it. The {@code url.template} SHOULD be as specific as possible and SHOULD NOT
     *       contain any parameter values. When using {@code url.template}, {@code url.path} SHOULD
     *       also be provided, but {@code url.template} is prioritized for cardinality reduction over
     *       {@code url.path}.
     * </ul>
     */
    public static final AttributeKey<String> URL_TEMPLATE = stringKey("url.template");

    /**
     * The effective top level domain (eTLD), also known as the domain suffix, is the last part of
     * the domain name. For example, the top level domain for example.com is {@code com}.
     *
     * <p>Notes:
     *
     * <ul>
     *   <li>This value can be determined precisely with the <a
     *       href="http://publicsuffix.org">public suffix list</a>.
     * </ul>
     */
    public static final AttributeKey<String> URL_TOP_LEVEL_DOMAIN = stringKey("url.top_level_domain");

    private UrlIncubatingAttributes() {
    }
}
