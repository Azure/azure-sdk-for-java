// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.util;

import com.azure.core.http.rest.PagedFluxBase;
import com.azure.search.AutocompletePagedResponse;
import com.azure.search.models.AutocompleteItem;
import reactor.core.publisher.Mono;

import java.util.function.Supplier;

/**
 * Implementation of {@link PagedFluxBase} where the element type is {@link AutocompleteItem} and the page type is
 * {@link AutocompletePagedResponse}.
 */
public final class AutocompletePagedFlux extends PagedFluxBase<AutocompleteItem, AutocompletePagedResponse> {
    public AutocompletePagedFlux(Supplier<Mono<AutocompletePagedResponse>> firstPageRetriever) {
        super(firstPageRetriever);
    }
}
