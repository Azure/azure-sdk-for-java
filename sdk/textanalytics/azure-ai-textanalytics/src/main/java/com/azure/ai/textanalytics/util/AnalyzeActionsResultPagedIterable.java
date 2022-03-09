// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.util;

import com.azure.ai.textanalytics.models.AnalyzeActionsResult;
import com.azure.core.annotation.Immutable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.util.paging.ContinuablePagedIterable;

import java.util.stream.Stream;

/**
 * This class provides utility to iterate over {@link PagedResponse} using {@link Stream} and {@link Iterable}
 * interfaces.
 */
@Immutable
public final class AnalyzeActionsResultPagedIterable
    extends ContinuablePagedIterable<String, AnalyzeActionsResult, PagedResponse<AnalyzeActionsResult>> {

    /**
     * Creates instance given {@link AnalyzeActionsResultPagedIterable}.
     *
     * @param textAnalyticsPagedFlux It used as iterable.
     */
    public AnalyzeActionsResultPagedIterable(AnalyzeActionsResultPagedFlux textAnalyticsPagedFlux) {
        super(textAnalyticsPagedFlux);
    }
}
