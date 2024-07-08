// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.v2.http.rest;

import com.azure.core.v2.util.IterableStream;

import io.clientcore.core.http.models.Response;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
        IterableStream<T> iterableStream = this.getElements();
        return iterableStream == null ? new ArrayList<>() : iterableStream.stream().collect(Collectors.toList());
    }
}
