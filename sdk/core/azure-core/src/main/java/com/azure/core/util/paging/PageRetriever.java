// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.paging;

import reactor.core.publisher.Flux;

/**
 * A type representing the contract to retrieve one or more pages.
 *
 * @param <C> the continuation token type
 * @param <P> the page elements type
 */
@FunctionalInterface
public interface PageRetriever<C, P> {
    /**
     * Retrieve a set of one or more pages starting from the page identified by
     * the given continuation token.
     *
     * @param continuationToken the token identifying the page set, a {@code null}
     *                          value indicate that retrieve pages from the beginning
     * @param pageSize the preferred number of items per page, a {@code null} value
     *                 indicate that client prefer server's default page size
     * @return a Flux that emits one or more pages
     */
    Flux<P> get(C continuationToken, Integer pageSize);
}
