// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.implementation.util;

/**
 * Utilities shared with HttpClient implementations.
 */
public final class HttpUtils {
    /**
     * Context key used to indicate to an HttpClient implementation if it should eagerly read the response from the
     * network.
     */
    public static final String AZURE_EAGERLY_READ_RESPONSE = "azure-eagerly-read-response";

    /**
     * Context key used to indicate to an HttpClient implementation if the response body should be ignored and eagerly
     * drained from the network.
     */
    public static final String AZURE_IGNORE_RESPONSE_BODY = "azure-ignore-response-body";

    /**
     * Context key used to indicate to an HttpClient a per-call response timeout.
     */
    public static final String AZURE_RESPONSE_TIMEOUT = "azure-response-timeout";

    /**
     * Context key used to indicate to an HttpClient if the implementation specific HTTP headers should be converted to
     * Azure Core HttpHeaders.
     */
    public static final String AZURE_EAGERLY_CONVERT_HEADERS = "azure-eagerly-convert-headers";

    private HttpUtils() {
    }
}
