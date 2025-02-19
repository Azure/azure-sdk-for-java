// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.core.http.rest;

import com.azure.v2.core.utils.CoreUtils;
import com.azure.v2.core.utils.IterableStream;
import com.azure.v2.core.utils.paging.ContinuablePagedIterable;
import com.azure.v2.core.utils.paging.PageRetriever;

import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * This class provides utility to iterate over responses that extend {@link PagedResponse} using {@link Stream} and
 * {@link Iterable} interfaces.
 *
 * <p>
 * <strong>Code sample using {@link Stream} by page</strong>
 * </p>
 *
 * <!-- src_embed com.azure.core.http.rest.pagedIterableBase.streamByPage -->
 * <!-- end com.azure.core.http.rest.pagedIterableBase.streamByPage -->
 *
 * <p>
 * <strong>Code sample using {@link Iterable} by page</strong>
 * </p>
 *
 * <!-- src_embed com.azure.core.http.rest.pagedIterableBase.iterableByPage -->
 * <!-- end com.azure.core.http.rest.pagedIterableBase.iterableByPage -->
 *
 * <p>
 * <strong>Code sample using {@link Iterable} by page and while loop</strong>
 * </p>
 *
 * <!-- src_embed com.azure.core.http.rest.pagedIterableBase.iterableByPage.while -->
 * <!-- end com.azure.core.http.rest.pagedIterableBase.iterableByPage.while -->
 *
 * @param <T> The type of value contained in this {@link IterableStream}.
 * @param <P> The response extending from {@link PagedResponse}
 * @see PagedResponse
 * @see IterableStream
 */
public class PagedIterableBase<T, P extends PagedResponse<T>> extends ContinuablePagedIterable<String, T, P> {
    /**
     * Creates instance given the {@link PageRetriever page retriever} {@link Supplier}.
     *
     * @param provider The page retriever {@link Supplier}.
     */
    public PagedIterableBase(Supplier<PageRetriever<String, P>> provider) {
        super(provider, null, token -> !CoreUtils.isNullOrEmpty(token));
    }
}
