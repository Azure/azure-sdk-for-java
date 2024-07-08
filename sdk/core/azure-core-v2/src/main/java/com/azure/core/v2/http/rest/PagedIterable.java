// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.http.rest;

import com.azure.core.v2.util.IterableStream;
import com.azure.core.v2.util.paging.PageRetrieverSync;

import java.util.ArrayList;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class provides utility to iterate over {@link PagedResponse} using {@link Stream} and {@link Iterable}
 * interfaces.
 *
 * <p>
 * <strong>Code sample using {@link Stream} by page</strong>
 * </p>
 *
 * <!-- src_embed com.azure.core.http.rest.PagedIterable.streamByPage -->
 * <pre>
 * &#47;&#47; process the streamByPage
 * pagedIterableResponse.streamByPage&#40;&#41;.forEach&#40;resp -&gt; &#123;
 *     System.out.printf&#40;&quot;Response headers are %s. Url %s  and status code %d %n&quot;, resp.getHeaders&#40;&#41;,
 *         resp.getRequest&#40;&#41;.getUrl&#40;&#41;, resp.getStatusCode&#40;&#41;&#41;;
 *     resp.getElements&#40;&#41;.forEach&#40;value -&gt; System.out.printf&#40;&quot;Response value is %d %n&quot;, value&#41;&#41;;
 * &#125;&#41;;
 *
 * </pre>
 * <!-- end com.azure.core.http.rest.PagedIterable.streamByPage -->
 *
 * <p>
 * <strong>Code sample using {@link Iterable} by page</strong>
 * </p>
 *
 * <!-- src_embed com.azure.core.http.rest.PagedIterable.iterableByPage -->
 * <pre>
 * &#47;&#47; process the iterableByPage
 * pagedIterableResponse.iterableByPage&#40;&#41;.forEach&#40;resp -&gt; &#123;
 *     System.out.printf&#40;&quot;Response headers are %s. Url %s  and status code %d %n&quot;, resp.getHeaders&#40;&#41;,
 *         resp.getRequest&#40;&#41;.getUrl&#40;&#41;, resp.getStatusCode&#40;&#41;&#41;;
 *     resp.getElements&#40;&#41;.forEach&#40;value -&gt; System.out.printf&#40;&quot;Response value is %d %n&quot;, value&#41;&#41;;
 * &#125;&#41;;
 * </pre>
 * <!-- end com.azure.core.http.rest.PagedIterable.iterableByPage -->
 *
 * <p>
 * <strong>Code sample using {@link Iterable} by page and while loop</strong>
 * </p>
 *
 * <!-- src_embed com.azure.core.http.rest.PagedIterable.iterableByPage.while -->
 * <pre>
 * &#47;&#47; iterate over each page
 * for &#40;PagedResponse&lt;Integer&gt; resp : pagedIterableResponse.iterableByPage&#40;&#41;&#41; &#123;
 *     System.out.printf&#40;&quot;Response headers are %s. Url %s  and status code %d %n&quot;, resp.getHeaders&#40;&#41;,
 *         resp.getRequest&#40;&#41;.getUrl&#40;&#41;, resp.getStatusCode&#40;&#41;&#41;;
 *     resp.getElements&#40;&#41;.forEach&#40;value -&gt; System.out.printf&#40;&quot;Response value is %d %n&quot;, value&#41;&#41;;
 * &#125;
 * </pre>
 * <!-- end com.azure.core.http.rest.PagedIterable.iterableByPage.while -->
 *
 * <p>
 * <strong>Code sample using {@link Iterable} by page and continuation token</strong>
 * </p>
 *
 * <!-- src_embed com.azure.core.http.rest.PagedIterable.pagesWithContinuationToken -->
 * <pre>
 * String continuationToken = getContinuationToken&#40;&#41;;
 * pagedIterable
 *     .iterableByPage&#40;continuationToken&#41;
 *     .forEach&#40;page -&gt; System.out.printf&#40;&quot;Processing page containing item values: %s%n&quot;,
 *         page.getElements&#40;&#41;.stream&#40;&#41;.map&#40;String::valueOf&#41;.collect&#40;Collectors.joining&#40;&quot;, &quot;&#41;&#41;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.core.http.rest.PagedIterable.pagesWithContinuationToken -->
 *
 * @param <T> The type of value contained in this {@link IterableStream}.
 * @see PagedResponse
 * @see IterableStream
 */
public class PagedIterable<T> extends PagedIterableBase<T, PagedResponse<T>> {
    private final Function<Integer, PagedResponse<T>> firstPageRetriever;
    private final BiFunction<String, Integer, PagedResponse<T>> nextPageRetriever;

    /**
     * Creates an instance of {@link PagedIterable} that consists of only a single page. This constructor takes a {@code
     * Supplier} that return the single page of {@code T}.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.core.http.rest.PagedIterable.singlepage.instantiation -->
     * <pre>
     * &#47;&#47; A supplier that fetches the first page of data from source&#47;service
     * Supplier&lt;PagedResponse&lt;Integer&gt;&gt; firstPageRetrieverFunction = &#40;&#41; -&gt; getFirstPage&#40;&#41;;
     *
     * PagedIterable&lt;Integer&gt; pagedIterableInstance = new PagedIterable&lt;&gt;&#40;firstPageRetrieverFunction,
     *     nextPageRetriever&#41;;
     * </pre>
     * <!-- end com.azure.core.http.rest.PagedIterable.singlepage.instantiation -->
     * @param firstPageRetriever Supplier that retrieves the first page.
     */
    public PagedIterable(Supplier<PagedResponse<T>> firstPageRetriever) {
        this(firstPageRetriever, null);
    }

    /**
     * Creates an instance of {@link PagedIterable} that consists of only a single page with a given element count.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.core.http.rest.PagedFlux.singlepage.instantiationWithPageSize -->
     * <pre>
     * &#47;&#47; A function that fetches the single page of data from a source&#47;service.
     * Function&lt;Integer, Mono&lt;PagedResponse&lt;Integer&gt;&gt;&gt; singlePageRetriever = pageSize -&gt;
     *     getFirstPageWithSize&#40;pageSize&#41;;
     *
     * PagedFlux&lt;Integer&gt; singlePageFluxWithPageSize = new PagedFlux&lt;Integer&gt;&#40;singlePageRetriever&#41;;
     * </pre>
     * <!-- end com.azure.core.http.rest.PagedFlux.singlepage.instantiationWithPageSize -->
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
     * <!-- src_embed com.azure.core.http.rest.PagedIterable.instantiation -->
     * <pre>
     * &#47;&#47; A supplier that fetches the first page of data from source&#47;service
     * Supplier&lt;PagedResponse&lt;Integer&gt;&gt; firstPageRetriever = &#40;&#41; -&gt; getFirstPage&#40;&#41;;
     *
     * &#47;&#47; A function that fetches subsequent pages of data from source&#47;service given a continuation token
     * Function&lt;String, PagedResponse&lt;Integer&gt;&gt; nextPageRetriever =
     *     continuationToken -&gt; getNextPage&#40;continuationToken&#41;;
     *
     * PagedIterable&lt;Integer&gt; pagedIterable = new PagedIterable&lt;&gt;&#40;firstPageRetriever,
     *     nextPageRetriever&#41;;
     * </pre>
     * <!-- end com.azure.core.http.rest.PagedIterable.instantiation -->
    
     * @param firstPageRetriever Supplier that retrieves the first page
     * @param nextPageRetriever Function that retrieves the next page given a continuation token
     */
    public PagedIterable(Supplier<PagedResponse<T>> firstPageRetriever,
        Function<String, PagedResponse<T>> nextPageRetriever) {
        this(
            () -> (continuationToken, pageSize) -> continuationToken == null
                ? firstPageRetriever.get()
                : nextPageRetriever.apply(continuationToken),
            pageSize -> firstPageRetriever.get(),
            (continuationToken, pageSize) -> nextPageRetriever.apply(continuationToken));
    }

    /**
     * Creates an instance of {@link PagedIterable} that is capable of retrieving multiple pages with of a given page size.
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.core.http.rest.PagedIterable.instantiationWithPageSize -->
     * <pre>
     * &#47;&#47; A function that fetches the first page of data from a source&#47;service.
     * Function&lt;Integer, PagedResponse&lt;Integer&gt;&gt; firstPageRetriever = pageSize -&gt; getPage&#40;pageSize&#41;;
     *
     * &#47;&#47; A function that fetches subsequent pages of data from a source&#47;service given a continuation token.
     * BiFunction&lt;String, Integer, PagedResponse&lt;Integer&gt;&gt; nextPageRetriever = &#40;continuationToken, pageSize&#41; -&gt;
     *     getPage&#40;continuationToken, pageSize&#41;;
     *
     * PagedIterable&lt;Integer&gt; pagedIterableWithPageSize = new PagedIterable&lt;&gt;&#40;firstPageRetriever, nextPageRetriever&#41;;
     * </pre>
     * <!-- end com.azure.core.http.rest.PagedIterable.instantiationWithPageSize -->
     * @param firstPageRetriever Function that retrieves the first page.
     * @param nextPageRetriever BiFunction that retrieves the next page given a continuation token and page size.
     */
    public PagedIterable(Function<Integer, PagedResponse<T>> firstPageRetriever,
        BiFunction<String, Integer, PagedResponse<T>> nextPageRetriever) {
        this(() -> (continuationToken, pageSize) -> continuationToken == null
            ? firstPageRetriever.apply(pageSize)
            : nextPageRetriever.apply(continuationToken, pageSize), firstPageRetriever, nextPageRetriever);
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
        Function<Integer, PagedResponse<S>> firstMappedPageRetriever = pageSize -> {
            PagedResponse<T> firstPageResponse = this.firstPageRetriever.apply(pageSize);
            PagedResponse<S> firstMappedPageResponse = mapPagedResponse(firstPageResponse, mapper);
            return firstMappedPageResponse;
        };
        BiFunction<String, Integer, PagedResponse<S>> nextMappedPageRetriever = (continuationToken, pageSize) -> {
            PagedResponse<T> nextPageResponse = this.nextPageRetriever.apply(continuationToken, pageSize);
            PagedResponse<S> nextMappedPageResponse = mapPagedResponse(nextPageResponse, mapper);
            return nextMappedPageResponse;
        };
        return new PagedIterable<>(firstMappedPageRetriever, nextMappedPageRetriever);
    }

    /**
     * Create PagedIterable backed by Page Retriever Function Supplier.
     *
     * @param provider the Page Retrieval Provider.
     * @param firstPageRetriever first page retriever function to get the first page given the page size.
     * @param nextPageRetriever next page retriever function to get the next page given a continuation token and the page size.
     */
    private PagedIterable(Supplier<PageRetrieverSync<String, PagedResponse<T>>> provider,
        Function<Integer, PagedResponse<T>> firstPageRetriever,
        BiFunction<String, Integer, PagedResponse<T>> nextPageRetriever) {
        super(provider);
        this.firstPageRetriever = firstPageRetriever;
        this.nextPageRetriever = nextPageRetriever;
    }

    private <S> PagedResponse<S> mapPagedResponse(PagedResponse<T> pagedResponse, Function<T, S> mapper) {
        if (pagedResponse == null) {
            return null;
        }
        return new PagedResponseBase<String, S>(pagedResponse.getRequest(), pagedResponse.getStatusCode(),
            pagedResponse.getHeaders(),
            pagedResponse.getValue()
                .stream()
                .map(mapper)
                .collect(Collectors.toCollection(() -> new ArrayList<>(pagedResponse.getValue().size()))),
            pagedResponse.getContinuationToken(), null);
    }

}
