// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.util;

import com.azure.core.http.rest.PagedFluxBase;
import com.azure.search.documents.models.AutocompleteItem;
import reactor.core.publisher.Mono;

import java.util.function.Supplier;

/**
 * Implementation of {@link PagedFluxBase} where the element type is {@link AutocompleteItem} and the page type is
 * {@link AutocompletePagedResponse}.
 */
public final class AutocompletePagedFlux extends PagedFluxBase<AutocompleteItem, AutocompletePagedResponse> {
    /**
     * Creates an instance of {@link AutocompletePagedFlux} that retrieves a single page.
     *
     * @param firstPageRetriever Supplier that handles retrieving the first page.
     */
    public AutocompletePagedFlux(Supplier<Mono<AutocompletePagedResponse>> firstPageRetriever) {
        super(firstPageRetriever);
    }
}
