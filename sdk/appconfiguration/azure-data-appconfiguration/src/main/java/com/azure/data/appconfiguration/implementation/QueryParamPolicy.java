// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.implementation;

import java.util.Arrays;
import java.util.stream.Collectors;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.logging.ClientLogger;

import reactor.core.publisher.Mono;

public class QueryParamPolicy implements HttpPipelinePolicy {
    private static final ClientLogger LOGGER = new ClientLogger(QueryParamPolicy.class);

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        HttpRequest request = context.getHttpRequest();

        try {
            String url = request.getUrl().toString();

            // Find the query string manually to preserve URL encoding
            int queryIndex = url.indexOf('?');
            if (queryIndex != -1) {
                String query = url.substring(queryIndex + 1);

                if (!query.isEmpty()) {
                    String normalizedQuery
                        = Arrays.stream(query.split("&")).filter(pair -> !pair.isEmpty()).map(pair -> {
                            int equalIndex = pair.indexOf('=');
                            return equalIndex != -1
                                ? pair.substring(0, equalIndex).toLowerCase() + "=" + pair.substring(equalIndex + 1)
                                : pair.toLowerCase() + "=";
                        }).sorted().collect(Collectors.joining("&"));
                    String urlWithoutQuery = url.substring(0, queryIndex);
                    String newUrl = urlWithoutQuery + "?" + normalizedQuery;
                    request.setUrl(newUrl);
                }
            }
        } catch (Exception e) {
            // If URL parsing fails, continue without modification
            LOGGER.warning(
                "Failed to parse URL for query parameter normalization. "
                    + "Request will proceed with original URL. URL: {}, Error: {}",
                request.getUrl(), e.getMessage(), e);
        }

        return next.process();
    }
}
