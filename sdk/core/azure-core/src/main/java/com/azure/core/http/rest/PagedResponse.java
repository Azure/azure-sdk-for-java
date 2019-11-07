// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.http.rest;

import java.io.Closeable;
import java.util.List;

/**
 * Response of a REST API that returns page.
 *
 * @see Page
 * @see Response
 *
 * @param <T> The type of items in the page.
 */
public interface PagedResponse<T> extends Page<T>, Response<List<T>>, Closeable {

    /**
     * Returns the items in the page.
     *
     * @return The items in the page.
     */
    default List<T> getValue() {
        return getItems();
    }
}
