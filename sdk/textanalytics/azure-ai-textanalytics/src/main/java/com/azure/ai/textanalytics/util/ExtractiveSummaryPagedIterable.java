// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.util;

import com.azure.core.annotation.Immutable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.paging.ContinuablePagedIterable;
import com.azure.core.util.paging.PageRetrieverSync;

import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * This class provides utility to iterate over {@link PagedResponse} using {@link Stream} and {@link Iterable}
 * interfaces.
 */
@Immutable
public final class ExtractiveSummaryPagedIterable extends ContinuablePagedIterable<String,
        ExtractiveSummaryResultCollection, PagedResponse<ExtractiveSummaryResultCollection>> {

    /**
     * Creates an instance of {@code ExtractiveSummaryPagedIterable}. The constructor takes a {@code Supplier} and
     * {@code Function}. The {@code Supplier} returns the first page of {@link ExtractiveSummaryResultCollection},
     * the {@code Function} retrieves subsequent pages of {@link ExtractiveSummaryResultCollection}.
     *
     * @param provider Supplier that retrieves the first page
     */
    public ExtractiveSummaryPagedIterable(
        Supplier<PageRetrieverSync<String, PagedResponse<ExtractiveSummaryResultCollection>>> provider) {
        super(provider, (Integer) null, (token) -> {
            return !CoreUtils.isNullOrEmpty(token);
        });
    }
}
