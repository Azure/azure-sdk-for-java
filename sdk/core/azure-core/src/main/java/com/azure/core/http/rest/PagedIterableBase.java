// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import com.azure.core.util.IterableStream;
import com.azure.core.util.paging.ContinuablePagedIterable;

import java.util.stream.Stream;

/**
 * This class provides utility to iterate over responses that extend {@link PagedResponse} using {@link Stream} and
 * {@link Iterable} interfaces.
 *
 * <p><strong>Code sample using {@link Stream} by page</strong></p>
 *
 * <!-- src_embed com.azure.core.http.rest.pagedIterableBase.streamByPage -->
 * <pre>
 * &#47;&#47; process the streamByPage
 * CustomPagedFlux&lt;String&gt; customPagedFlux = createCustomInstance&#40;&#41;;
 * PagedIterableBase&lt;String, PagedResponse&lt;String&gt;&gt; customPagedIterableResponse =
 *     new PagedIterableBase&lt;&gt;&#40;customPagedFlux&#41;;
 * customPagedIterableResponse.streamByPage&#40;&#41;.forEach&#40;resp -&gt; &#123;
 *     System.out.printf&#40;&quot;Response headers are %s. Url %s  and status code %d %n&quot;, resp.getHeaders&#40;&#41;,
 *         resp.getRequest&#40;&#41;.getUrl&#40;&#41;, resp.getStatusCode&#40;&#41;&#41;;
 *     resp.getElements&#40;&#41;.forEach&#40;value -&gt; System.out.printf&#40;&quot;Response value is %s %n&quot;, value&#41;&#41;;
 * &#125;&#41;;
 * </pre>
 * <!-- end com.azure.core.http.rest.pagedIterableBase.streamByPage -->
 *
 * <p><strong>Code sample using {@link Iterable} by page</strong></p>
 *
 * <!-- src_embed com.azure.core.http.rest.pagedIterableBase.iterableByPage -->
 * <pre>
 * &#47;&#47; process the iterableByPage
 * customPagedIterableResponse.iterableByPage&#40;&#41;.forEach&#40;resp -&gt; &#123;
 *     System.out.printf&#40;&quot;Response headers are %s. Url %s  and status code %d %n&quot;, resp.getHeaders&#40;&#41;,
 *         resp.getRequest&#40;&#41;.getUrl&#40;&#41;, resp.getStatusCode&#40;&#41;&#41;;
 *     resp.getElements&#40;&#41;.forEach&#40;value -&gt; System.out.printf&#40;&quot;Response value is %s %n&quot;, value&#41;&#41;;
 * &#125;&#41;;
 * </pre>
 * <!-- end com.azure.core.http.rest.pagedIterableBase.iterableByPage -->
 *
 * <p><strong>Code sample using {@link Iterable} by page and while loop</strong></p>
 *
 * <!-- src_embed com.azure.core.http.rest.pagedIterableBase.iterableByPage.while -->
 * <pre>
 * &#47;&#47; iterate over each page
 * for &#40;PagedResponse&lt;String&gt; resp : customPagedIterableResponse.iterableByPage&#40;&#41;&#41; &#123;
 *     System.out.printf&#40;&quot;Response headers are %s. Url %s  and status code %d %n&quot;, resp.getHeaders&#40;&#41;,
 *         resp.getRequest&#40;&#41;.getUrl&#40;&#41;, resp.getStatusCode&#40;&#41;&#41;;
 *     resp.getElements&#40;&#41;.forEach&#40;value -&gt; System.out.printf&#40;&quot;Response value is %s %n&quot;, value&#41;&#41;;
 * &#125;
 * </pre>
 * <!-- end com.azure.core.http.rest.pagedIterableBase.iterableByPage.while -->
 *
 * @param <T> The type of value contained in this {@link IterableStream}.
 * @param <P> The response extending from {@link PagedResponse}
 * @see PagedResponse
 * @see IterableStream
 */
public class PagedIterableBase<T, P extends PagedResponse<T>> extends ContinuablePagedIterable<String, T, P> {
    /**
     * Creates instance given {@link PagedFluxBase}.
     *
     * @param pagedFluxBase to use as iterable
     */
    @SuppressWarnings("deprecation")
    public PagedIterableBase(PagedFluxBase<T, P> pagedFluxBase) {
        super(pagedFluxBase);
    }
}
