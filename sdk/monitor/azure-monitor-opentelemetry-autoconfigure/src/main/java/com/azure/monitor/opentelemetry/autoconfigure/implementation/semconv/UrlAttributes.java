// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package com.azure.monitor.opentelemetry.autoconfigure.implementation.semconv;

import static io.opentelemetry.api.common.AttributeKey.stringKey;

import io.opentelemetry.api.common.AttributeKey;

// this is a copy of io.opentelemetry.semconv.UrlAttributes (1.37.0)

// DO NOT EDIT, this is an Auto-generated file from
// buildscripts/templates/registry/java/SemanticAttributes.java.j2
@SuppressWarnings("unused")
public final class UrlAttributes {
    /** The <a href="https://www.rfc-editor.org/rfc/rfc3986#section-3.5">URI fragment</a> component */
    public static final AttributeKey<String> URL_FRAGMENT = stringKey("url.fragment");

    /**
     * Absolute URL describing a network resource according to <a
     * href="https://www.rfc-editor.org/rfc/rfc3986">RFC3986</a>
     *
     * <p>Notes:
     *
     * <p>For network calls, URL usually has {@code scheme://host[:port][path][?query][#fragment]}
     * format, where the fragment is not transmitted over HTTP, but if it is known, it SHOULD be
     * included nevertheless.
     *
     * <p>{@code url.full} MUST NOT contain credentials passed via URL in form of {@code
     * https://username:password@www.example.com/}. In such case username and password SHOULD be
     * redacted and attribute's value SHOULD be {@code https://REDACTED:REDACTED@www.example.com/}.
     *
     * <p>{@code url.full} SHOULD capture the absolute URL when it is available (or can be
     * reconstructed).
     *
     * <p>Sensitive content provided in {@code url.full} SHOULD be scrubbed when instrumentations can
     * identify it.
     *
     * <p>Query string values for the following keys SHOULD be redacted by default and replaced by the
     * value {@code REDACTED}:
     *
     * <ul>
     *   <li><a
     *       href="https://docs.aws.amazon.com/AmazonS3/latest/userguide/RESTAuthentication.html#RESTAuthenticationQueryStringAuth">{@code
     *       AWSAccessKeyId}</a>
     *   <li><a
     *       href="https://docs.aws.amazon.com/AmazonS3/latest/userguide/RESTAuthentication.html#RESTAuthenticationQueryStringAuth">{@code
     *       Signature}</a>
     *   <li><a
     *       href="https://learn.microsoft.com/azure/storage/common/storage-sas-overview#sas-token">{@code
     *       sig}</a>
     *   <li><a href="https://cloud.google.com/storage/docs/access-control/signed-urls">{@code
     *       X-Goog-Signature}</a>
     * </ul>
     *
     * <p>This list is subject to change over time.
     *
     * <p>When a query string value is redacted, the query string key SHOULD still be preserved, e.g.
     * {@code https://www.example.com/path?color=blue&sig=REDACTED}.
     */
    public static final AttributeKey<String> URL_FULL = stringKey("url.full");

    /**
     * The <a href="https://www.rfc-editor.org/rfc/rfc3986#section-3.3">URI path</a> component
     *
     * <p>Notes:
     *
     * <p>Sensitive content provided in {@code url.path} SHOULD be scrubbed when instrumentations can
     * identify it.
     */
    public static final AttributeKey<String> URL_PATH = stringKey("url.path");

    /**
     * The <a href="https://www.rfc-editor.org/rfc/rfc3986#section-3.4">URI query</a> component
     *
     * <p>Notes:
     *
     * <p>Sensitive content provided in {@code url.query} SHOULD be scrubbed when instrumentations can
     * identify it.
     *
     * <p>Query string values for the following keys SHOULD be redacted by default and replaced by the
     * value {@code REDACTED}:
     *
     * <ul>
     *   <li><a
     *       href="https://docs.aws.amazon.com/AmazonS3/latest/userguide/RESTAuthentication.html#RESTAuthenticationQueryStringAuth">{@code
     *       AWSAccessKeyId}</a>
     *   <li><a
     *       href="https://docs.aws.amazon.com/AmazonS3/latest/userguide/RESTAuthentication.html#RESTAuthenticationQueryStringAuth">{@code
     *       Signature}</a>
     *   <li><a
     *       href="https://learn.microsoft.com/azure/storage/common/storage-sas-overview#sas-token">{@code
     *       sig}</a>
     *   <li><a href="https://cloud.google.com/storage/docs/access-control/signed-urls">{@code
     *       X-Goog-Signature}</a>
     * </ul>
     *
     * <p>This list is subject to change over time.
     *
     * <p>When a query string value is redacted, the query string key SHOULD still be preserved, e.g.
     * {@code q=OpenTelemetry&sig=REDACTED}.
     */
    public static final AttributeKey<String> URL_QUERY = stringKey("url.query");

    /**
     * The <a href="https://www.rfc-editor.org/rfc/rfc3986#section-3.1">URI scheme</a> component
     * identifying the used protocol.
     */
    public static final AttributeKey<String> URL_SCHEME = stringKey("url.scheme");

    private UrlAttributes() {
    }
}
