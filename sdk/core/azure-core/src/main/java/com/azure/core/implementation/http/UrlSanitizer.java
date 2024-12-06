// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.http;

import com.azure.core.implementation.ImplUtils;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.UrlBuilder;

import java.net.URL;
import java.util.Collection;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.azure.core.implementation.logging.LoggingKeys.REDACTED_PLACEHOLDER;

/**
 * Sanitizes URLs by redacting query parameters based on a configured allowlist.
 */
public final class UrlSanitizer {
    private final Predicate<String> canLogQueryParam;

    /**
     * Creates a new instance of UrlSanitizer with the default allowlist.
     *
     * @param allowedQueryParamNames A collection of query parameter names that should not be redacted.
     */
    public UrlSanitizer(Collection<String> allowedQueryParamNames) {
        if (CoreUtils.isNullOrEmpty(allowedQueryParamNames)) {
            this.canLogQueryParam = "api-version"::equalsIgnoreCase;
        } else {
            Set<String> lowercasedAllowedQueryParamNames = allowedQueryParamNames.stream()
                .map(queryParamName -> queryParamName.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
            lowercasedAllowedQueryParamNames.add("api-version");
            this.canLogQueryParam
                = paramName -> lowercasedAllowedQueryParamNames.contains(paramName.toLowerCase(Locale.ROOT));
        }
    }

    /**
     * Generates the redacted URL for logging or tracing.
     *
     * @param url URL where the request is being sent.
     *
     * @return A URL with query parameters redacted based on configured allowlist
     */
    public String getRedactedUrl(URL url) {
        String query = url.getQuery();
        if (CoreUtils.isNullOrEmpty(query)) {
            return url.toString();
        }

        // URL does have a query string that may need redactions.
        // Use UrlBuilder to break apart the URL, clear the query string, and add the redacted query string.
        UrlBuilder urlBuilder = ImplUtils.parseUrl(url, false);

        CoreUtils.parseQueryParameters(query).forEachRemaining(queryParam -> {
            if (canLogQueryParam.test(queryParam.getKey())) {
                urlBuilder.addQueryParameter(queryParam.getKey(), queryParam.getValue());
            } else {
                urlBuilder.addQueryParameter(queryParam.getKey(), REDACTED_PLACEHOLDER);
            }
        });

        return urlBuilder.toString();
    }
}
