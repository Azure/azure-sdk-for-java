// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.paging.ContinuablePagedFluxCore;
import com.azure.core.util.paging.PageRetriever;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * This class is a flux that can operate on any type that extends {@link PagedResponse} and also provides the ability to
 * operate on individual items. When processing the response by page, each response will contain the items in the page
 * as well as the request details like status code and headers.
 *
 * <p><strong>Process each item in Flux</strong></p>
 * <p>To process one item at a time, simply subscribe to this Flux.</p>
 * <!-- src_embed com.azure.core.http.rest.pagedfluxbase.items -->
 * <pre>
 * pagedFluxBase
 *     .log&#40;&#41;
 *     .subscribe&#40;item -&gt; System.out.println&#40;&quot;Processing item with value: &quot; + item&#41;,
 *         error -&gt; System.err.println&#40;&quot;An error occurred: &quot; + error&#41;,
 *         &#40;&#41; -&gt; System.out.println&#40;&quot;Processing complete.&quot;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.core.http.rest.pagedfluxbase.items -->
 *
 * <p><strong>Process one page at a time</strong></p>
 * <p>To process one page at a time, starting from the beginning, use {@link #byPage() byPage()} method.</p>
 * <!-- src_embed com.azure.core.http.rest.pagedfluxbase.pages -->
 * <pre>
 * pagedFluxBase
 *     .byPage&#40;&#41;
 *     .log&#40;&#41;
 *     .subscribe&#40;page -&gt; System.out.printf&#40;&quot;Processing page containing item values: %s%n&quot;,
 *         page.getElements&#40;&#41;.stream&#40;&#41;.map&#40;String::valueOf&#41;.collect&#40;Collectors.joining&#40;&quot;, &quot;&#41;&#41;&#41;,
 *         error -&gt; System.err.println&#40;&quot;An error occurred: &quot; + error&#41;,
 *         &#40;&#41; -&gt; System.out.println&#40;&quot;Processing complete.&quot;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.core.http.rest.pagedfluxbase.pages -->
 *
 * <p><strong>Process items starting from a continuation token</strong></p>
 * <p>To process items one page at a time starting from any page associated with a continuation token, use
 * {@link #byPage(String)}.</p>
 * <!-- src_embed com.azure.core.http.rest.pagedfluxbase.pagesWithContinuationToken -->
 * <pre>
 * String continuationToken = getContinuationToken&#40;&#41;;
 * pagedFluxBase
 *     .byPage&#40;continuationToken&#41;
 *     .log&#40;&#41;
 *     .doOnSubscribe&#40;ignored -&gt; System.out.println&#40;
 *         &quot;Subscribed to paged flux processing pages starting from: &quot; + continuationToken&#41;&#41;
 *     .subscribe&#40;page -&gt; System.out.printf&#40;&quot;Processing page containing item values: %s%n&quot;,
 *         page.getElements&#40;&#41;.stream&#40;&#41;.map&#40;String::valueOf&#41;.collect&#40;Collectors.joining&#40;&quot;, &quot;&#41;&#41;&#41;,
 *         error -&gt; System.err.println&#40;&quot;An error occurred: &quot; + error&#41;,
 *         &#40;&#41; -&gt; System.out.println&#40;&quot;Processing complete.&quot;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.core.http.rest.pagedfluxbase.pagesWithContinuationToken -->
 *
 * @param <T> The type of items in {@code P}.
 * @param <P> The {@link PagedResponse} holding items of type {@code T}.
 * @see PagedResponse
 * @see Page
 * @see Flux
 * @deprecated use {@link ContinuablePagedFluxCore}.
 */
@Deprecated
public class PagedFluxBase<T, P extends PagedResponse<T>> extends ContinuablePagedFluxCore<String, T, P> {
    /**
     * Creates an instance of {@link PagedFluxBase} that consists of only a single page. This constructor takes a {@code
     * Supplier} that return the single page of {@code T}.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.core.http.rest.pagedfluxbase.singlepage.instantiation -->
     * <pre>
     * &#47;&#47; A supplier that fetches the first page of data from source&#47;service
     * Supplier&lt;Mono&lt;PagedResponse&lt;Integer&gt;&gt;&gt; firstPageRetrieverFunction = &#40;&#41; -&gt; getFirstPage&#40;&#41;;
     *
     * PagedFluxBase&lt;Integer, PagedResponse&lt;Integer&gt;&gt; pagedFluxBaseInstance =
     *     new PagedFluxBase&lt;&gt;&#40;firstPageRetrieverFunction,
     *         nextPageRetriever&#41;;
     * </pre>
     * <!-- end com.azure.core.http.rest.pagedfluxbase.singlepage.instantiation -->
     *
     * @param firstPageRetriever Supplier that retrieves the first page.
     */
    public PagedFluxBase(Supplier<Mono<P>> firstPageRetriever) {
        this(firstPageRetriever, token -> Mono.empty());
    }

    /**
     * Creates an instance of {@link PagedFluxBase}. The constructor takes a {@code Supplier} and {@code Function}. The
     * {@code Supplier} returns the first page of {@code T}, the {@code Function} retrieves subsequent pages of {@code
     * T}.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.core.http.rest.pagedfluxbase.instantiation -->
     * <pre>
     * &#47;&#47; A supplier that fetches the first page of data from source&#47;service
     * Supplier&lt;Mono&lt;PagedResponse&lt;Integer&gt;&gt;&gt; firstPageRetriever = &#40;&#41; -&gt; getFirstPage&#40;&#41;;
     *
     * &#47;&#47; A function that fetches subsequent pages of data from source&#47;service given a continuation token
     * Function&lt;String, Mono&lt;PagedResponse&lt;Integer&gt;&gt;&gt; nextPageRetriever =
     *     continuationToken -&gt; getNextPage&#40;continuationToken&#41;;
     *
     * PagedFluxBase&lt;Integer, PagedResponse&lt;Integer&gt;&gt; pagedFluxBase = new PagedFluxBase&lt;&gt;&#40;firstPageRetriever,
     *     nextPageRetriever&#41;;
     * </pre>
     * <!-- end com.azure.core.http.rest.pagedfluxbase.instantiation -->
     *
     * @param firstPageRetriever Supplier that retrieves the first page
     * @param nextPageRetriever Function that retrieves the next page given a continuation token
     */
    public PagedFluxBase(Supplier<Mono<P>> firstPageRetriever, Function<String, Mono<P>> nextPageRetriever) {
        this(() -> (continuationToken, pageSize) -> continuationToken == null
            ? firstPageRetriever.get().flux()
            : nextPageRetriever.apply(continuationToken).flux(), true);
    }

    /**
     * PACKAGE INTERNAL CONSTRUCTOR, exists only to support the PRIVATE PagedFlux.ctr(Supplier, boolean) use case.
     *
     * Create PagedFlux backed by Page Retriever Function Supplier.
     *
     * @param provider the Page Retrieval Provider
     * @param ignored An additional ignored parameter as generic types are erased and this would conflict with
     * {@link #PagedFluxBase(Supplier)} without it.
     */
    PagedFluxBase(Supplier<PageRetriever<String, P>> provider, boolean ignored) {
        super(provider, null, token -> !CoreUtils.isNullOrEmpty(token));
    }

    /**
     * Creates a Flux of {@link PagedResponse} starting from the first page.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.core.http.rest.pagedfluxbase.bypage -->
     * <pre>
     * &#47;&#47; Start processing the results from first page
     * pagedFluxBase.byPage&#40;&#41;
     *     .log&#40;&#41;
     *     .doOnSubscribe&#40;ignoredVal -&gt; System.out.println&#40;
     *         &quot;Subscribed to paged flux processing pages starting from first page&quot;&#41;&#41;
     *     .subscribe&#40;page -&gt; System.out.printf&#40;&quot;Processing page containing item values: %s%n&quot;,
     *         page.getElements&#40;&#41;.stream&#40;&#41;.map&#40;String::valueOf&#41;.collect&#40;Collectors.joining&#40;&quot;, &quot;&#41;&#41;&#41;,
     *         error -&gt; System.err.println&#40;&quot;An error occurred: &quot; + error&#41;,
     *         &#40;&#41; -&gt; System.out.println&#40;&quot;Processing complete.&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.core.http.rest.pagedfluxbase.bypage -->
     *
     * @return A {@link PagedFluxBase} starting from the first page
     */
    public Flux<P> byPage() {
        return super.byPage();
    }

    /**
     * Creates a Flux of {@link PagedResponse} starting from the next page associated with the given continuation token.
     * To start from first page, use {@link #byPage()} instead.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.core.http.rest.pagedfluxbase.bypage#String -->
     * <pre>
     * &#47;&#47; Start processing the results from a page associated with the continuation token
     * String continuationToken = getContinuationToken&#40;&#41;;
     * pagedFluxBase.byPage&#40;continuationToken&#41;
     *     .log&#40;&#41;
     *     .doOnSubscribe&#40;ignoredVal -&gt; System.out.println&#40;
     *         &quot;Subscribed to paged flux processing page starting from &quot; + continuationToken&#41;&#41;
     *     .subscribe&#40;page -&gt; System.out.printf&#40;&quot;Processing page containing item values: %s%n&quot;,
     *         page.getElements&#40;&#41;.stream&#40;&#41;.map&#40;String::valueOf&#41;.collect&#40;Collectors.joining&#40;&quot;, &quot;&#41;&#41;&#41;,
     *         error -&gt; System.err.println&#40;&quot;An error occurred: &quot; + error&#41;,
     *         &#40;&#41; -&gt; System.out.println&#40;&quot;Processing complete.&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.core.http.rest.pagedfluxbase.bypage#String -->
     *
     * @param continuationToken The continuation token used to fetch the next page
     * @return A {@link PagedFluxBase} starting from the page associated with the continuation token
     */
    public Flux<P> byPage(String continuationToken) {
        return super.byPage(continuationToken);
    }

    /**
     * Subscribe to consume all items of type {@code T} in the sequence respectively. This is recommended for most
     * common scenarios. This will seamlessly fetch next page when required and provide with a {@link Flux} of items.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.core.http.rest.pagedfluxbase.subscribe -->
     * <pre>
     * pagedFluxBase.subscribe&#40;new BaseSubscriber&lt;Integer&gt;&#40;&#41; &#123;
     *     &#64;Override
     *     protected void hookOnSubscribe&#40;Subscription subscription&#41; &#123;
     *         System.out.println&#40;&quot;Subscribed to paged flux processing items&quot;&#41;;
     *         super.hookOnSubscribe&#40;subscription&#41;;
     *     &#125;
     *
     *     &#64;Override
     *     protected void hookOnNext&#40;Integer value&#41; &#123;
     *         System.out.println&#40;&quot;Processing item with value: &quot; + value&#41;;
     *     &#125;
     *
     *     &#64;Override
     *     protected void hookOnComplete&#40;&#41; &#123;
     *         System.out.println&#40;&quot;Processing complete.&quot;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.core.http.rest.pagedfluxbase.subscribe -->
     *
     * @param coreSubscriber The subscriber for this {@link PagedFluxBase}
     */
    @Override
    public void subscribe(CoreSubscriber<? super T> coreSubscriber) {
        super.subscribe(coreSubscriber);
    }
}
