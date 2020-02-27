// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.util;

import com.azure.core.http.rest.PagedFluxBase;
import com.azure.search.SuggestPagedResponse;
import com.azure.search.models.SuggestResult;
import reactor.core.publisher.Mono;

import java.util.function.Supplier;

/**
 * Implementation of {@link PagedFluxBase} where the element type is {@link SuggestResult} and the page type is {@link
 * SuggestPagedResponse}.
 */
public final class SuggestPagedFlux extends PagedFluxBase<SuggestResult, SuggestPagedResponse> {
    public SuggestPagedFlux(Supplier<Mono<SuggestPagedResponse>> firstPageRetriever) {
        super(firstPageRetriever);
    }
}
