/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.common.http.rest;

import java.io.Closeable;
import java.util.List;

/**
 * Response of a REST API that returns page.
 *
 * @param <T> the type items in the page
 */
public interface RestPagedResponse<T> extends RestResponse<List<T>>, Closeable {
    /**
     * Gets the items in the page.
     *
     * @return The items in the page.
     */
    List<T> items();

    /**
     * Get the link to retrieve RestPagedResponse containing next page.
     *
     * @return the next page link.
     */
    String nextLink();

    /**
     * Returns the items in the page.
     *
     * @return The items in the page.
     */
    default List<T> body() {
        return items();
    }
}
