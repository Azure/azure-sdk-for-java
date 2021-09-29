// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import com.azure.core.http.HttpRequest;
import com.azure.core.util.paging.PageRetriever;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * PagedFlux is a Flux that provides the ability to operate on paginated REST responses of type {@link PagedResponse}
 * and individual items in such pages. When processing the response by page each response will contain the items in the
 * page as well as the REST response details such as status code and headers.
 *
 * <p>To process one item at a time, simply subscribe to this flux as shown below </p>
 * <p><strong>Code sample</strong></p>
 * <!-- src_embed com.azure.core.http.rest.pagedflux.items -->
 * <pre>
 * &#47;&#47; Subscribe to process one item at a time
 * pagedFlux
 *     .log&#40;&#41;
 *     .subscribe&#40;item -&gt; System.out.println&#40;&quot;Processing item with value: &quot; + item&#41;,
 *         error -&gt; System.err.println&#40;&quot;An error occurred: &quot; + error&#41;,
 *         &#40;&#41; -&gt; System.out.println&#40;&quot;Processing complete.&quot;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.core.http.rest.pagedflux.items -->
 *
 * <p>To process one page at a time, use {@link #byPage()} method as shown below </p>
 * <p><strong>Code sample</strong></p>
 * <!-- src_embed com.azure.core.http.rest.pagedflux.pages -->
 * <pre>
 * &#47;&#47; Subscribe to process one page at a time from the beginning
 * pagedFlux
 *     .byPage&#40;&#41;
 *     .log&#40;&#41;
 *     .subscribe&#40;page -&gt; System.out.printf&#40;&quot;Processing page containing item values: %s%n&quot;,
 *         page.getElements&#40;&#41;.stream&#40;&#41;.map&#40;String::valueOf&#41;.collect&#40;Collectors.joining&#40;&quot;, &quot;&#41;&#41;&#41;,
 *         error -&gt; System.err.println&#40;&quot;An error occurred: &quot; + error&#41;,
 *         &#40;&#41; -&gt; System.out.println&#40;&quot;Processing complete.&quot;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.core.http.rest.pagedflux.pages -->
 *
 * <p>To process items one page at a time starting from any page associated with a continuation token,
 * use {@link #byPage(String)} as shown below</p>
 * <p><strong>Code sample</strong></p>
 * <!-- src_embed com.azure.core.http.rest.pagedflux.pagesWithContinuationToken -->
 * <pre>
 * &#47;&#47; Subscribe to process one page at a time starting from a page associated with
 * &#47;&#47; a continuation token
 * String continuationToken = getContinuationToken&#40;&#41;;
 * pagedFlux
 *     .byPage&#40;continuationToken&#41;
 *     .log&#40;&#41;
 *     .doOnSubscribe&#40;ignored -&gt; System.out.println&#40;
 *         &quot;Subscribed to paged flux processing pages starting from: &quot; + continuationToken&#41;&#41;
 *     .subscribe&#40;page -&gt; System.out.printf&#40;&quot;Processing page containing item values: %s%n&quot;,
 *         page.getElements&#40;&#41;.stream&#40;&#41;.map&#40;String::valueOf&#41;.collect&#40;Collectors.joining&#40;&quot;, &quot;&#41;&#41;&#41;,
 *         error -&gt; System.err.println&#40;&quot;An error occurred: &quot; + error&#41;,
 *         &#40;&#41; -&gt; System.out.println&#40;&quot;Processing complete.&quot;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.core.http.rest.pagedflux.pagesWithContinuationToken -->
 *
 * @param <T> The type of items in a {@link PagedResponse}
 * @see PagedResponse
 * @see Page
 * @see Flux
 */
@SuppressWarnings("deprecation")
public class PagedFlux<T> extends PagedFluxBase<T, PagedResponse<T>> {
    /**
     * Creates an instance of {@link PagedFlux} that consists of only a single page. This constructor takes a {@code
     * Supplier} that return the single page of {@code T}.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.core.http.rest.pagedflux.singlepage.instantiation -->
     * <pre>
     * &#47;&#47; A supplier that fetches the first page of data from source&#47;service
     * Supplier&lt;Mono&lt;PagedResponse&lt;Integer&gt;&gt;&gt; firstPageRetrieverFunction = &#40;&#41; -&gt; getFirstPage&#40;&#41;;
     *
     * PagedFlux&lt;Integer&gt; pagedFluxInstance = new PagedFlux&lt;&gt;&#40;firstPageRetrieverFunction,
     *     nextPageRetriever&#41;;
     * </pre>
     * <!-- end com.azure.core.http.rest.pagedflux.singlepage.instantiation -->
     *
     * @param firstPageRetriever Supplier that retrieves the first page.
     */
    public PagedFlux(Supplier<Mono<PagedResponse<T>>> firstPageRetriever) {
        this(firstPageRetriever, token -> Mono.empty());
    }

    /**
     * Creates an instance of {@link PagedFlux} that consists of only a single page with a given element count.
     *
     * <p><strong>Code sample</strong></p>
     *
     * <!-- src_embed com.azure.core.http.rest.PagedFlux.singlepage.instantiationWithPageSize -->
     * <pre>
     * &#47;&#47; A function that fetches the single page of data from a source&#47;service.
     * Function&lt;Integer, Mono&lt;PagedResponse&lt;Integer&gt;&gt;&gt; singlePageRetriever = pageSize -&gt;
     *     getFirstPageWithSize&#40;pageSize&#41;;
     *
     * PagedFlux&lt;Integer&gt; singlePageFluxWithPageSize = new PagedFlux&lt;Integer&gt;&#40;singlePageRetriever&#41;;
     * </pre>
     * <!-- end com.azure.core.http.rest.PagedFlux.singlepage.instantiationWithPageSize -->
     *
     * @param firstPageRetriever Function that retrieves the first page.
     */
    public PagedFlux(Function<Integer, Mono<PagedResponse<T>>> firstPageRetriever) {
        this(firstPageRetriever, (token, pageSize) -> Mono.empty());
    }

    /**
     * Creates an instance of {@link PagedFlux}. The constructor takes a {@code Supplier} and {@code Function}. The
     * {@code Supplier} returns the first page of {@code T}, the {@code Function} retrieves subsequent pages of {@code
     * T}.
     *
     * <p><strong>Code sample</strong></p>
     *
     * <!-- src_embed com.azure.core.http.rest.pagedflux.instantiation -->
     * <pre>
     * &#47;&#47; A supplier that fetches the first page of data from source&#47;service
     * Supplier&lt;Mono&lt;PagedResponse&lt;Integer&gt;&gt;&gt; firstPageRetriever = &#40;&#41; -&gt; getFirstPage&#40;&#41;;
     *
     * &#47;&#47; A function that fetches subsequent pages of data from source&#47;service given a continuation token
     * Function&lt;String, Mono&lt;PagedResponse&lt;Integer&gt;&gt;&gt; nextPageRetriever =
     *     continuationToken -&gt; getNextPage&#40;continuationToken&#41;;
     *
     * PagedFlux&lt;Integer&gt; pagedFlux = new PagedFlux&lt;&gt;&#40;firstPageRetriever,
     *     nextPageRetriever&#41;;
     * </pre>
     * <!-- end com.azure.core.http.rest.pagedflux.instantiation -->
     *
     * @param firstPageRetriever Supplier that retrieves the first page
     * @param nextPageRetriever Function that retrieves the next page given a continuation token
     */
    public PagedFlux(Supplier<Mono<PagedResponse<T>>> firstPageRetriever,
        Function<String, Mono<PagedResponse<T>>> nextPageRetriever) {
        this(() -> (continuationToken, pageSize) -> continuationToken == null
            ? firstPageRetriever.get().flux()
            : nextPageRetriever.apply(continuationToken).flux(), true);
    }

    /**
     * Creates an instance of {@link PagedFlux} that is capable of retrieving multiple pages with of a given page size.
     *
     * <p><strong>Code sample</strong></p>
     *
     * <!-- src_embed com.azure.core.http.rest.PagedFlux.instantiationWithPageSize -->
     * <pre>
     * &#47;&#47; A function that fetches the first page of data from a source&#47;service.
     * Function&lt;Integer, Mono&lt;PagedResponse&lt;Integer&gt;&gt;&gt; firstPageRetriever = pageSize -&gt; getFirstPageWithSize&#40;pageSize&#41;;
     *
     * &#47;&#47; A function that fetches subsequent pages of data from a source&#47;service given a continuation token.
     * BiFunction&lt;String, Integer, Mono&lt;PagedResponse&lt;Integer&gt;&gt;&gt; nextPageRetriever = &#40;continuationToken, pageSize&#41; -&gt;
     *     getNextPageWithSize&#40;continuationToken, pageSize&#41;;
     *
     * PagedFlux&lt;Integer&gt; pagedFluxWithPageSize = new PagedFlux&lt;&gt;&#40;firstPageRetriever, nextPageRetriever&#41;;
     * </pre>
     * <!-- end com.azure.core.http.rest.PagedFlux.instantiationWithPageSize -->
     *
     * @param firstPageRetriever Function that retrieves the first page.
     * @param nextPageRetriever BiFunction that retrieves the next page given a continuation token and page size.
     */
    public PagedFlux(Function<Integer, Mono<PagedResponse<T>>> firstPageRetriever,
        BiFunction<String, Integer, Mono<PagedResponse<T>>> nextPageRetriever) {
        this(() -> (continuationToken, pageSize) -> continuationToken == null
            ? firstPageRetriever.apply(pageSize).flux()
            : nextPageRetriever.apply(continuationToken, pageSize).flux(), true);
    }

    /**
     * Create PagedFlux backed by Page Retriever Function Supplier.
     *
     * @param provider the Page Retrieval Provider
     * @param ignored param is ignored, exists in signature only to avoid conflict with first ctr
     */
    private PagedFlux(Supplier<PageRetriever<String, PagedResponse<T>>> provider, boolean ignored) {
        super(provider, ignored);
    }

    /**
     * Creates an instance of {@link PagedFlux} backed by a Page Retriever Supplier (provider). When invoked provider
     * should return {@link PageRetriever}. The provider will be called for each Subscription to the PagedFlux instance.
     * The Page Retriever can get called multiple times in serial fashion, each time after the completion of the Flux
     * returned from the previous invocation. The final completion signal will be send to the Subscriber when the last
     * Page emitted by the Flux returned by Page Retriever has {@code null} continuation token.
     *
     * The provider is useful mainly in two scenarios:
     * <ul>
     * <li> To manage state across multiple call to Page Retrieval within the same Subscription.
     * <li> To decorate a PagedFlux to produce new PagedFlux.
     * </ul>
     *
     * <p><strong>Decoration sample</strong></p>
     * <!-- src_embed com.azure.core.http.rest.pagedflux.create.decoration -->
     * <pre>
     *
     * &#47;&#47; Transform a PagedFlux with Integer items to PagedFlux of String items.
     * final PagedFlux&lt;Integer&gt; intPagedFlux = createAnInstance&#40;&#41;;
     *
     * &#47;&#47; PagedResponse&lt;Integer&gt; to PagedResponse&lt;String&gt; mapper
     * final Function&lt;PagedResponse&lt;Integer&gt;, PagedResponse&lt;String&gt;&gt; responseMapper
     *     = intResponse -&gt; new PagedResponseBase&lt;Void, String&gt;&#40;intResponse.getRequest&#40;&#41;,
     *     intResponse.getStatusCode&#40;&#41;,
     *     intResponse.getHeaders&#40;&#41;,
     *     intResponse.getValue&#40;&#41;
     *         .stream&#40;&#41;
     *         .map&#40;intValue -&gt; Integer.toString&#40;intValue&#41;&#41;.collect&#40;Collectors.toList&#40;&#41;&#41;,
     *     intResponse.getContinuationToken&#40;&#41;,
     *     null&#41;;
     *
     * final Supplier&lt;PageRetriever&lt;String, PagedResponse&lt;String&gt;&gt;&gt; provider = &#40;&#41; -&gt;
     *     &#40;continuationToken, pageSize&#41; -&gt; &#123;
     *         Flux&lt;PagedResponse&lt;Integer&gt;&gt; flux = &#40;continuationToken == null&#41;
     *             ? intPagedFlux.byPage&#40;&#41;
     *             : intPagedFlux.byPage&#40;continuationToken&#41;;
     *         return flux.map&#40;responseMapper&#41;;
     *     &#125;;
     * PagedFlux&lt;String&gt; strPagedFlux = PagedFlux.create&#40;provider&#41;;
     *
     * &#47;&#47; Create a PagedFlux from a PagedFlux with all exceptions mapped to a specific exception.
     * final PagedFlux&lt;Integer&gt; pagedFlux = createAnInstance&#40;&#41;;
     * final Supplier&lt;PageRetriever&lt;String, PagedResponse&lt;Integer&gt;&gt;&gt; eprovider = &#40;&#41; -&gt;
     *     &#40;continuationToken, pageSize&#41; -&gt; &#123;
     *         Flux&lt;PagedResponse&lt;Integer&gt;&gt; flux = &#40;continuationToken == null&#41;
     *             ? pagedFlux.byPage&#40;&#41;
     *             : pagedFlux.byPage&#40;continuationToken&#41;;
     *         return flux.onErrorMap&#40;PaginationException::new&#41;;
     *     &#125;;
     * final PagedFlux&lt;Integer&gt; exceptionMappedPagedFlux = PagedFlux.create&#40;eprovider&#41;;
     * </pre>
     * <!-- end com.azure.core.http.rest.pagedflux.create.decoration -->
     *
     * @param provider the Page Retrieval Provider
     * @param <T> The type of items in a {@link PagedResponse}
     * @return PagedFlux backed by the Page Retriever Function Supplier
     */
    public static <T> PagedFlux<T> create(Supplier<PageRetriever<String, PagedResponse<T>>> provider) {
        return new PagedFlux<>(provider, true);
    }

    /**
     * Maps this PagedFlux instance of T to a PagedFlux instance of type S as per the provided mapper function.
     *
     * @param mapper The mapper function to convert from type T to type S.
     * @param <S> The mapped type.
     * @return A PagedFlux of type S.
     * @deprecated refer the decoration samples for {@link PagedFlux#create(Supplier)}.
     */
    @Deprecated
    public <S> PagedFlux<S> mapPage(Function<T, S> mapper) {
        Supplier<PageRetriever<String, PagedResponse<S>>> provider = () -> (continuationToken, pageSize) -> {
            Flux<PagedResponse<T>> flux = (continuationToken == null)
                ? byPage()
                : byPage(continuationToken);
            return flux.map(mapPagedResponse(mapper));
        };
        return PagedFlux.create(provider);
    }

    private <S> Function<PagedResponse<T>, PagedResponse<S>> mapPagedResponse(Function<T, S> mapper) {
        return pagedResponse -> new PagedResponseBase<HttpRequest, S>(pagedResponse.getRequest(),
            pagedResponse.getStatusCode(),
            pagedResponse.getHeaders(),
            pagedResponse.getValue().stream().map(mapper).collect(Collectors.toList()),
            pagedResponse.getContinuationToken(),
            null);
    }
}
