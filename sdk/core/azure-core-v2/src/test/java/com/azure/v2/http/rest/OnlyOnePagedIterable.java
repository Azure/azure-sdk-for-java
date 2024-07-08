// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import com.azure.core.v2.util.paging.ContinuablePagedFlux;
import com.azure.core.v2.util.paging.ContinuablePagedIterable;
import com.azure.core.v2.util.paging.PageRetrieverSync;

import java.util.function.Predicate;
import java.util.function.Supplier;

public class OnlyOnePagedIterable extends ContinuablePagedIterable<Integer, Integer, OnlyOneContinuablePage> {
    public OnlyOnePagedIterable(ContinuablePagedFlux<Integer, Integer, OnlyOneContinuablePage> pagedFlux) {
        this(pagedFlux, 1);
    }

    public OnlyOnePagedIterable(ContinuablePagedFlux<Integer, Integer, OnlyOneContinuablePage> pagedFlux,
        int batchSize) {
        super(pagedFlux, batchSize);
    }

    public OnlyOnePagedIterable(Supplier<PageRetrieverSync<Integer, OnlyOneContinuablePage>> pageRetrieverProvider,
        Integer pageSize, Predicate<Integer> continuationPredicate) {
        super(pageRetrieverProvider, pageSize, continuationPredicate);
    }
}
