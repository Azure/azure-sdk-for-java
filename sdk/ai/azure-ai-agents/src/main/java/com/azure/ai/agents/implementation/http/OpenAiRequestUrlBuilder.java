// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation.http;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.openai.core.http.HttpRequest;
import com.openai.core.http.QueryParams;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.StringJoiner;

/**
 * Utility methods that reconstruct the absolute {@link URL} required by the Azure pipeline from the
 * OpenAI request metadata. The builder keeps the low-level path/query handling isolated so that
 * {@link HttpClientHelper} can focus on the higher-level request mapping logic.
 */
final class OpenAiRequestUrlBuilder {

    private static final ClientLogger LOGGER = new ClientLogger(OpenAiRequestUrlBuilder.class);

    private OpenAiRequestUrlBuilder() {
    }

    /**
     * Builds an absolute {@link URL} using the base URL, path segments, and query parameters that are stored in the
     * OpenAI {@link HttpRequest} abstraction.
     *
     * @param request Source request provided by the OpenAI client.
     * @return Absolute URL that can be consumed by Azure HTTP components.
     */
    static URL buildUrl(HttpRequest request) {
        try {
            URI baseUri = URI.create(request.baseUrl());
            URL baseUrl = baseUri.toURL();
            String path = buildPath(baseUrl.getPath(), request.pathSegments());
            String query = buildQueryString(request.queryParams());
            URI resolved = new URI(baseUrl.getProtocol(), baseUrl.getUserInfo(), baseUrl.getHost(), baseUrl.getPort(),
                path, query, null);
            return resolved.toURL();
        } catch (MalformedURLException | URISyntaxException ex) {
            throw LOGGER.logThrowableAsWarning(
                new IllegalStateException(
                    "Failed to build Azure HTTP request URL from base: " + request.baseUrl(), ex));
        }
    }

    /**
     * Creates a normalized path that merges the OpenAI base path with the additional path segments present on the
     * request.
     */
    private static String buildPath(String basePath, List<String> pathSegments) {
        StringBuilder builder = new StringBuilder();
        String normalizedBasePath = normalizeBasePath(basePath);
        if (!CoreUtils.isNullOrEmpty(normalizedBasePath)) {
            builder.append(normalizedBasePath);
        }

        for (String segment : pathSegments) {
            if (builder.length() == 0 || builder.charAt(builder.length() - 1) != '/') {
                builder.append('/');
            }
            if (segment != null) {
                builder.append(segment);
            }
        }

        return builder.length() == 0 ? "/" : builder.toString();
    }

    /**
     * Normalizes the base path ensuring trailing slashes are removed and {@code null} inputs result in an empty path.
     */
    private static String normalizeBasePath(String basePath) {
        if (CoreUtils.isNullOrEmpty(basePath)) {
            return "";
        }
        if ("/".equals(basePath)) {
            return "";
        }
        return trimTrailingSlash(basePath);
    }

    /**
     * Removes the final {@code '/'} character when present so that subsequent concatenation does not duplicate
     * separators.
     */
    private static String trimTrailingSlash(String value) {
        if (value == null) {
            return null;
        }
        int length = value.length();
        if (length == 0) {
            return value;
        }
        return value.charAt(length - 1) == '/' ? value.substring(0, length - 1) : value;
    }

    /**
     * Converts OpenAI {@link QueryParams} into a flattened query string. Encoding is deferred to {@link URI} so we do
     * not double-encode values already escaped by upstream layers.
     */
    private static String buildQueryString(QueryParams queryParams) {
        if (queryParams == null || queryParams.isEmpty()) {
            return null;
        }

        StringJoiner joiner = new StringJoiner("&");
        queryParams.keys().forEach(name -> {
            List<String> values = queryParams.values(name);
            if (values.isEmpty()) {
                joiner.add(name);
            } else {
                values.forEach(value -> joiner.add(formatQueryComponent(name, value)));
            }
        });
        String query = joiner.toString();
        return query.isEmpty() ? null : query;
    }

    /**
     * Formats a single query component using {@code name=value} semantics, handling parameters that omit a value.
     */
    private static String formatQueryComponent(String name, String value) {
        if (value == null) {
            return name;
        }
        return name + "=" + value;
    }
}
