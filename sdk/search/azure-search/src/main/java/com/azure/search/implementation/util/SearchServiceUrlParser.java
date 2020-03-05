// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.implementation.util;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Utility class used to parse endpoint URLs for use with the underlying AutoRest clients.
 */
public final class SearchServiceUrlParser {

    /**
     * Parses the {@code endpoint} into the expected service URL parts.
     *
     * @param endpoint The endpoint to parse for service URL parts.
     * @return A {@link SearchServiceUrlParts} object containing the service URL parts.
     * @throws IllegalArgumentException If the {@code endpoint} is a malformed URL or doesn't contain a properly
     * formatted Search service host.
     */
    public static SearchServiceUrlParts parseServiceUrlParts(String endpoint) {
        ClientLogger logger = new ClientLogger(SearchServiceUrlParser.class);

        URL url;
        try {
            // Using the URL class to validate the given endpoint structure
            url = new URL(endpoint);
        } catch (MalformedURLException exc) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Illegal endpoint URL: " + exc.getMessage()));
        }

        // Now that we know that the endpoint is in a valid form, extract the host part
        // (e.g. http://myservice.search.windows.net ==> myservice.search.windows.net) and verify its structure,
        // we expect the service name and domain to be present.
        String extractedHost = url.getHost();
        if (CoreUtils.isNullOrEmpty(extractedHost) || extractedHost.startsWith(".") || extractedHost.endsWith(".")) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Illegal endpoint URL: invalid host"));
        }

        String[] tokens = extractedHost.split("\\.");
        if ((tokens.length < 3) || (CoreUtils.isNullOrEmpty(tokens[0]))) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Illegal endpoint URL: invalid host"));
        }

        // split the service name and dns suffix
        String serviceName = tokens[0];
        int index = extractedHost.indexOf(".");
        String searchDnsSuffix = extractedHost.substring(index + 1);

        return new SearchServiceUrlParts(serviceName, searchDnsSuffix);
    }

    /**
     * Model class containing the Search service URL parts needed by the AutoRest client.
     */
    public static class SearchServiceUrlParts {
        final String serviceName;
        final String dnsSuffix;

        SearchServiceUrlParts(String serviceName, String dnsSuffix) {
            this.serviceName = serviceName;
            this.dnsSuffix = dnsSuffix;
        }

        /**
         * @return The name of the Search service resource.
         */
        public String getServiceName() {
            return serviceName;
        }

        /**
         * @return The DNS suffix of the Search service resource.
         */
        public String getDnsSuffix() {
            return dnsSuffix;
        }
    }

    private SearchServiceUrlParser() {
    }
}
