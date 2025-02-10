// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.properties.core.client;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.spring.cloud.core.provider.HttpLoggingOptionsProvider;

import java.util.HashSet;
import java.util.Set;

/**
 * Options related to http logging. For example, if you want to log the http request or response, you could set the
 *  * level to {@link HttpLogDetailLevel#BASIC} or some other levels.
 */
public class HttpLoggingConfigurationProperties implements HttpLoggingOptionsProvider.HttpLoggingOptions {

    /**
     * The level of detail to log on HTTP messages. Supported types are: NONE, BASIC, HEADERS, BODY, BODY_AND_HEADERS. The default value is `NONE`.
     */
    private HttpLogDetailLevel level;
    /**
     * Comma-delimited list of allowlist headers that should be logged. The default value is `"x-ms-request-id","x-ms-client-request-id","x-ms-return-client-request-id","traceparent","MS-CV","Accept","Cache-Control","Connection","Content-Length","Content-Type","Date","ETag","Expires","If-Match","If-Modified-Since","If-None-Match","If-Unmodified-Since","Last-Modified","Pragma","Request-Id","Retry-After","Server","Transfer-Encoding","User-Agent","WWW-Authenticate"`.
     */
    private final Set<String> allowedHeaderNames = new HashSet<>();
    /**
     * Comma-delimited list of allowlist query parameters. The default value is `"api-version"`.
     */
    private final Set<String> allowedQueryParamNames = new HashSet<>();
    /**
     * Whether to pretty print the message bodies. The default value is `false`.
     */
    private Boolean prettyPrintBody;

    @Override
    public HttpLogDetailLevel getLevel() {
        return level;
    }

    public void setLevel(HttpLogDetailLevel level) {
        this.level = level;
    }

    @Override
    public Set<String> getAllowedHeaderNames() {
        return allowedHeaderNames;
    }

    @Override
    public Set<String> getAllowedQueryParamNames() {
        return allowedQueryParamNames;
    }

    @Override
    public Boolean getPrettyPrintBody() {
        return prettyPrintBody;
    }

    public void setPrettyPrintBody(Boolean prettyPrintBody) {
        this.prettyPrintBody = prettyPrintBody;
    }
}
