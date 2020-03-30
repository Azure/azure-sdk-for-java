// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.util;

import com.azure.core.http.rest.PagedIterableBase;
import com.azure.search.documents.models.SuggestResult;

/**
 * Implementation of {@link PagedIterableBase} where the element type is {@link SuggestResult} and the page type is
 * {@link SuggestPagedResponse}.
 */
public final class SuggestPagedIterable extends PagedIterableBase<SuggestResult, SuggestPagedResponse> {
    /**
     * Creates instance given {@link SuggestPagedIterable}.
     *
     * @param pagedFluxBase The {@link SuggestPagedIterable} that will be consumed as an iterable.
     */
    public SuggestPagedIterable(SuggestPagedFlux pagedFluxBase) {
        super(pagedFluxBase);
    }
}
