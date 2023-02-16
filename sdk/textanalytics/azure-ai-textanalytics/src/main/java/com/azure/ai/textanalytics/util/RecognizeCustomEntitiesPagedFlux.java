// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.util;

import com.azure.core.annotation.Immutable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.util.paging.ContinuablePagedFluxCore;
import com.azure.core.util.paging.PageRetriever;

import java.util.function.Supplier;

/**
 * An implementation of {@link ContinuablePagedFluxCore} that uses default {@link PagedResponse}.
 *
 * @see ContinuablePagedFluxCore
 */
@Immutable
public final class RecognizeCustomEntitiesPagedFlux extends ContinuablePagedFluxCore<String,
    RecognizeCustomEntitiesResultCollection, PagedResponse<RecognizeCustomEntitiesResultCollection>> {
    /**
     * Creates an instance of {@link RecognizeCustomEntitiesPagedFlux}
     *
     * @param pageRetrieverProvider a provider that returns {@link PageRetriever}
     */
    public RecognizeCustomEntitiesPagedFlux(
        Supplier<PageRetriever<String, PagedResponse<RecognizeCustomEntitiesResultCollection>>>
            pageRetrieverProvider) {
        super(pageRetrieverProvider);
    }
}
