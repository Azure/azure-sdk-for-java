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
public final class AnalyzeHealthcareEntitiesPagedIterable extends ContinuablePagedIterable<String,
    AnalyzeHealthcareEntitiesResultCollection, PagedResponse<AnalyzeHealthcareEntitiesResultCollection>> {

    /**
     * Creates instance given {@link AnalyzeHealthcareEntitiesPagedIterable}.
     *
     * @param pagedFlux It used as iterable.
     */
    public AnalyzeHealthcareEntitiesPagedIterable(AnalyzeHealthcareEntitiesPagedFlux pagedFlux) {
        super(pagedFlux);
    }

    /**
     * Creates an instance of {@link AnalyzeHealthcareEntitiesPagedIterable}. The constructor takes a {@code Supplier}
     * and {@code Function}. The {@code Supplier} returns the first page of
     * {@link AnalyzeHealthcareEntitiesResultCollection}, the {@code Function} retrieves subsequent pages of
     * {@link AnalyzeHealthcareEntitiesResultCollection}.
     *
     * @param provider Supplier that retrieves the first page
     */
    public AnalyzeHealthcareEntitiesPagedIterable(
        Supplier<PageRetrieverSync<String, PagedResponse<AnalyzeHealthcareEntitiesResultCollection>>> provider) {
        super(provider, (Integer) null, (token) -> {
            return !CoreUtils.isNullOrEmpty(token);
        });
    }
}
