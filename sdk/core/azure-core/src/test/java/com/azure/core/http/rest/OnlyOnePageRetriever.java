// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import com.azure.core.util.paging.PageRetriever;
import reactor.core.publisher.Flux;

import java.util.concurrent.atomic.AtomicInteger;

public class OnlyOnePageRetriever implements PageRetriever<Integer, OnlyOneContinuablePage> {
    private final AtomicInteger getCount = new AtomicInteger();

    @Override
    public Flux<OnlyOneContinuablePage> get(Integer continuationToken, Integer pageSize) {
        int value = getCount.getAndIncrement();
        if (value == 3) {
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
