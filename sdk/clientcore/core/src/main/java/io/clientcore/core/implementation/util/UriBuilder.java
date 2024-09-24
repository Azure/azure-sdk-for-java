// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static io.clientcore.core.implementation.util.ImplUtils.isNullOrEmpty;

/**
 * A builder class that is used to create URIs.
 */
public final class UriBuilder {
    private static final Map<String, UriBuilder> PARSED_URIS = new ConcurrentHashMap<>();
    private static final int MAX_CACHE_SIZE = 10000;

    private String scheme;
    private String host;
    private Integer port;
    private String path;

    private Map<String, QueryParameter> queryToCopy;
    private Map<String, QueryParameter> query;

    /**
     * Creates a new instance of {@link UriBuilder}.
     */
    public UriBuilder() {
        this(null);
    }

    private UriBuilder(Map<String, QueryParameter> queryToCopy) {
        this.queryToCopy = queryToCopy;
    }

    /**
     * Set the scheme/protocol that will be used to build the final URI.
     *
     * @param scheme The scheme/protocol that will be used to build the final URI.
     * @return This UriBuilder so that multiple setters can be chained together.
     */
    public UriBuilder setScheme(String scheme) {
        if (scheme == null || scheme.isEmpty()) {
            this.scheme = null;
        } else {
            with(scheme, UriTokenizerState.SCHEME);
        }
        return this;
    }

    /**
     * Get the scheme/protocol that has been assigned to this UriBuilder.
     *
     * @return the scheme/protocol that has been assigned to this UriBuilder.
     */
    public String getScheme() {
        return scheme;
    }

    /**
     * Set the host that will be used to build the final URI.
     *
     * @param host The host that will be used to build the final URI.
     * @return This UriBuilder so that multiple setters can be chained together.
     */
    public UriBuilder setHost(String host) {
        if (host == null || host.isEmpty()) {
            this.host = null;
        } else {
            with(host, UriTokenizerState.SCHEME_OR_HOST);
        }
        return this;
    }

    /**
     * Get the host that has been assigned to this UriBuilder.
     *
     * @return the host that has been assigned to this UriBuilder.
     */
    public String getHost() {
        return host;
    }

    /**
     * Set the port that will be used to build the final URI.
     *
     * @param port The port that will be used to build the final URI.
     * @return This UriBuilder so that multiple setters can be chained together.
     */
    public UriBuilder setPort(String port) {
        if (isNullOrEmpty(port)) {
            this.port = null;
            return this;
        }

        return with(port, UriTokenizerState.PORT);
    }

    /**
     * Set the port that will be used to build the final URI.
     *
     * @param port The port that will be used to build the final URI.
     * @return This UriBuilder so that multiple setters can be chained together.
     */
    public UriBuilder setPort(int port) {
        this.port = port;
        return this;
    }

    /**
     * Get the port that has been assigned to this UriBuilder.
     *
     * @return the port that has been assigned to this UriBuilder.
     */
    public Integer getPort() {
        return port;
    }

    /**
     * Set the path that will be used to build the final URI.
     *
     * @param path The path that will be used to build the final URI.
     * @return This UriBuilder so that multiple setters can be chained together.
     */
    public UriBuilder setPath(String path) {
        if (path == null || path.isEmpty()) {
            this.path = null;
        } else {
            with(path, UriTokenizerState.PATH);
        }
        return this;
    }

    /**
     * Get the path that has been assigned to this UriBuilder.
     *
     * @return the path that has been assigned to this UriBuilder.
     */
    public String getPath() {
        return path;
    }

    /**
     * Set the provided query parameter name and encoded value to query string for the final URI.
     *
     * @param queryParameterName The name of the query parameter.
     * @param queryParameterEncodedValue The encoded value of the query parameter.
     * @return The provided query parameter name and encoded value to query string for the final URI.
     * @throws NullPointerException if {@code queryParameterName} or {@code queryParameterEncodedValue} are null.
     */
    public UriBuilder setQueryParameter(String queryParameterName, String queryParameterEncodedValue) {
        initializeQuery();

        query.put(queryParameterName, new QueryParameter(queryParameterName, queryParameterEncodedValue));
        return this;
    }

    /**
     * Append the provided query parameter name and encoded value to query string for the final URI.
     *
     * @param queryParameterName The name of the query parameter.
     * @param queryParameterEncodedValue The encoded value of the query parameter.
     * @return The provided query parameter name and encoded value to query string for the final URI.
     * @throws NullPointerException if {@code queryParameterName} or {@code queryParameterEncodedValue} are null.
     */
    public UriBuilder addQueryParameter(String queryParameterName, String queryParameterEncodedValue) {
        initializeQuery();

        query.compute(queryParameterName, (key, value) -> {
            if (value == null) {
                return new QueryParameter(queryParameterName, queryParameterEncodedValue);
            }
            value.addValue(queryParameterEncodedValue);
            return value;
        });
        return this;
    }

    /**
     * Set the query that will be used to build the final URI.
     *
     * @param query The query that will be used to build the final URI.
     * @return This UriBuilder so that multiple setters can be chained together.
     */
    public UriBuilder setQuery(String query) {
        return (query == null || query.isEmpty()) ? clearQuery() : with(query, UriTokenizerState.QUERY);
    }

    /**
     * Clear the query that will be used to build the final URI.
     *
     * @return This UriBuilder so that multiple setters can be chained together.
     */
    public UriBuilder clearQuery() {
        if (isNullOrEmpty(query)) {
            return this;
        }

        query.clear();
        return this;
    }

    /**
     * Get a view of the query that has been assigned to this UriBuilder.
     * <p>
     * Changes to the {@link Map} returned by this API won't be reflected in the UriBuilder.
     *
     * @return A view of the query that has been assigned to this UriBuilder.
     */
    public Map<String, String> getQuery() {
        initializeQuery();

        // This contains a map of key=value query parameters, replacing
        // multiple values for a single key with a list of values under the same name,
        // joined together with a comma. As discussed in https://github.com/Azure/azure-sdk-for-java/pull/21203.
        return query.entrySet().stream()
            // get all parameters joined by a comma.
            // name=a&name=b&name=c becomes name=a,b,c
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getValue()));
    }

    /**
     * Returns the query string currently configured in this UriBuilder instance.
     *
     * @return A String containing the currently configured query string.
     */
    public String getQueryString() {
        if (isNullOrEmpty(queryToCopy) && isNullOrEmpty(query)) {
            return "";
        }

        StringBuilder queryBuilder = new StringBuilder();
        appendQueryString(queryBuilder);

        return queryBuilder.toString();
    }

    private void appendQueryString(StringBuilder stringBuilder) {
        if (isNullOrEmpty(queryToCopy) && isNullOrEmpty(query)) {
            return;
        }

        stringBuilder.append('?');

        boolean first = true;

        // queryToCopy hasn't been copied yet as no operations on query parameters have been applied since creating
        // this UriBuilder. Use queryToCopy to create the query string and while doing so copy it into query.
        if (query == null) {
            query = new LinkedHashMap<>(queryToCopy.size());

            for (Map.Entry<String, QueryParameter> entry : queryToCopy.entrySet()) {
                first = writeQueryValues(stringBuilder, entry.getKey(), entry.getValue().getValuesList(), first);

                query.put(entry.getKey(), entry.getValue());
            }
        } else {
            // queryToCopy has been copied, use query to build the query string.
            for (Map.Entry<String, QueryParameter> entry : query.entrySet()) {
                first = writeQueryValues(stringBuilder, entry.getKey(), entry.getValue().getValuesList(), first);
            }
        }
    }

    private static boolean writeQueryValues(StringBuilder builder, String key, List<String> values, boolean first) {
        if (isNullOrEmpty(values)) {
            if (!first) {
                builder.append('&');
            }

            builder.append(key);

            first = false;
        } else {
            for (String value : values) {
                if (!first) {
                    builder.append('&');
                }

                builder.append(key).append('=').append(value);

                first = false;
            }
        }

        return first;
    }

    private UriBuilder with(String text, UriTokenizerState startState) {
        final UriTokenizer tokenizer = new UriTokenizer(text, startState);

        while (tokenizer.next()) {
            final UriToken token = tokenizer.current();
            final String tokenText = emptyToNull(token.text());
            final UriTokenType tokenType = token.type();
            switch (tokenType) {
                case SCHEME:
                    scheme = tokenText;
                    break;

                case HOST:
                    host = tokenText;
                    break;

                case PORT:
                    port = tokenText == null ? null : Integer.parseInt(tokenText);
                    break;

                case PATH:
                    if (path == null || "/".equals(path) || !"/".equals(tokenText)) {
                        path = tokenText;
                    }
                    break;

                case QUERY:
                    parseQueryParameters(tokenText).forEachRemaining(
                        queryParam -> addQueryParameter(queryParam.getKey(), queryParam.getValue()));
                    break;

                default:
                    break;
            }
        }
        return this;
    }

    /**
     * Get the URI that is being built.
     *
     * @return The URI that is being built.
     * @throws URISyntaxException if the URI is not fully formed.
     */
    public URI toUri() throws URISyntaxException {
        // Continue using new URI constructor here as URI either cannot accept certain characters in the path or
        // escapes '/', depending on the API used to create the URI.
        return new URI(toString());
    }

    /**
     * Get the string representation of the URI that is being built.
     *
     * @return The string representation of the URI that is being built.
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
            result.append(':');
            result.append(port);
        }

        if (path != null) {
            if (result.length() != 0 && !path.startsWith("/")) {
                result.append('/');
            }
            result.append(path);
        }

        appendQueryString(result);

        return result.toString();
    }

    /**
     * Returns the map of parsed URIs and their {@link UriBuilder UriBuilders}
     *
     * @return the map of parsed URIs and their {@link UriBuilder UriBuilders}
     */
    static Map<String, UriBuilder> getParsedUris() {
        return PARSED_URIS;
    }

    /**
     * Parses the passed {@code uri} string into a UriBuilder.
     *
     * @param uri The URI string to parse.
     * @return The UriBuilder that was created from parsing the passed URI string.
     */
    public static UriBuilder parse(String uri) {
        /*
         * Parsing the URI string into a UriBuilder is a non-trivial operation and many calls into RestProxy will use
         * the same root URI string. To save CPU costs we retain a parsed version of the URI string in memory. Given
         * that UriBuilder is mutable we must return a cloned version of the cached UriBuilder.
         */
        // ConcurrentHashMap doesn't allow for null keys, coerce it into an empty string.
        String concurrentSafeUri = (uri == null) ? "" : uri;

        // If the number of parsed uris are above threshold, clear the map and start fresh.
        // This prevents the map from growing without bounds if too many unique URIs are parsed.
        // TODO (srnagar): consider using an LRU cache to evict selectively
        if (PARSED_URIS.size() >= MAX_CACHE_SIZE) {
            PARSED_URIS.clear();
        }
        return PARSED_URIS.computeIfAbsent(concurrentSafeUri,
            u -> new UriBuilder().with(u, UriTokenizerState.SCHEME_OR_HOST)).copy();
    }

    /**
     * Parse a UriBuilder from the provided URI object.
     *
     * @param uri The URI object to parse.
     * @return The UriBuilder that was parsed from the URI object.
     */
    public static UriBuilder parse(URI uri) {
        return parseUri(uri, true);
    }

    /**
     * Utility method for parsing a {@link URI} into a {@link UriBuilder}.
     *
     * @param uri The URI being parsed.
     * @param includeQuery Whether the query string should be excluded.
     * @return The UriBuilder that represents the parsed URI.
     */
    private static UriBuilder parseUri(URI uri, boolean includeQuery) {
        final UriBuilder result = new UriBuilder();

        if (uri != null) {
            String scheme = uri.getScheme();
            if (scheme != null && !scheme.isEmpty()) {
                result.setScheme(scheme);
            }

            final String host = uri.getHost();
            if (host != null && !host.isEmpty()) {
                result.setHost(host);
            }

            final int port = uri.getPort();
            if (port != -1) {
                result.setPort(port);
            }

            final String path = uri.getPath();
            if (path != null && !path.isEmpty()) {
                result.setPath(path);
            }

            final String query = uri.getQuery();
            if (query != null && !query.isEmpty() && includeQuery) {
                result.setQuery(query);
            }
        }

        return result;
    }

    private static String emptyToNull(String value) {
        return value == null || value.isEmpty() ? null : value;
    }

    private UriBuilder copy() {
        UriBuilder copy = new UriBuilder(query);

        copy.scheme = this.scheme;
        copy.host = this.host;
        copy.path = this.path;
        copy.port = this.port;

        return copy;
    }

    private void initializeQuery() {
        if (query == null) {
            query = new LinkedHashMap<>();
        }

        if (queryToCopy != null) {
            query.putAll(queryToCopy);
            queryToCopy = null;
        }
    }

    /**
     * Utility method for parsing query parameters one-by-one without the use of string splitting.
     * <p>
     * This method provides an optimization over parsing query parameters with {@link String#split(String)} or a
     * {@link java.util.regex.Pattern} as it doesn't allocate any arrays to maintain values, instead it parses the query
     * parameters linearly.
     * <p>
     * Query parameter parsing works the following way, {@code key=value} will turn into an immutable {@link Map.Entry}
     * where the {@link Map.Entry#getKey()} is {@code key} and the {@link Map.Entry#getValue()} is {@code value}. For
     * query parameters without a value, {@code key=} or just {@code key}, the value will be an empty string.
     *
     * @param queryParameters The query parameter string.
     * @return An {@link Iterator} over the query parameter key-value pairs.
     */
    private static Iterator<Map.Entry<String, String>> parseQueryParameters(String queryParameters) {
        return (isNullOrEmpty(queryParameters))
            ? Collections.emptyIterator()
            : new QueryParameterIterator(queryParameters);
    }

    private static final class QueryParameterIterator implements Iterator<Map.Entry<String, String>> {
        private final String queryParameters;
        private final int queryParametersLength;

        private boolean done = false;
        private int position;

        QueryParameterIterator(String queryParameters) {
            this.queryParameters = queryParameters;
            this.queryParametersLength = queryParameters.length();

            // If the URI query begins with '?' the first possible start of a query parameter key is the
            // second character in the query.
            position = (queryParameters.startsWith("?")) ? 1 : 0;
        }

        @Override
        public boolean hasNext() {
            return !done;
        }

        @Override
        public Map.Entry<String, String> next() {
            if (done) {
                throw new NoSuchElementException();
            }

            int nextPosition = position;
            char c;
            while (nextPosition < queryParametersLength) {
                // Next position can either be '=' or '&' as a query parameter may not have a '=', ex 'key&key2=value'.
                c = queryParameters.charAt(nextPosition);
                if (c == '=') {
                    break;
                } else if (c == '&') {
                    String key = queryParameters.substring(position, nextPosition);

                    // Position is set to nextPosition + 1 to skip over the '&'
                    position = nextPosition + 1;

                    return new AbstractMap.SimpleImmutableEntry<>(key, "");
                }

                nextPosition++;
            }

            if (nextPosition == queryParametersLength) {
                // Query parameters completed.
                done = true;
                return new AbstractMap.SimpleImmutableEntry<>(queryParameters.substring(position), "");
            }

            String key = queryParameters.substring(position, nextPosition);

            // Position is set to nextPosition + 1 to skip over the '='
            position = nextPosition + 1;

            nextPosition = queryParameters.indexOf('&', position);

            String value = null;
            if (nextPosition == -1) {
                // This was the last key-value pair in the query parameters 'https://example.com?param=done'
                done = true;
                value = queryParameters.substring(position);
            } else {
                value = queryParameters.substring(position, nextPosition);
                // Position is set to nextPosition + 1 to skip over the '&'
                position = nextPosition + 1;
            }

            return new AbstractMap.SimpleImmutableEntry<>(key, value);
        }
    }
}
