// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;

import java.util.stream.Stream;

/**
 *  //TODO: add javadoc
 * @param <T> add javadoc
 */
@Immutable
public final class TextAnalyticsPagedIterable<T> extends IterableStream<T> {

    private static final int DEFAULT_BATCH_SIZE = 1;
    private final TextAnalyticsPagedFlux<T> textAnalyticsPagedFlux;

    /**
     * //TODO: add javadoc
     *
     * @param textAnalyticsPagedFlux add
     */
    public TextAnalyticsPagedIterable(TextAnalyticsPagedFlux<T> textAnalyticsPagedFlux) {
        super(textAnalyticsPagedFlux);
        this.textAnalyticsPagedFlux = textAnalyticsPagedFlux;
    }

    /**
     *  //TODO: add javadoc
     *
     * @return aaa
     */
    public Stream<TextAnalyticsPagedResponse<T>> streamByPage() {
        return textAnalyticsPagedFlux.byPage().toStream(DEFAULT_BATCH_SIZE);
    }

    /**
     * //TODO: add javadoc
     *
     * @return aaa
     */
    public Iterable<TextAnalyticsPagedResponse<T>> iterableByPage() {
        return textAnalyticsPagedFlux.byPage().toIterable(DEFAULT_BATCH_SIZE);
    }
}
