// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.util;

import com.azure.core.http.rest.PagedFluxBase;
import com.azure.core.http.rest.PagedIterableBase;
import com.azure.search.SuggestPagedResponse;
import com.azure.search.models.SuggestResult;

/**
 * Implementation of {@link PagedIterableBase} where the element type is {@link SuggestResult} and the page type is
 * {@link SuggestPagedResponse}.
 */
public final class SuggestPagedIterable extends PagedIterableBase<SuggestResult, SuggestPagedResponse> {
    /**
     * Creates instance given {@link PagedFluxBase}.
     *
     * @param pagedFluxBase to use as iterable
     */
    public SuggestPagedIterable(PagedFluxBase<SuggestResult, SuggestPagedResponse> pagedFluxBase) {
        super(pagedFluxBase);
    }
}
