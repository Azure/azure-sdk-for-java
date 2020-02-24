// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import com.azure.core.util.paging.ContinuablePage;
import com.azure.core.util.paging.ContinuablePagedFluxCore;
import com.azure.core.util.paging.PageRetriever;

import java.util.function.Supplier;

/**
 * A {@link ContinuablePagedFluxCore} implementation used by Azure Search.
 *
 * @param <T> Type of the elements returned in the {@link com.azure.core.http.rest.PagedResponse}.
 * @param <P> Type of the paged returned as a response.
 */
public final class SearchPagedFlux<T, P extends ContinuablePage<String, T>>
    extends ContinuablePagedFluxCore<String, T, P> {
    protected SearchPagedFlux(Supplier<PageRetriever<String, P>> pageRetrieverProvider) {
        super(pageRetrieverProvider);
    }
}
