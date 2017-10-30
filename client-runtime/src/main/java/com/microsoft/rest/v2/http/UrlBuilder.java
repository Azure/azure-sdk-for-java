/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

/**
 * A builder class that is used to create URLs.
 */
public class UrlBuilder {
    private String scheme;
    private String host;
    private Integer port;
    private String path;
    private String query;

    /**
     * Set the scheme/protocol that will be used to build the final URL.
     * @param scheme The scheme/protocol that will be used to build the final URL.
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
        if (host != null && host.endsWith("/")) {
            host = host.substring(0, host.length() - 1);
        }
        this.host = host;
        return this;
    }

    /**
     * Set the port that will be used to build the final URL.
     * @param port The port that will be used to build the final URL.
     * @return This UrlBuilder so that multiple setters can be chained together.
     */
    public UrlBuilder withPort(int port) {
        this.port = port;
        return this;
    }

    /**
     * Set the path that will be used to build the final URL.
     * @param path The path that will be used to build the final URL.
     * @return This UrlBuilder so that multiple setters can be chained together.
     */
    public UrlBuilder withPath(String path) {
        if (path != null) {
            String[] parts = path.split("\\?");
            this.path = parts[0];
            if (parts.length > 1) {
                String[] queryPairs = parts[1].split("&");
                for (String queryPair : queryPairs) {
                    String[] nameAndValue = queryPair.split("=");
                    if (nameAndValue.length != 2) {
                        throw new IllegalArgumentException("Path contained malformed query: " + path);
                    }

                    withQueryParameter(nameAndValue[0], nameAndValue[1]);
                }
            }
        }
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
            query = "?";
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
        final StringBuilder result = new StringBuilder();

        final boolean isAbsolutePath = path != null && (path.startsWith("http://") || path.startsWith("https://"));
        if (!isAbsolutePath) {
            if (scheme != null) {
                result.append(scheme);

                if (!scheme.endsWith("://")) {
                    result.append("://");
                }
            }

            if (host != null) {
                result.append(host);
            }
        }

        if (port != null) {
            result.append(":");
            result.append(port);
        }

        if (path != null) {
            if (result.length() != 0 && !path.startsWith("/")) {
                result.append('/');
            }
            result.append(path);
        }

        if (query != null) {
            result.append(query);
        }

        return result.toString();
    }
}
