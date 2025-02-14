// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation;

import io.clientcore.core.implementation.utils.ImplUtils;

import java.net.URI;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Utility class for URL redaction.
 */
public final class UrlRedactionUtil {
    private static final String REDACTED_PLACEHOLDER = "REDACTED";

    /**
     * Generates the redacted URI for logging.
     *
     * @param uri URI where the request is being sent.
     * @param allowedQueryParameterNames Set of query parameter names that are allowed to be logged.
     * @return A URI with query parameters redacted based on provided allow-list.
     */
    public static String getRedactedUri(URI uri, Set<String> allowedQueryParameterNames) {
        String query = uri.getQuery();

        int estimatedUriLength = uri.toString().length() + 128;
        StringBuilder uriBuilder = new StringBuilder(estimatedUriLength);

        // Add the protocol, host and port to the uriBuilder
        uriBuilder.append(uri.getScheme()).append("://").append(uri.getHost());

        if (uri.getPort() != -1) {
            uriBuilder.append(":").append(uri.getPort());
        }

        // Add the path to the uriBuilder
        uriBuilder.append(uri.getPath());

        if (query != null && !query.isEmpty()) {
            uriBuilder.append("?");

            // Parse and redact the query parameters
            boolean firstQueryParam = true;
            for (Map.Entry<String, String> kvp : new ImplUtils.QueryParameterIterable(query)) {
                if (!firstQueryParam) {
                    uriBuilder.append('&');
                }

                uriBuilder.append(kvp.getKey());
                uriBuilder.append('=');

                if (allowedQueryParameterNames.contains(kvp.getKey().toLowerCase(Locale.ROOT))) {
                    uriBuilder.append(kvp.getValue());
                } else {
                    uriBuilder.append(REDACTED_PLACEHOLDER);
                }

                firstQueryParam = false;
            }
        }

        return uriBuilder.toString();
    }

    private UrlRedactionUtil() {
    }
}
