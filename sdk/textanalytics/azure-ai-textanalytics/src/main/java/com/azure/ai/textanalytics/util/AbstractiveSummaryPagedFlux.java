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
public final class AbstractiveSummaryPagedFlux extends ContinuablePagedFluxCore<String,
        AbstractiveSummaryResultCollection, PagedResponse<AbstractiveSummaryResultCollection>> {
    /**
     * Creates an instance of {@link AbstractiveSummaryPagedFlux}
     *
     * @param pageRetrieverProvider a provider that returns {@link PageRetriever}
     */
    public AbstractiveSummaryPagedFlux(
        Supplier<PageRetriever<String, PagedResponse<AbstractiveSummaryResultCollection>>> pageRetrieverProvider) {
        super(pageRetrieverProvider);
    }
}
