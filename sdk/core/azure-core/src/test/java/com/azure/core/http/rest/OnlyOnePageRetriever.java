// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import com.azure.core.util.paging.PageRetriever;
import reactor.core.publisher.Flux;

import java.util.concurrent.atomic.AtomicInteger;

public class OnlyOnePageRetriever implements PageRetriever<Integer, OnlyOneContinuablePage> {
    private final AtomicInteger getCount = new AtomicInteger();

    private final int pageCount;

    public OnlyOnePageRetriever(int pageCount) {
        this.pageCount = pageCount;
    }

    @Override
    public Flux<OnlyOneContinuablePage> get(Integer continuationToken, Integer pageSize) {
        getCount.getAndIncrement();
        if (continuationToken != null && continuationToken == pageCount - 1) {
            pageSize = (pageSize == null) ? 10 : pageSize;
            return Flux.just(new OnlyOneContinuablePage(continuationToken, null, pageSize));
        } else {
            continuationToken = (continuationToken == null) ? 0 : continuationToken;
            pageSize = (pageSize == null) ? 10 : pageSize;
            return Flux.just(new OnlyOneContinuablePage(continuationToken, continuationToken + 1, pageSize));
        }
    }

    public int getGetCount() {
        return getCount.get();
    }
}
