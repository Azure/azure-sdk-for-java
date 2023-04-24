// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.util;

import com.azure.core.http.rest.PagedIterableBase;
import com.azure.search.documents.models.AutocompleteItem;

/**
 * Implementation of {@link PagedIterableBase} where the element type is {@link AutocompleteItem} and the page type is
 * {@link AutocompletePagedResponse}.
 */
public final class AutocompletePagedIterable extends PagedIterableBase<AutocompleteItem, AutocompletePagedResponse> {
    /**
     * Creates instance given {@link AutocompletePagedIterable}.
     *
     * @param pagedFluxBase The {@link AutocompletePagedFlux} that will be consumed as an iterable.
     */
    public AutocompletePagedIterable(AutocompletePagedFlux pagedFluxBase) {
        super(pagedFluxBase);
    }
}
