// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.implementation.http;

import com.azure.core.v2.util.CoreUtils;
import io.clientcore.core.implementation.util.ImplUtils;
import io.clientcore.core.implementation.util.UrlBuilder;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import static io.clientcore.core.implementation.util.LoggingKeys.REDACTED_PLACEHOLDER;

/**
 * Sanitizes URLs by redacting query parameters based on a configured allowlist.
 */
public final class UrlSanitizer {
    static final Set<String> DEFAULT_QUERY_PARAMS_ALLOWLIST
        = Collections.unmodifiableSet(new HashSet<>(Collections.singletonList("api-version")));
    private final Set<String> allowedQueryParamNames;

    /**
     * Creates a new instance of UrlSanitizer with the default allowlist.
     *
     * @param allowedQueryParamNames A collection of query parameter names that should not be redacted.
     */
    public UrlSanitizer(Collection<String> allowedQueryParamNames) {
        if (allowedQueryParamNames == null) {
            this.allowedQueryParamNames = DEFAULT_QUERY_PARAMS_ALLOWLIST;
        } else {
            this.allowedQueryParamNames = allowedQueryParamNames.stream()
                .map(queryParamName -> queryParamName.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
            this.allowedQueryParamNames.addAll(DEFAULT_QUERY_PARAMS_ALLOWLIST);
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
            if (allowedQueryParamNames.contains(queryParam.getKey().toLowerCase(Locale.ROOT))) {
                urlBuilder.addQueryParameter(queryParam.getKey(), queryParam.getValue());
            } else {
                urlBuilder.addQueryParameter(queryParam.getKey(), REDACTED_PLACEHOLDER);
            }
        });

        return urlBuilder.toString();
    }
}
