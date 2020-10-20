// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import java.util.Map;

/**
 * The REST client API.
 */
interface RestClient {

    /**
     * Issue a GET request.
     *
     * @param url the URL.
     * @param headers the request headers map.
     * @return the response body as a string.
     */
    String get(String url, Map<String, String> headers);

    /**
     * Issue a POST request.
     *
     * @param url the URL.
     * @param body the request body.
     * @return the response body as a string.
     */
    String post(String url, String body, String contentType);
}
