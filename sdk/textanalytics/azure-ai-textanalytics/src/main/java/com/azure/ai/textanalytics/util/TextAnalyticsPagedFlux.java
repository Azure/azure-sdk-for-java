// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.util;

import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.core.annotation.Immutable;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.util.paging.ContinuablePagedFluxCore;
import com.azure.core.util.paging.PageRetriever;

import java.util.function.Supplier;

/**
 * An implementation of {@link ContinuablePagedFluxCore} that uses {@link TextAnalyticsPagedResponse} which extends
 * default {@link PagedResponseBase} along with {@code modelVersion} and {@link TextDocumentBatchStatistics}.
 *
 * <p><strong>Code sample using {@link TextAnalyticsPagedFlux} </strong></p>
 * {@codesnippet com.azure.ai.textanalytics.util.TextAnalyticsPagedFlux.subscribe}
 *
 * <p><strong>Code sample using {@link TextAnalyticsPagedFlux} by page</strong></p>
 * {@codesnippet com.azure.ai.textanalytics.util.TextAnalyticsPagedFlux.subscribeByPage}
 *
 * @param <T> The type of items contained in the {@link TextAnalyticsPagedFlux}
 *
 * @see ContinuablePagedFluxCore
 */
@Immutable
public final class TextAnalyticsPagedFlux<T>
    extends ContinuablePagedFluxCore<String, T, TextAnalyticsPagedResponse<T>> {
    /**
     * Create an instance of {@link TextAnalyticsPagedFlux}
     *
     * @param pageRetrieverProvider a provider that returns {@link PageRetriever}
     */
    public TextAnalyticsPagedFlux(
        Supplier<PageRetriever<String, TextAnalyticsPagedResponse<T>>> pageRetrieverProvider) {
        super(pageRetrieverProvider);
    }
}


