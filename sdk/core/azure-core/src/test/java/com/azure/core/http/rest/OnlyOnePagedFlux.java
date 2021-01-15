// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import com.azure.core.util.paging.ContinuablePagedFluxCore;
import com.azure.core.util.paging.PageRetriever;

import java.util.function.Supplier;

public class OnlyOnePagedFlux extends ContinuablePagedFluxCore<Integer, Integer, OnlyOneContinuablePage> {
    public OnlyOnePagedFlux(Supplier<PageRetriever<Integer, OnlyOneContinuablePage>> pageRetrieverProvider) {
        super(pageRetrieverProvider);
    }
}
