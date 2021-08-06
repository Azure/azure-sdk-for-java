// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A builder class that is used to create URLs.
 */
public final class UrlBuilder {
    private static final Map<String, UrlBuilder> PARSED_URLS = new ConcurrentHashMap<>();

    // future improvement - make this configurable
    private static final int MAX_CACHE_SIZE = 10000;

    private String scheme;
    private String host;
    private String port;
    private String path;

    // LinkedHashMap preserves insertion order
    private final Map<String, String> query = new LinkedHashMap<>();

    /**
     * Set the scheme/protocol that will be used to build the final URL.
     *
     * @param scheme The scheme/protocol that will be used to build the final URL.
     * @return This UrlBuilder so that multiple setters can be chained together.
     */
    public UrlBuilder setScheme(String scheme) {
        if (scheme == null || scheme.isEmpty()) {
            this.scheme = null;
        } else {
            with(scheme, UrlTokenizerState.SCHEME);
        }
        return this;
    }

    /**
     * Get the scheme/protocol that has been assigned to this UrlBuilder.
     *
     * @return the scheme/protocol that has been assigned to this UrlBuilder.
     */
    public String getScheme() {
        return scheme;
    }

    /**
     * Set the host that will be used to build the final URL.
     *
     * @param host The host that will be used to build the final URL.
     * @return This UrlBuilder so that multiple setters can be chained together.
     */
    public UrlBuilder setHost(String host) {
        if (host == null || host.isEmpty()) {
            this.host = null;
        } else {
            with(host, UrlTokenizerState.SCHEME_OR_HOST);
        }
        return this;
    }

    /**
     * Get the host that has been assigned to this UrlBuilder.
     *
     * @return the host that has been assigned to this UrlBuilder.
     */
    public String getHost() {
        return host;
    }

    /**
     * Set the port that will be used to build the final URL.
     *
     * @param port The port that will be used to build the final URL.
     * @return This UrlBuilder so that multiple setters can be chained together.
     */
    public UrlBuilder setPort(String port) {
        if (port == null || port.isEmpty()) {
            this.port = null;
        } else {
            with(port, UrlTokenizerState.PORT);
        }
        return this;
    }

    /**
     * Set the port that will be used to build the final URL.
     *
     * @param port The port that will be used to build the final URL.
     * @return This UrlBuilder so that multiple setters can be chained together.
     */
    public UrlBuilder setPort(int port) {
        return setPort(Integer.toString(port));
    }

    /**
     * Get the port that has been assigned to this UrlBuilder.
     *
     * @return the port that has been assigned to this UrlBuilder.
     */
    public Integer getPort() {
        return port == null ? null : Integer.valueOf(port);
    }

    /**
     * Set the path that will be used to build the final URL.
     *
     * @param path The path that will be used to build the final URL.
     * @return This UrlBuilder so that multiple setters can be chained together.
     */
    public UrlBuilder setPath(String path) {
        if (path == null || path.isEmpty()) {
            this.path = null;
        } else {
            with(path, UrlTokenizerState.PATH);
        }
        return this;
    }

    /**
     * Get the path that has been assigned to this UrlBuilder.
     *
     * @return the path that has been assigned to this UrlBuilder.
     */
    public String getPath() {
        return path;
    }

    /**
     * Set the provided query parameter name and encoded value to query string for the final URL.
     *
     * @param queryParameterName The name of the query parameter.
     * @param queryParameterEncodedValue The encoded value of the query parameter.
     * @return The provided query parameter name and encoded value to query string for the final URL.
     */
    public UrlBuilder setQueryParameter(String queryParameterName, String queryParameterEncodedValue) {
        query.put(queryParameterName, queryParameterEncodedValue);
        return this;
    }

    /**
     * Set the query that will be used to build the final URL.
     *
     * @param query The query that will be used to build the final URL.
     * @return This UrlBuilder so that multiple setters can be chained together.
     */
    public UrlBuilder setQuery(String query) {
        if (query == null || query.isEmpty()) {
            this.query.clear();
        } else {
            with(query, UrlTokenizerState.QUERY);
        }
        return this;
    }

    /**
     * Get the query that has been assigned to this UrlBuilder.
     *
     * @return the query that has been assigned to this UrlBuilder.
     */
    public Map<String, String> getQuery() {
        return query;
    }

    /**
     * Returns the query string currently configured in this UrlBuilder instance.
     * @return A String containing the currently configured query string.
     */
    public String getQueryString() {
        if (query.isEmpty()) {
            return "";
        }

        StringBuilder queryBuilder = new StringBuilder("?");
        for (Map.Entry<String, String> entry : query.entrySet()) {
            if (queryBuilder.length() > 1) {
                queryBuilder.append("&");
            }
            queryBuilder.append(entry.getKey());
            queryBuilder.append("=");
            queryBuilder.append(entry.getValue());
        }

        return queryBuilder.toString();
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
                                setQueryParameter(nameValue[0], "");
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
     *
     * @return The URL that is being built.
     * @throws MalformedURLException if the URL is not fully formed.
     */
    public URL toUrl() throws MalformedURLException {
        return new URL(toString());
    }

    /**
     * Get the string representation of the URL that is being built.
     *
     * @return The string representation of the URL that is being built.
     */
    @Override
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

        result.append(getQueryString());

        return result.toString();
    }

    /**
     * Returns the map of parsed URLs and their {@link UrlBuilder UrlBuilders}
     * @return the map of parsed URLs and their {@link UrlBuilder UrlBuilders}
     */
    static Map<String, UrlBuilder> getParsedUrls() {
        return PARSED_URLS;
    }

    /**
     * Parses the passed {@code url} string into a UrlBuilder.
     *
     * @param url The URL string to parse.
     * @return The UrlBuilder that was created from parsing the passed URL string.
     */
    public static UrlBuilder parse(String url) {
        /*
         * Parsing the URL string into a UrlBuilder is a non-trivial operation and many calls into RestProxy will use
         * the same root URL string. To save CPU costs we retain a parsed version of the URL string in memory. Given
         * that UrlBuilder is mutable we must return a cloned version of the cached UrlBuilder.
         */
        // ConcurrentHashMap doesn't allow for null keys, coerce it into an empty string.
        String concurrentSafeUrl = (url == null) ? "" : url;

        // If the number of parsed urls are above threshold, clear the map and start fresh.
        // This prevents the map from growing without bounds if too many unique URLs are parsed.
        // TODO (srnagar): consider using an LRU cache to evict selectively
        if (PARSED_URLS.size() >= MAX_CACHE_SIZE) {
            PARSED_URLS.clear();
        }
        return PARSED_URLS.computeIfAbsent(concurrentSafeUrl, u ->
            new UrlBuilder().with(u, UrlTokenizerState.SCHEME_OR_HOST)).copy();
    }

    /**
     * Parse a UrlBuilder from the provided URL object.
     *
     * @param url The URL object to parse.
     * @return The UrlBuilder that was parsed from the URL object.
     */
    public static UrlBuilder parse(URL url) {
        final UrlBuilder result = new UrlBuilder();

        if (url != null) {
            final String protocol = url.getProtocol();
            if (protocol != null && !protocol.isEmpty()) {
                result.setScheme(protocol);
            }

            final String host = url.getHost();
            if (host != null && !host.isEmpty()) {
                result.setHost(host);
            }

            final int port = url.getPort();
            if (port != -1) {
                result.setPort(port);
            }

            final String path = url.getPath();
            if (path != null && !path.isEmpty()) {
                result.setPath(path);
            }

            final String query = url.getQuery();
            if (query != null && !query.isEmpty()) {
                result.setQuery(query);
            }
        }

        return result;
    }

    private static String emptyToNull(String value) {
        return value == null || value.isEmpty() ? null : value;
    }

    private UrlBuilder copy() {
        UrlBuilder copy = new UrlBuilder();

        copy.scheme = this.scheme;
        copy.host = this.host;
        copy.path = this.path;
        copy.port = this.port;
        copy.query.putAll(this.query);

        return copy;
    }
}
