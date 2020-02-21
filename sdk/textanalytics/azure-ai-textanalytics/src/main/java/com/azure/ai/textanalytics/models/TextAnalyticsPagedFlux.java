// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.http.rest.Page;
import com.azure.core.util.paging.ContinuablePagedFluxCore;
import com.azure.core.util.paging.PageRetriever;

import java.util.function.Supplier;

/**
 * // TODO: update it
 *
 * @param <T> The type of items contained in the {@link Page}
 */
@Immutable
public final class TextAnalyticsPagedFlux<T>
    extends ContinuablePagedFluxCore<String, T, TextAnalyticsPagedResponse<T>> {
    /**
     * // TODO: update it
     *
     * @param pageRetrieverProvider provider
     */
    public TextAnalyticsPagedFlux(
        Supplier<PageRetriever<String, TextAnalyticsPagedResponse<T>>> pageRetrieverProvider) {
        super(pageRetrieverProvider);
    }
}


