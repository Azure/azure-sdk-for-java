// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import com.azure.core.util.paging.PageRetrieverSync;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class OnlyOnePageRetrieverSync implements PageRetrieverSync<Integer, OnlyOneContinuablePage> {
    private final AtomicInteger getCount = new AtomicInteger();

    private final int pageCount;

    public OnlyOnePageRetrieverSync(int pageCount) {
        this.pageCount = pageCount;
    }

    @Override
    public Stream<OnlyOneContinuablePage> getPage(Integer continuationToken, Integer pageSize) {
        getCount.getAndIncrement();
        if (continuationToken != null && continuationToken == pageCount - 1) {
            pageSize = (pageSize == null) ? 10 : pageSize;
            return Stream.of(new OnlyOneContinuablePage(continuationToken, null, pageSize));
        } else {
            continuationToken = (continuationToken == null) ? 0 : continuationToken;
            pageSize = (pageSize == null) ? 10 : pageSize;
            return Stream.of(new OnlyOneContinuablePage(continuationToken, continuationToken + 1, pageSize));
        }
    }

    public int getGetCount() {
        return getCount.get();
    }
}
