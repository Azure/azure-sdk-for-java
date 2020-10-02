// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.models;

import com.azure.core.annotation.Fluent;

/**
 * The HttpRequestDataFeedSource model.
 */
@Fluent
public final class HttpRequestDataFeedSource extends DataFeedSource {

    /*
     * HTTP URL
     */
    private final String url;

    /*
     * HTTP header
     */
    private String httpHeader;

    /*
     * HTTP method
     */
    private final String httpMethod;

    /*
     * HTTP request body
     */
    private String payload;

    /**
     * Create a HttpRequestDataFeedSource instance.
     *  @param url the HTTP url.
     * @param httpMethod the HTTP method.
     */
    public HttpRequestDataFeedSource(final String url, final String httpMethod) {
        this.url = url;
        this.httpMethod = httpMethod;
    }

    /**
     * Get the url property: HTTP URL.
     *
     * @return the url value.
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * Get the httpHeader property: HTTP header.
     *
     * @return the httpHeader value.
     */
    public String getHttpHeader() {
        return this.httpHeader;
    }

    /**
     * Get the httpMethod property: HTTP method.
     *
     * @return the httpMethod value.
     */
    public String getHttpMethod() {
        return this.httpMethod;
    }

    /**
     * Get the payload property: HTTP request body.
     *
     * @return the payload value.
     */
    public String getPayload() {
        return this.payload;
    }

    /**
     * Set the httpHeader property: HTTP header.
     *
     * @param httpHeader the httpHeader value to set.
     * @return the HttpRequestDataFeedSource object itself.
     */
    public HttpRequestDataFeedSource setHttpHeader(String httpHeader) {
        this.httpHeader = httpHeader;
        return this;
    }

    /**
     * Set the payload property: HTTP reuqest body.
     *
     * @param payload the payload value to set.
     * @return the HttpRequestDataFeedSource object itself.
     */
    public HttpRequestDataFeedSource setPayload(String payload) {
        this.payload = payload;
        return this;
    }
}
