/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * A builder class that is used to create URLs.
 */
public class UrlBuilder {
    private String scheme;
    private String host;
    private String path;
    private String query;

    /**
<<<<<<< HEAD
     * Set the scheme/protocol that will be used to build the final URL.
     * @param scheme The setScheme/protocol that will be used to build the final URL.
     * @return This UrlBuilder so that multiple setters can be chained together.
     */
    public UrlBuilder withScheme(String scheme) {
        this.scheme = scheme;
        return this;
    }

    /**
     * Set the host that will be used to build the final URL.
     * @param host The host that will be used to build the final URL.
     * @return This UrlBuilder so that multiple setters can be chained together.
     */
    public UrlBuilder withHost(String host) {
        this.host = host;
        return this;
    }

    /**
     * Set the path that will be used to build the final URL.
     * @param path The path that will be used to build the final URL.
     * @return This UrlBuilder so that multiple setters can be chained together.
     */
    public UrlBuilder withPath(String path) {
        if (path != null && !path.startsWith("/")) {
            path = "/" + path;
        }
        this.path = path;
        return this;
    }

    /**
     * Add the provided query parameter name and encoded value to query string for the final URL.
     * @param queryParameterName The name of the query parameter.
     * @param queryParameterEncodedValue The encoded value of the query parameter.
     * @return The provided query parameter name and encoded value to query string for the final
     * URL.
     */
    public UrlBuilder withQueryParameter(String queryParameterName, String queryParameterEncodedValue) {
        if (query == null) {
            query = "";
        }
        else {
            query += "&";
        }
        query += queryParameterName + "=" + queryParameterEncodedValue;
        return this;
    }

    /**
     * Get the string representation of the URL that is being built.
     * @return The string representation of the URL that is being built.
     */
    public String toString() {
        URI uri;
        try {
            uri = new URI(scheme, null, host, -1, path, query, null);
        }
        catch (URISyntaxException e) {
            uri = null;
        }
        return uri == null ? null : uri.toString();
    }
}
