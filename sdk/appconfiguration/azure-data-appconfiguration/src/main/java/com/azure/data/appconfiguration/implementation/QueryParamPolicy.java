// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.implementation;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.policy.HttpPipelineSyncPolicy;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.UrlBuilder;
import com.azure.core.util.logging.ClientLogger;

public final class QueryParamPolicy extends HttpPipelineSyncPolicy {
    private static final ClientLogger LOGGER = new ClientLogger(QueryParamPolicy.class);

    @Override
    protected void beforeSendingRequest(HttpPipelineCallContext context) {
        HttpRequest httpRequest = context.getHttpRequest();

        try {
            UrlBuilder builder = UrlBuilder.parse(httpRequest.getUrl());
            String queryString = builder.getQueryString();
            builder.clearQuery();
            TreeMap<String, List<String>> orderedQuery = new TreeMap<>(String::compareTo);
            CoreUtils.parseQueryParameters(queryString)
                .forEachRemaining(kvp -> orderedQuery.compute(kvp.getKey(), (ignored, values) -> {
                    if (values == null) {
                        values = new ArrayList<>();
                    }
                    values.add(kvp.getValue());
                    return values;
                }));
            for (Map.Entry<String, List<String>> ordered : orderedQuery.entrySet()) {
                for (String val : ordered.getValue()) {
                    builder.addQueryParameter(ordered.getKey(), val);
                }
            }
            httpRequest.setUrl(builder.toUrl().toString());
        } catch (IllegalArgumentException | MalformedURLException e) {
            // If the constructed URL is invalid when setting it, continue without modification
            LOGGER.warning(
                "Failed to set normalized URL due to invalid format. "
                    + "Request will proceed with original URL. URL: {}, Error: {}",
                httpRequest.getUrl(), e.getMessage(), e);
        }
    }

}
