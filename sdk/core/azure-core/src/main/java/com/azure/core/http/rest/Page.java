// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import com.azure.core.paging.PageCore;

/**
 * Represents a paginated REST response from the service.
 *
 * @param <T> Type of the listed objects in that response.
 */
public interface Page<T> extends PageCore<T> {
    /**
     * Gets a link to the next page, or {@code null} if there are no more results.
     *
     * @return A link to the next page, or {@code null} if there are no more results.
     */
    String getContinuationToken();
}
