/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v3;

import com.microsoft.rest.v3.http.HttpRequest;

import java.io.Closeable;
import java.util.List;
import java.util.Map;

/**
 * Response of a REST API returning page.
 *
 * @param <T> the type items in the page
 */
public interface RestPagedResponse<T> extends Closeable {
    /**
     * Gets the items in the page.
     *
     * @return the items
     */
    List<T> items();

    /**
     * Get the link to retrieve RestPagedResponse containing next page.
     *
     * @return the next page link
     */
    String nextLink();

    /**
     * Get the request resulted in this RestPagedResponse.
     *
     * @return the request
     */
    HttpRequest request();

    /**
     * Get the response status code.
     *
     * @return the status code of the HTTP response
     */
    public int statusCode();

    /**
     * Get the response header as a map.
     *
     * @return a Map containing the raw HTTP response headers.
     */
    Map<String, String> headers();
}
