// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.paging;

import com.azure.core.util.IterableStream;

/**
 * This class handles retrieving pages.
 *
 * @param <C> Type of the continuation token.
 * @param <P> the page elements type
 */
@FunctionalInterface
public interface SyncPageRetriever<C, P> {

    /**
     * Retrieves one or more pages starting from the page identified by the given continuation token.
     *
     * @param continuationToken Token identifying which page to retrieve, passing {@code null} indicates to retrieve
     * the first page.
     * @param pageSize The number of items to retrieve per page, passing {@code null} will use the source's default
     * page size.
     * @return A {@link IterableStream} that emits one or more pages.
     */
    IterableStream<P> getIterable(C continuationToken, Integer pageSize);
}
