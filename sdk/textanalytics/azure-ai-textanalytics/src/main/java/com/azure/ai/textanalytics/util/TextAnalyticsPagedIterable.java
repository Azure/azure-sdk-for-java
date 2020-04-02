// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.util;

import com.azure.core.annotation.Immutable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.util.IterableStream;

import java.util.stream.Stream;

/**
 * This class provides utility to iterate over {@link PagedResponse} using {@link Stream} and {@link Iterable}
 * interfaces.
 *
 * <p><strong>Code sample using {@link Stream}</strong></p>
 * {@codesnippet com.azure.ai.textanalytics.util.TextAnalyticsPagedIterable.stream}
 *
 * <p><strong>Code sample using {@link Stream} by page</strong></p>
 * {@codesnippet com.azure.ai.textanalytics.util.TextAnalyticsPagedIterable.streamByPage}
 *
 * <p><strong>Code sample using {@link Iterable} </strong></p>
 * {@codesnippet com.azure.ai.textanalytics.util.TextAnalyticsPagedIterable.iterator}
 *
 * <p><strong>Code sample using {@link Iterable} by page</strong></p>
 * {@codesnippet com.azure.ai.textanalytics.util.TextAnalyticsPagedIterable.iterableByPage}
 *
 * @param <T> The type of items contained in the {@link TextAnalyticsPagedIterable}
 *
 * @see IterableStream
 */
@Immutable
public final class TextAnalyticsPagedIterable<T> extends IterableStream<T> {
    /*
     * This is the default batch size that will be requested when using stream or iterable by page, this will indicate
     * to Reactor how many elements should be prefetch before another batch is requested.
     */
    private static final int DEFAULT_BATCH_SIZE = 1;

    private final TextAnalyticsPagedFlux<T> textAnalyticsPagedFlux;

    /**
     * Creates instance given {@link TextAnalyticsPagedFlux}.
     *
     * @param textAnalyticsPagedFlux It used as iterable.
     */
    public TextAnalyticsPagedIterable(TextAnalyticsPagedFlux<T> textAnalyticsPagedFlux) {
        super(textAnalyticsPagedFlux);
        this.textAnalyticsPagedFlux = textAnalyticsPagedFlux;
    }

    /**
     * Retrieve the {@link Stream}, one page at a time, starting from the next page associated with the given
     * continuation token. To start from first page, use {@link #streamByPage()} instead.
     *
     * @return {@link Stream} of a {@link TextAnalyticsPagedResponse} of {@code T}.
     */
    public Stream<TextAnalyticsPagedResponse<T>> streamByPage() {
        return textAnalyticsPagedFlux.byPage().toStream(DEFAULT_BATCH_SIZE);
    }

    /**
     * Provides {@link Iterable} API for {@link TextAnalyticsPagedResponse}.
     *
     * @return {@link Iterable} of {@link TextAnalyticsPagedResponse} of {@code T}.
     */
    public Iterable<TextAnalyticsPagedResponse<T>> iterableByPage() {
        return textAnalyticsPagedFlux.byPage().toIterable(DEFAULT_BATCH_SIZE);
    }
}
