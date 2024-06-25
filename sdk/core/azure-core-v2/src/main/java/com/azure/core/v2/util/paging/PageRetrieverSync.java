// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.util.paging;

/**
 * This class handles retrieving page synchronously.
 *
 * @param <C> Type of the continuation token.
 * @param <P> the page elements type
 */
@FunctionalInterface
public interface PageRetrieverSync<C, P> {

    /**
     * Retrieves one starting from the page identified by the given continuation token.
     *
     * @param continuationToken Token identifying which page to retrieve, passing {@code null} indicates to retrieve
     * the first page.
     * @param pageSize The number of items to retrieve per page, passing {@code null} will use the source's default
     * page size.
     * @return A page of elements type <P>.
     */
    P getPage(C continuationToken, Integer pageSize);
}
