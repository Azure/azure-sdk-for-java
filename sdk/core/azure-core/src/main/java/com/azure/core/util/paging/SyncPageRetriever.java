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

    IterableStream<P> getIterable(C continuationToken, Integer pageSize);
}
