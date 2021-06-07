// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables.models;

import com.azure.core.annotation.Fluent;

/**
 * CORS is an HTTP feature that enables a web application running under one domain to access resources in another
 * domain. Web browsers implement a security restriction known as same-origin policy that prevents a web page from
 * calling APIs in a different domain; CORS provides a secure way to allow one domain (the origin domain) to call APIs
 * in another domain.
 */
@Fluent
public final class TableServiceCorsRule {
    /*
     * The origin domains that are permitted to make a request against the service via CORS. The origin domain is the
     * domain from which the request originates. Note that the origin must be an exact case-sensitive match with the
     * origin that the user age sends to the service. You can also use the wildcard character '*' to allow all origin
     * domains to make requests via CORS.
     */
    private String allowedOrigins;

    /*
     * The methods (HTTP request verbs) that the origin domain may use for a CORS request. (comma separated)
     */
    private String allowedMethods;

    /*
     * The request headers that the origin domain may specify on the CORS request.
     */
    private String allowedHeaders;

    /*
     * The response headers that may be sent in the response to the CORS request and exposed by the browser to the
     * request issuer.
     */
    private String exposedHeaders;

    /*
     * The maximum amount time that a browser should cache the preflight OPTIONS request.
     */
    private int maxAgeInSeconds;

    /**
     * Get the origin domains that are permitted to make a request against the service via CORS. The origin domain is
     * the domain from which the request originates. Note that the origin must be an exact case-sensitive match with
     * the origin that the user age sends to the service. You can also use the wildcard character '*' to allow all
     * origin domains to make requests via CORS.
     *
     * @return The {@code allowedOrigins}.
     */
    public String getAllowedOrigins() {
        return this.allowedOrigins;
    }

    /**
     * Set the allowedOrigins property: The origin domains that are permitted to make a request against the service via
     * CORS. The origin domain is the domain from which the request originates. Note that the origin must be an exact
     * case-sensitive match with the origin that the user age sends to the service. You can also use the wildcard
     * character '*' to allow all origin domains to make requests via CORS.
     *
     * @param allowedOrigins The {@code allowedOrigins} to set.
     *
     * @return The updated {@link TableServiceCorsRule} object.
     */
    public TableServiceCorsRule setAllowedOrigins(String allowedOrigins) {
        this.allowedOrigins = allowedOrigins;

        return this;
    }

    /**
     * Get the allowedMethods property: The methods (HTTP request verbs) that the origin domain may use for a CORS
     * request. (comma separated).
     *
     * @return The {@code allowedMethods}.
     */
    public String getAllowedMethods() {
        return this.allowedMethods;
    }

    /**
     * Set the allowedMethods property: The methods (HTTP request verbs) that the origin domain may use for a CORS
     * request. (comma separated).
     *
     * @param allowedMethods The {@code allowedMethods} to set.
     *
     * @return The updated {@link TableServiceCorsRule} object.
     */
    public TableServiceCorsRule setAllowedMethods(String allowedMethods) {
        this.allowedMethods = allowedMethods;

        return this;
    }

    /**
     * Get the request headers that the origin domain may specify on the CORS request.
     *
     * @return The {@code allowedHeaders}.
     */
    public String getAllowedHeaders() {
        return this.allowedHeaders;
    }

    /**
     * Set the request headers that the origin domain may specify on the CORS request.
     *
     * @param allowedHeaders The {@code allowedHeaders} to set.
     *
     * @return The updated {@link TableServiceCorsRule} object.
     */
    public TableServiceCorsRule setAllowedHeaders(String allowedHeaders) {
        this.allowedHeaders = allowedHeaders;

        return this;
    }

    /**
     * Get the response headers that may be sent in the response to the CORS request and exposed by the browser to
     * the request issuer.
     *
     * @return The {@code exposedHeaders} value.
     */
    public String getExposedHeaders() {
        return this.exposedHeaders;
    }

    /**
     * Set the response headers that may be sent in the response to the CORS request and exposed by the browser to
     * the request issuer.
     *
     * @param exposedHeaders The {@code exposedHeaders} to set.
     *
     * @return The updated {@link TableServiceCorsRule} object.
     */
    public TableServiceCorsRule setExposedHeaders(String exposedHeaders) {
        this.exposedHeaders = exposedHeaders;

        return this;
    }

    /**
     * Get the maximum amount time that a browser should cache the preflight OPTIONS request.
     *
     * @return The {@code maxAgeInSeconds}.
     */
    public int getMaxAgeInSeconds() {
        return this.maxAgeInSeconds;
    }

    /**
     * Set the maximum amount time that a browser should cache the preflight OPTIONS request.
     *
     * @param maxAgeInSeconds The {@code maxAgeInSeconds} to set.
     *
     * @return The updated {@link TableServiceCorsRule} object.
     */
    public TableServiceCorsRule setMaxAgeInSeconds(int maxAgeInSeconds) {
        this.maxAgeInSeconds = maxAgeInSeconds;

        return this;
    }
}
