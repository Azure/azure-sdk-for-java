// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.client.core.http.rest;

import com.client.core.util.paging.ContinuablePagedFluxCore;
import com.client.core.util.paging.PageRetriever;

import java.util.function.Supplier;

public class OnlyOnePagedFlux extends ContinuablePagedFluxCore<Integer, Integer, OnlyOneContinuablePage> {
    public OnlyOnePagedFlux(Supplier<PageRetriever<Integer, OnlyOneContinuablePage>> pageRetrieverProvider) {
        super(pageRetrieverProvider);
    }
}
