// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.paging;

import com.azure.core.util.IterableStream;

import java.util.Iterator;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * This class provides utility to iterate over {@link ContinuablePage} using {@link Stream} {@link Iterable}
 * interfaces.
 *
 * @param <C> the type of the continuation token
 * @param <T> The type of elements in a {@link ContinuablePage}
 * @param <P> The {@link ContinuablePage} holding items of type {@code T}.
 * @see IterableStream
 * @see ContinuablePagedFlux
 */
public abstract class ContinuablePagedIterable<C, T, P extends ContinuablePage<C, T>> extends IterableStream<T> {
    private final ContinuablePagedFlux<C, T, P> pagedFlux;
    private final int batchSize;

    /**
     * Creates instance with the given {@link ContinuablePagedFlux}.
     *
     * @param pagedFlux the paged flux use as iterable
     */
    public ContinuablePagedIterable(ContinuablePagedFlux<C, T, P> pagedFlux) {
        this(pagedFlux, 1);
    }

    /**
     * Creates instance with the given {@link ContinuablePagedFlux}.
     *
     * @param pagedFlux the paged flux use as iterable
     * @param batchSize the bounded capacity to prefetch from the {@link ContinuablePagedFlux}
     */
    public ContinuablePagedIterable(ContinuablePagedFlux<C, T, P> pagedFlux, int batchSize) {
        super(pagedFlux);
        this.pagedFlux = pagedFlux;
        this.batchSize = batchSize;
    }

    @Override
    public Stream<T> stream() {
        return StreamSupport.stream(iterableByItemInternal().spliterator(), false);
    }

    /**
     * Retrieve the {@link Stream}, one page at a time. It will provide same {@link Stream} of T values from starting if
     * called multiple times.
     *
     * @return {@link Stream} of a pages
     */
    public Stream<P> streamByPage() {
        return streamByPageInternal(null, null, () -> this.pagedFlux.byPage().toStream(batchSize));
    }

    /**
     * Retrieve the {@link Stream}, one page at a time, starting from the next page associated with the given
     * continuation token. To start from first page, use {@link #streamByPage()} instead.
     *
     * @param continuationToken The continuation token used to fetch the next page
     * @return {@link Stream} of a pages
     */
    public Stream<P> streamByPage(C continuationToken) {
        return streamByPageInternal(continuationToken, null,
            () -> this.pagedFlux.byPage(continuationToken).toStream(batchSize));
    }

    /**
     * Retrieve the {@link Stream}, one page at a time, with each page containing {@code preferredPageSize} items.
     *
     * It will provide same {@link Stream} of T values from starting if called multiple times.
     *
     * @param preferredPageSize the preferred page size, service may or may not honor the page size preference hence
     * client MUST be prepared to handle pages with different page size.
     * @return {@link Stream} of a pages
     */
    public Stream<P> streamByPage(int preferredPageSize) {
        return streamByPageInternal(null, preferredPageSize,
            () -> this.pagedFlux.byPage(preferredPageSize).toStream(batchSize));
    }

    /**
     * Retrieve the {@link Stream}, one page at a time, with each page containing {@code preferredPageSize} items,
     * starting from the next page associated with the given continuation token. To start from first page, use {@link
     * #streamByPage()} or {@link #streamByPage(int)} instead.
     *
     * @param preferredPageSize the preferred page size, service may or may not honor the page size preference hence
     * client MUST be prepared to handle pages with different page size.
     * @param continuationToken The continuation token used to fetch the next page
     * @return {@link Stream} of a pages
     */
    public Stream<P> streamByPage(C continuationToken, int preferredPageSize) {
        return streamByPageInternal(continuationToken, preferredPageSize,
            () -> this.pagedFlux.byPage(continuationToken, preferredPageSize).toStream(batchSize));
    }

    @Override
    public Iterator<T> iterator() {
        return iterableByItemInternal().iterator();
    }

    /**
     * Retrieve the {@link Iterable}, one page at a time. It will provide same {@link Iterable} of T values from
     * starting if called multiple times.
     *
     * @return {@link Stream} of a pages
     */
    public Iterable<P> iterableByPage() {
        return iterableByPageInternal(null, null, () -> this.pagedFlux.byPage().toIterable(batchSize));
    }

    /**
     * Retrieve the {@link Iterable}, one page at a time, starting from the next page associated with the given
     * continuation token. To start from first page, use {@link #iterableByPage()} instead.
     *
     * @param continuationToken The continuation token used to fetch the next page
     * @return {@link Iterable} of a pages
     */
    public Iterable<P> iterableByPage(C continuationToken) {
        return iterableByPageInternal(continuationToken, null,
            () -> this.pagedFlux.byPage(continuationToken).toIterable(batchSize));
    }

    /**
     * Retrieve the {@link Iterable}, one page at a time, with each page containing {@code preferredPageSize} items.
     *
     * It will provide same {@link Iterable} of T values from starting if called multiple times.
     *
     * @param preferredPageSize the preferred page size, service may or may not honor the page size preference hence
     * client MUST be prepared to handle pages with different page size.
     * @return {@link Iterable} of a pages
     */
    public Iterable<P> iterableByPage(int preferredPageSize) {
        return iterableByPageInternal(null, preferredPageSize,
            () -> this.pagedFlux.byPage(preferredPageSize).toIterable(batchSize));
    }

    /**
     * Retrieve the {@link Iterable}, one page at a time, with each page containing {@code preferredPageSize} items,
     * starting from the next page associated with the given continuation token. To start from first page, use {@link
     * #iterableByPage()} or {@link #iterableByPage(int)} instead.
     *
     * @param preferredPageSize the preferred page size, service may or may not honor the page size preference hence
     * client MUST be prepared to handle pages with different page size.
     * @param continuationToken The continuation token used to fetch the next page
     * @return {@link Iterable} of a pages
     */
    public Iterable<P> iterableByPage(C continuationToken, int preferredPageSize) {
        return iterableByPageInternal(continuationToken, preferredPageSize,
            () -> this.pagedFlux.byPage(continuationToken, preferredPageSize).toIterable(batchSize));
    }

    private Stream<P> streamByPageInternal(C continuationToken, Integer preferredPageSize,
        Supplier<Stream<P>> nonPagedFluxCoreIterableSupplier) {
        if (pagedFlux instanceof ContinuablePagedFluxCore) {
            return StreamSupport.stream(iterableByPageInternal(continuationToken, preferredPageSize, null)
                .spliterator(), false);
        } else {
            return nonPagedFluxCoreIterableSupplier.get();
        }
    }

    private Iterable<P> iterableByPageInternal(C continuationToken, Integer preferredPageSize,
        Supplier<Iterable<P>> nonPagedFluxCoreIterableSupplier) {
        if (pagedFlux instanceof ContinuablePagedFluxCore) {
            ContinuablePagedFluxCore<C, T, P> pagedFluxCore = (ContinuablePagedFluxCore<C, T, P>) pagedFlux;
            return new ContinuablePagedByPageIterable<>(pagedFluxCore.pageRetrieverProvider.get(), continuationToken,
                preferredPageSize);
        } else {
            return nonPagedFluxCoreIterableSupplier.get();
        }
    }

    private Iterable<T> iterableByItemInternal() {
        if (pagedFlux instanceof ContinuablePagedFluxCore) {
            ContinuablePagedFluxCore<C, T, P> pagedFluxCore = (ContinuablePagedFluxCore<C, T, P>) pagedFlux;
            return new ContinuablePagedByItemIterable<>(pagedFluxCore.pageRetrieverProvider.get(), null, null);
        } else {
            return this.pagedFlux.toIterable(this.batchSize);
        }
    }
}
