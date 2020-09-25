// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import com.azure.core.util.paging.ContinuablePagedFlux;
import com.azure.core.util.paging.ContinuablePagedIterable;

public class OnlyOnePagedIterable extends ContinuablePagedIterable<Integer, Integer, OnlyOneContinuablePage> {
    public OnlyOnePagedIterable(ContinuablePagedFlux<Integer, Integer, OnlyOneContinuablePage> pagedFlux) {
        super(pagedFlux);
    }
}
