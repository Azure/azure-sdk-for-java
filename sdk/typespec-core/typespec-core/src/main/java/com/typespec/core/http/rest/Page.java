// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.http.rest;

import com.typespec.core.util.IterableStream;
import com.typespec.core.util.paging.ContinuablePage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a paginated REST response from the service.
 *
 * @param <T> Type of items in the page response.
 */
public interface Page<T> extends ContinuablePage<String, T> {
    /**
     * Get list of elements in the page.
     *
     * @return the page elements
     *
     * @deprecated use {@link #getElements()}.
     */
    @Deprecated
    default List<T> getItems() {
        IterableStream<T> iterableStream = this.getElements();
        return iterableStream == null
            ? new ArrayList<>()
            : this.getElements().stream().collect(Collectors.toList());
    }
}
