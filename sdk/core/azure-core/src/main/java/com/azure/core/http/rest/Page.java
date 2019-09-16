// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import java.util.List;

/**
 * Represents a paginated REST response from the service.
 *
 * @param <T> Type of the listed objects in that response.
 */
public interface Page<T> {

    /**
     * @return A list of items from that service.
     */
    List<T> getItems();

    /**
     * @return A link to the next page, or null if there are no more results.
     */
    String getNextLink();
}
