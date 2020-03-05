// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.implementation.polling;

import com.azure.core.http.HttpHeaders;
import com.azure.core.util.logging.ClientLogger;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Utility class.
 */
class Util {
    /**
     * Gets value of Azure-AsyncOperation header from the given Http headers.
     *
     * @param headers the Http headers
     * @param logger the logger
     * @return the Azure-AsyncOperation header value if exists, null otherwise
     */
    static URL getAzureAsyncOperationUrl(HttpHeaders headers, ClientLogger logger) {
        return getUrl("Azure-AsyncOperation", headers, logger);
    }

    /**
     * Gets value of Location header from the given Http headers.
     *
     * @param headers the Http headers
     * @param logger the logger
     * @return the Location header value if exists, null otherwise
     */
    static URL getLocationUrl(HttpHeaders headers, ClientLogger logger) {
        return getUrl("Location", headers, logger);
    }

    /**
     * Get a url from Http headers.
     *
     * @param urlHeaderName the header name
     * @param headers the http headers
     * @param logger the logger
     * @return the URL value of the given header, null if header does not exists.
     */
    static URL getUrl(String urlHeaderName, HttpHeaders headers, ClientLogger logger) {
        String value = headers.getValue(urlHeaderName);
        if (value != null) {
            try {
                return new URL(value);
            } catch (MalformedURLException me) {
                throw logger.logExceptionAsError(new RuntimeException("Malformed value '" + value
                    + "' for URL header: '" + urlHeaderName + "'.", me));
            }
        }
        return null;
    }
}
