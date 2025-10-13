// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.implementation;

import java.util.Arrays;
import java.util.stream.Collectors;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.policy.HttpPipelineSyncPolicy;
import com.azure.core.util.logging.ClientLogger;

public final class QueryParamPolicy extends HttpPipelineSyncPolicy {
    private static final ClientLogger LOGGER = new ClientLogger(QueryParamPolicy.class);

    @Override
    protected void beforeSendingRequest(HttpPipelineCallContext context) {
        HttpRequest request = context.getHttpRequest();

        try {
            String originalUrl = request.getUrl().toString();
            String normalizedUrl = normalizeQueryParameters(originalUrl);
            
            if (normalizedUrl != null && !normalizedUrl.equals(originalUrl)) {
                request.setUrl(normalizedUrl);
            }
        } catch (IllegalArgumentException e) {
            // If the constructed URL is invalid when setting it, continue without modification
            LOGGER.warning(
                "Failed to set normalized URL due to invalid format. "
                + "Request will proceed with original URL. URL: {}, Error: {}",
                request.getUrl(), e.getMessage(), e);
        }
    }

    /**
     * Normalizes query parameters in a URL by converting parameter names to lowercase and sorting them.
     * Preserves URL encoding of parameter values.
     *
     * @param url the URL to normalize
     * @return the normalized URL, or the original URL if no query parameters exist, or null if normalization fails
     */
    static String normalizeQueryParameters(String url) {
        if (url == null) {
            return url;
        }

        try {
            // Find the query string manually to preserve URL encoding
            int queryIndex = url.indexOf('?');
            if (queryIndex == -1) {
                return url;
            }

            // Check for fragment after query string
            int fragmentIndex = url.indexOf('#', queryIndex);
            String fragment = "";
            String query;
            
            if (fragmentIndex != -1) {
                query = url.substring(queryIndex + 1, fragmentIndex);
                fragment = url.substring(fragmentIndex); // Include the '#'
            } else {
                query = url.substring(queryIndex + 1);
            }
            
            if (query.isEmpty()) {
                return url;
            }

            String normalizedQuery = Arrays.stream(query.split("&"))
                .filter(pair -> !pair.isEmpty())
                .map(pair -> {
                    int equalIndex = pair.indexOf('=');
                    return equalIndex != -1
                        ? pair.substring(0, equalIndex).toLowerCase() + "=" + pair.substring(equalIndex + 1)
                        : pair.toLowerCase();
                })
                .sorted()
                .collect(Collectors.joining("&"));

            String urlWithoutQuery = url.substring(0, queryIndex);
            return urlWithoutQuery + "?" + normalizedQuery + fragment;
        } catch (IndexOutOfBoundsException e) {
            // If string manipulation fails due to invalid indices, return null to indicate failure
            LOGGER.warning(
                "Failed to parse URL for query parameter normalization due to string manipulation error. "
                    + "URL: {}, Error: {}",
                url, e.getMessage(), e);
            return null;
        } catch (NullPointerException e) {
            // If string operations fail on null, return null to indicate failure
            LOGGER.warning(
                "Failed to parse URL for query parameter normalization due to null value. "
                + "Error: {}",
                e.getMessage(), e);
            return null;
        }
    }
}
