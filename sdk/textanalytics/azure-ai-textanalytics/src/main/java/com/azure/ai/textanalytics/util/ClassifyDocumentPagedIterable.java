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
public final class ClassifyDocumentPagedIterable extends ContinuablePagedIterable<String,
    ClassifyDocumentResultCollection, PagedResponse<ClassifyDocumentResultCollection>> {

    /**
     * Creates instance given {@link ClassifyDocumentPagedIterable}.
     *
     * @param pagedFlux It used as iterable.
     */
    public ClassifyDocumentPagedIterable(ClassifyDocumentPagedFlux pagedFlux) {
        super(pagedFlux);
    }

    /**
     * Creates an instance of {@link ClassifyDocumentPagedIterable}. The constructor takes a {@code Supplier} and
     * {@code Function}. The {@code Supplier} returns the first page of {@link ClassifyDocumentResultCollection},
     * the {@code Function} retrieves subsequent pages of {@link ClassifyDocumentResultCollection}.
     *
     * @param provider Supplier that retrieves the first page
     */
    public ClassifyDocumentPagedIterable(
        Supplier<PageRetrieverSync<String, PagedResponse<ClassifyDocumentResultCollection>>> provider) {
        super(provider, (Integer) null, (token) -> {
            return !CoreUtils.isNullOrEmpty(token);
        });
    }
}
