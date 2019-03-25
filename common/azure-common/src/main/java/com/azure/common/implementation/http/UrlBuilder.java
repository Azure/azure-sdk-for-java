/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.common.implementation.http;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A builder class that is used to create URLs.
 */
public final class UrlBuilder {
    private String scheme;
    private String host;
    private String port;
    private String path;

    // LinkedHashMap preserves insertion order
    private final Map<String, String> query = new LinkedHashMap<>();

    /**
     * Set the scheme/protocol that will be used to build the final URL.
     * @param scheme The scheme/protocol that will be used to build the final URL.
     * @return This UrlBuilder so that multiple setters can be chained together.
     */
    public UrlBuilder withScheme(String scheme) {
        if (scheme == null || scheme.isEmpty()) {
            this.scheme = null;
        }
        else {
            with(scheme, UrlTokenizerState.SCHEME);
        }
        return this;
    }

    /**
     * Get the scheme/protocol that has been assigned to this UrlBuilder.
     * @return the scheme/protocol that has been assigned to this UrlBuilder.
     */
    public String scheme() {
        return scheme;
    }

    /**
     * Set the host that will be used to build the final URL.
     * @param host The host that will be used to build the final URL.
     * @return This UrlBuilder so that multiple setters can be chained together.
     */
    public UrlBuilder withHost(String host) {
        if (host == null || host.isEmpty()) {
            this.host = null;
        }
        else {
            with(host, UrlTokenizerState.SCHEME_OR_HOST);
        }
        return this;
    }

    /**
     * Get the host that has been assigned to this UrlBuilder.
     * @return the host that has been assigned to this UrlBuilder.
     */
    public String host() {
        return host;
    }

    /**
     * Set the port that will be used to build the final URL.
     * @param port The port that will be used to build the final URL.
     * @return This UrlBuilder so that multiple setters can be chained together.
     */
    public UrlBuilder withPort(String port) {
        if (port == null || port.isEmpty()) {
            this.port = null;
        }
        else {
            with(port, UrlTokenizerState.PORT);
        }
        return this;
    }

    /**
     * Set the port that will be used to build the final URL.
     * @param port The port that will be used to build the final URL.
     * @return This UrlBuilder so that multiple setters can be chained together.
     */
    public UrlBuilder withPort(int port) {
        return withPort(Integer.toString(port));
    }

    /**
     * Get the port that has been assigned to this UrlBuilder.
     * @return the port that has been assigned to this UrlBuilder.
     */
    public Integer port() {
        return port == null ? null : Integer.valueOf(port);
    }

    /**
     * Set the path that will be used to build the final URL.
     * @param path The path that will be used to build the final URL.
     * @return This UrlBuilder so that multiple setters can be chained together.
     */
    public UrlBuilder withPath(String path) {
        if (path == null || path.isEmpty()) {
            this.path = null;
        }
        else {
            with(path, UrlTokenizerState.PATH);
        }
        return this;
    }

    /**
     * Get the path that has been assigned to this UrlBuilder.
     * @return the path that has been assigned to this UrlBuilder.
     */
    public String path() {
        return path;
    }

    /**
     * Set the provided query parameter name and encoded value to query string for the final URL.
     * @param queryParameterName The name of the query parameter.
     * @param queryParameterEncodedValue The encoded value of the query parameter.
     * @return The provided query parameter name and encoded value to query string for the final
     * URL.
     */
    public UrlBuilder setQueryParameter(String queryParameterName, String queryParameterEncodedValue) {
        query.put(queryParameterName, queryParameterEncodedValue);
        return this;
    }

    /**
     * Set the query that will be used to build the final URL.
     * @param query The query that will be used to build the final URL.
     * @return This UrlBuilder so that multiple setters can be chained together.
     */
    public UrlBuilder withQuery(String query) {
        if (query == null || query.isEmpty()) {
            this.query.clear();
        }
        else {
            with(query, UrlTokenizerState.QUERY);
        }
        return this;
    }

    /**
     * Get the query that has been assigned to this UrlBuilder.
     * @return the query that has been assigned to this UrlBuilder.
     */
    public Map<String, String> query() {
        return query;
    }

    private UrlBuilder with(String text, UrlTokenizerState startState) {
        final UrlTokenizer tokenizer = new UrlTokenizer(text, startState);

        while (tokenizer.next()) {
            final UrlToken token = tokenizer.current();
            final String tokenText = token.text();
            final UrlTokenType tokenType = token.type();
            switch (tokenType) {
                case SCHEME:
                    scheme = emptyToNull(tokenText);
                    break;

                case HOST:
                    host = emptyToNull(tokenText);
                    break;

                case PORT:
                    port = emptyToNull(tokenText);
                    break;

                case PATH:
                    final String tokenPath = emptyToNull(tokenText);
                    if (path == null || path.equals("/") || !tokenPath.equals("/")) {
                        path = tokenPath;
                    }
                    break;

                case QUERY:
                    String queryString = emptyToNull(tokenText);
                    if (queryString != null) {
                        if (queryString.startsWith("?")) {
                            queryString = queryString.substring(1);
                        }

                        for (String entry : queryString.split("&")) {
                            String[] nameValue = entry.split("=");
                            if (nameValue.length == 2) {
                                setQueryParameter(nameValue[0], nameValue[1]);
                            } else {
                                throw new IllegalArgumentException("Malformed query entry: " + entry);
                            }
                        }
                    }

                    break;

                default:
                    break;
            }
        }
        return this;
    }

    /**
     * Get the URL that is being built.
     * @return The URL that is being built.
     * @throws MalformedURLException if the URL is not fully formed.
     */
    public URL toURL() throws MalformedURLException {
        return new URL(toString());
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

        if (!query.isEmpty()) {
            StringBuilder queryBuilder = new StringBuilder("?");
            for (Map.Entry<String, String> entry : query.entrySet()) {
                if (queryBuilder.length() > 1) {
                    queryBuilder.append("&");
                }
                queryBuilder.append(entry.getKey());
                queryBuilder.append("=");
                queryBuilder.append(entry.getValue());
            }

            result.append(queryBuilder.toString());
        }

        return result.toString();
    }

    /**
     * Parse a UrlBuilder from the provided URL string.
     * @param url The string to parse.
     * @return The UrlBuilder that was parsed from the string.
     */
    public static UrlBuilder parse(String url) {
        final UrlBuilder result = new UrlBuilder();
        result.with(url, UrlTokenizerState.SCHEME_OR_HOST);
        return result;
    }

    /**
     * Parse a UrlBuilder from the provided URL object.
     * @param url The URL object to parse.
     * @return The UrlBuilder that was parsed from the URL object.
     */
    public static UrlBuilder parse(URL url) {
        final UrlBuilder result = new UrlBuilder();

        if (url != null) {
            final String protocol = url.getProtocol();
            if (protocol != null && !protocol.isEmpty()) {
                result.withScheme(protocol);
            }

            final String host = url.getHost();
            if (host != null && !host.isEmpty()) {
                result.withHost(host);
            }

            final int port = url.getPort();
            if (port != -1) {
                result.withPort(port);
            }

            final String path = url.getPath();
            if (path != null && !path.isEmpty()) {
                result.withPath(path);
            }

            final String query = url.getQuery();
            if (query != null && !query.isEmpty()) {
                result.withQuery(query);
            }
        }

        return result;
    }

    private static String emptyToNull(String value) {
        return value == null || value.isEmpty() ? null : value;
    }
}
