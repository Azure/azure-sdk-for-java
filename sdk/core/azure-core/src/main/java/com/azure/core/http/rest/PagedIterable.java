// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import com.azure.core.util.IterableStream;
import com.azure.core.util.paging.SyncPageRetriever;

import java.util.Collections;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * This class provides utility to iterate over {@link PagedResponse} using {@link Stream} and {@link Iterable}
 * interfaces.
 *
 * <p><strong>Code sample using {@link Stream} by page</strong></p>
 *
 * <!-- src_embed com.azure.core.http.rest.pagedIterable.streamByPage -->
 * <pre>
 * &#47;&#47; process the streamByPage
 * pagedIterableResponse.streamByPage&#40;&#41;.forEach&#40;resp -&gt; &#123;
 *     System.out.printf&#40;&quot;Response headers are %s. Url %s  and status code %d %n&quot;, resp.getHeaders&#40;&#41;,
 *         resp.getRequest&#40;&#41;.getUrl&#40;&#41;, resp.getStatusCode&#40;&#41;&#41;;
 *     resp.getElements&#40;&#41;.forEach&#40;value -&gt; System.out.printf&#40;&quot;Response value is %d %n&quot;, value&#41;&#41;;
 * &#125;&#41;;
 *
 * </pre>
 * <!-- end com.azure.core.http.rest.pagedIterable.streamByPage -->
 *
 * <p><strong>Code sample using {@link Iterable} by page</strong></p>
 *
 * <!-- src_embed com.azure.core.http.rest.pagedIterable.iterableByPage -->
 * <pre>
 * &#47;&#47; process the iterableByPage
 * pagedIterableResponse.iterableByPage&#40;&#41;.forEach&#40;resp -&gt; &#123;
 *     System.out.printf&#40;&quot;Response headers are %s. Url %s  and status code %d %n&quot;, resp.getHeaders&#40;&#41;,
 *         resp.getRequest&#40;&#41;.getUrl&#40;&#41;, resp.getStatusCode&#40;&#41;&#41;;
 *     resp.getElements&#40;&#41;.forEach&#40;value -&gt; System.out.printf&#40;&quot;Response value is %d %n&quot;, value&#41;&#41;;
 * &#125;&#41;;
 * </pre>
 * <!-- end com.azure.core.http.rest.pagedIterable.iterableByPage -->
 *
 * <p><strong>Code sample using {@link Iterable} by page and while loop</strong></p>
 *
 * <!-- src_embed com.azure.core.http.rest.pagedIterable.iterableByPage.while -->
 * <pre>
 * &#47;&#47; iterate over each page
 * for &#40;PagedResponse&lt;Integer&gt; resp : pagedIterableResponse.iterableByPage&#40;&#41;&#41; &#123;
 *     System.out.printf&#40;&quot;Response headers are %s. Url %s  and status code %d %n&quot;, resp.getHeaders&#40;&#41;,
 *         resp.getRequest&#40;&#41;.getUrl&#40;&#41;, resp.getStatusCode&#40;&#41;&#41;;
 *     resp.getElements&#40;&#41;.forEach&#40;value -&gt; System.out.printf&#40;&quot;Response value is %d %n&quot;, value&#41;&#41;;
 * &#125;
 * </pre>
 * <!-- end com.azure.core.http.rest.pagedIterable.iterableByPage.while -->
 *
 * @param <T> The type of value contained in this {@link IterableStream}.
 * @see PagedResponse
 * @see IterableStream
 */
public class PagedIterable<T> extends PagedIterableBase<T, PagedResponse<T>> {
    private final PagedFlux<T> pagedFlux;

    /**
     * Creates instance given {@link PagedFlux}.
     * @param pagedFlux to use as iterable
     */
    public PagedIterable(PagedFlux<T> pagedFlux) {
        super(pagedFlux);
        this.pagedFlux = pagedFlux;
    }

    /**
     * Creates an instance of {@link PagedIterable} that consists of only a single page. This constructor takes a {@code
     * Supplier} that return the single page of {@code T}.
     *
     * <p><strong>Code sample</strong></p>
     *
     *
     * @param firstPageRetriever Supplier that retrieves the first page.
     */
    public PagedIterable(Supplier<PagedResponse<T>> firstPageRetriever) {
        this(firstPageRetriever, null);
    }

    /**
     * Creates an instance of {@link PagedIterable} that consists of only a single page with a given element count.
     *
     * <p><strong>Code sample</strong></p>
     *
     *
     * @param firstPageRetriever Function that retrieves the first page.
     */
    public PagedIterable(Function<Integer, PagedResponse<T>> firstPageRetriever) {
        this(firstPageRetriever, (token, pageSize) -> null);
    }

    /**
     * Creates an instance of {@link PagedIterable}. The constructor takes a {@code Supplier} and {@code Function}. The
     * {@code Supplier} returns the first page of {@code T}, the {@code Function} retrieves subsequent pages of {@code
     * T}.
     *
     * <p><strong>Code sample</strong></p>
     *
     * @param firstPageRetriever Supplier that retrieves the first page
     * @param nextPageRetriever Function that retrieves the next page given a continuation token
     */
    public PagedIterable(Supplier<PagedResponse<T>> firstPageRetriever,
                     Function<String, PagedResponse<T>> nextPageRetriever) {
        this(() -> (continuationToken, pageSize) ->
            continuationToken == null
                 ? toIterable(firstPageRetriever.get())
                 : toIterable(nextPageRetriever.apply(continuationToken)), true);
    }

    private static <T> IterableStream<PagedResponse<T>> toIterable(PagedResponse<T> pagedResponse) {
        if (pagedResponse == null) {
            return IterableStream.of(null);
        }
        return IterableStream.of(Collections.singletonList(pagedResponse));
    }

    /**
     * Creates an instance of {@link PagedIterable} that is capable of retrieving multiple pages with of a given page size.
     *
     * @param firstPageRetriever Function that retrieves the first page.
     * @param nextPageRetriever BiFunction that retrieves the next page given a continuation token and page size.
     */
    public PagedIterable(Function<Integer, PagedResponse<T>> firstPageRetriever,
                     BiFunction<String, Integer, PagedResponse<T>> nextPageRetriever) {
        this(() -> (continuationToken, pageSize) -> continuationToken == null
             ? toIterable(firstPageRetriever.apply(pageSize))
             : toIterable(nextPageRetriever.apply(continuationToken, pageSize)), true);
    }

    /**
     * Create PagedIterable backed by Page Retriever Function Supplier.
     *
     * @param provider the Page Retrieval Provider
     * @param ignored param is ignored, exists in signature only to avoid conflict with first ctr
     */
    private PagedIterable(Supplier<SyncPageRetriever<String, PagedResponse<T>>> provider, boolean ignored) {
        super(provider);
        this.pagedFlux = null;
    }

    /**
     * Maps this PagedIterable instance of T to a PagedIterable instance of type S as per the provided mapper function.
     *
     * @param mapper The mapper function to convert from type T to type S.
     * @param <S> The mapped type.
     * @return A PagedIterable of type S.
     */
    @SuppressWarnings("deprecation")
    public <S> PagedIterable<S> mapPage(Function<T, S> mapper) {
        return new PagedIterable<>(pagedFlux.mapPage(mapper));
    }
}
