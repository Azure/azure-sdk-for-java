// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.util;

import com.azure.core.http.rest.PagedFluxBase;
import com.azure.search.documents.models.SuggestResult;
import reactor.core.publisher.Mono;

import java.util.function.Supplier;

/**
 * Implementation of {@link PagedFluxBase} where the element type is {@link SuggestResult} and the page type is {@link
 * SuggestPagedResponse}.
 */
public final class SuggestPagedFlux extends PagedFluxBase<SuggestResult, SuggestPagedResponse> {
    /**
     * Creates an instance of {@link SuggestPagedFlux} that retrieves a single page.
     *
     * @param firstPageRetriever Supplier that handles retrieving the first page.
     */
    public SuggestPagedFlux(Supplier<Mono<SuggestPagedResponse>> firstPageRetriever) {
        super(firstPageRetriever);
    }
}
