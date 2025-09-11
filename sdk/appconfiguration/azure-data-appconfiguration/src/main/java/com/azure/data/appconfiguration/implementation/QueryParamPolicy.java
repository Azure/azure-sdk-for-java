// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.implementation;

import java.net.MalformedURLException;
import java.util.Map;
import java.util.TreeMap;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.UrlBuilder;
import com.azure.core.util.logging.ClientLogger;

import reactor.core.publisher.Mono;

public class QueryParamPolicy implements HttpPipelinePolicy {
    private static final ClientLogger LOGGER = new ClientLogger(QueryParamPolicy.class);

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        HttpRequest request = context.getHttpRequest();

        try {
            UrlBuilder urlBuilder = UrlBuilder.parse(request.getUrl());
            Map<String, String> queryParams = urlBuilder.getQuery();

            if (queryParams != null && !queryParams.isEmpty()) {
                // Create a new TreeMap to automatically sort by keys alphabetically
                Map<String, String> sortedParams = new TreeMap<>();

                // Process each query parameter: convert key to lowercase and add to sorted map
                for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();

                    // Convert key to lowercase, but preserve special cases like $Select -> $select
                    String lowercaseKey = key.toLowerCase();
                    sortedParams.put(lowercaseKey, value);
                }

                // Clear existing query parameters and add sorted ones
                urlBuilder.setQuery(null);
                for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
                    urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
                }

                // Update the request URL with reordered parameters
                request.setUrl(urlBuilder.toUrl());
            }
        } catch (MalformedURLException e) {
            // If URL parsing fails, continue without modification
            LOGGER.warning(
                "Failed to parse URL for query parameter normalization. "
                    + "Request will proceed with original URL. URL: {}, Error: {}",
                request.getUrl(), e.getMessage(), e);
        }

        return next.process();
    }

}
