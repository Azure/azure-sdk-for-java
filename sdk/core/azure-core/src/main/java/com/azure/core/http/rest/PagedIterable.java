// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import com.azure.core.util.IterableStream;

import java.util.function.Function;
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
