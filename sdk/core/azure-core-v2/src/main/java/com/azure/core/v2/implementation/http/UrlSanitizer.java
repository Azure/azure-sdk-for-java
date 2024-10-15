// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.implementation.http;

import com.azure.core.v2.util.CoreUtils;
import io.clientcore.core.implementation.util.ImplUtils;
import io.clientcore.core.implementation.util.UriBuilder;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Sanitizes URLs by redacting query parameters based on a configured allowlist.
 */
public final class UrlSanitizer {
    static final Set<String> DEFAULT_QUERY_PARAMS_ALLOWLIST
        = Collections.unmodifiableSet(new HashSet<>(Collections.singletonList("api-version")));
    private final Set<String> allowedQueryParamNames;
    private static final String REDACTED_PLACEHOLDER = "REDACTED";

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
     * @param uri URI where the request is being sent.
     *
     * @return A URI with query parameters redacted based on configured allowlist
     */
    public String getRedactedUrl(URI uri) {
        String query = uri.getQuery();
        if (ImplUtils.isNullOrEmpty(query)) {
            return uri.toString();
        }

        // URL does have a query string that may need redactions.
        // Use UrlBuilder to break apart the URL, clear the query string, and add the redacted query string.
        UriBuilder urlBuilder = ImplUtils.parseUri(uri, false);

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
