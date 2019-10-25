// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import com.azure.core.util.logging.ClientLogger;
import org.apache.commons.lang3.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * This class is used internally to parse endpoint URLs for use with the underlying AutoRest clients
 */
class SearchServiceUrlParser {

    static SearchServiceUrlParts parseServiceUrlParts(String endpoint) {
        ClientLogger logger = new ClientLogger(SearchServiceUrlParser.class);

        if (StringUtils.isBlank(endpoint)) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("Illegal endpoint URL: endpoint cannot be blank"));
        }

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
        if (StringUtils.isBlank(extractedHost) || extractedHost.startsWith(".") || extractedHost.endsWith(".")) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Illegal endpoint URL: invalid host"));
        }

        String[] tokens = StringUtils.split(extractedHost, ".");
        if ((tokens.length < 3) || (StringUtils.isBlank(tokens[0]))) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Illegal endpoint URL: invalid host"));
        }

        // split the service name and dns suffix
        String serviceName = tokens[0];
        int index = StringUtils.indexOf(extractedHost, ".");
        String searchDnsSuffix = extractedHost.substring(index + 1);

        return new SearchServiceUrlParts(serviceName, searchDnsSuffix);
    }

    static class SearchServiceUrlParts {
        final String serviceName;
        final String dnsSuffix;

        SearchServiceUrlParts(String serviceName, String dnsSuffix) {
            this.serviceName = serviceName;
            this.dnsSuffix = dnsSuffix;
        }
    }
}
