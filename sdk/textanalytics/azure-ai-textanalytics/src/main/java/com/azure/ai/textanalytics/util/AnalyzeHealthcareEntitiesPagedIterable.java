// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.util;

import com.azure.core.annotation.Immutable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.util.paging.ContinuablePagedIterable;

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
}
