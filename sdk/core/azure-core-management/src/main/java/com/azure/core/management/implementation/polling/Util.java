// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.implementation.polling;

import com.azure.core.http.HttpHeaders;
import com.azure.core.util.logging.ClientLogger;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Utility class.
 */
class Util {

    /**
     * An exception thrown while parsing an invalid URL.
     */
    static class MalformedUrlException extends RuntimeException {
        MalformedUrlException(String message) {
            super(message);
        }
        MalformedUrlException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Gets value of Azure-AsyncOperation header from the given Http headers.
     *
     * @param headers the Http headers
     * @param logger the logger
     * @return the Azure-AsyncOperation header value if exists, null otherwise
     */
    static URL getAzureAsyncOperationUrl(HttpHeaders headers, ClientLogger logger) {
        return getUrl("Azure-AsyncOperation", headers, logger, false);
    }

    /**
     * Gets value of Azure-AsyncOperation header from the given Http headers.
     *
     * @param headers the Http headers
     * @param logger the logger
     * @param ignoreException whether to ignore malformed URL
     * @return the Azure-AsyncOperation header value if exists, null otherwise
     */
    static URL getAzureAsyncOperationUrl(HttpHeaders headers, ClientLogger logger, boolean ignoreException) {
        return getUrl("Azure-AsyncOperation", headers, logger, ignoreException);
    }

    /**
     * Gets value of Location header from the given Http headers.
     *
     * @param headers the Http headers
     * @param logger the logger
     * @return the Location header value if exists, null otherwise
     */
    static URL getLocationUrl(HttpHeaders headers, ClientLogger logger) {
        return getUrl("Location", headers, logger, false);
    }

    /**
     * Gets value of Location header from the given Http headers.
     *
     * @param headers the Http headers
     * @param logger the logger
     * @param ignoreException whether to ignore malformed URL
     * @return the URL value of the given header, null if header does not exists.
     */
    static URL getLocationUrl(HttpHeaders headers, ClientLogger logger, boolean ignoreException) {
        return getUrl("Location", headers, logger, ignoreException);
    }

    /**
     * Get a url from Http headers.
     *
     * @param urlHeaderName the header name
     * @param headers the http headers
     * @param logger the logger
     * @param ignoreException whether to ignore malformed URL
     * @return the URL value of the given header, null if header does not exists.
     */
    private static URL getUrl(String urlHeaderName, HttpHeaders headers, ClientLogger logger, boolean ignoreException) {
        String value = headers.getValue(urlHeaderName);
        if (value != null) {
            try {
                return new URI(value).toURL();
            } catch (MalformedURLException | URISyntaxException | IllegalArgumentException me) {
                String message = "Malformed value '" + value + "' for URL header: '" + urlHeaderName + "'.";
                if (ignoreException) {
                    logger.logExceptionAsError(new MalformedUrlException(message, me));
                } else {
                    throw logger.logExceptionAsError(new MalformedUrlException(message, me));
                }
            }
        }
        return null;
    }
}
