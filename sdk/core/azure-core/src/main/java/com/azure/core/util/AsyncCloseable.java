// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import reactor.core.publisher.Mono;

/**
 * Interface for close operations that are asynchronous.
 *
 * <p><strong>Asynchronously closing a class</strong></p>
 * <p>In the snippet below, we have a long-lived {@code NetworkResource} class. There are some operations such
 * as closing {@literal I/O}. Instead of returning a sync {@code close()}, we use {@code closeAsync()} so users'
 * programs don't block waiting for this operation to complete.</p>
 *
 * <!-- src_embed com.azure.core.util.AsyncCloseable.closeAsync -->
 * <pre>
 * NetworkResource resource = new NetworkResource&#40;&#41;;
 * resource.longRunningDownload&#40;&quot;https:&#47;&#47;longdownload.com&quot;&#41;
 *     .subscribe&#40;
 *         byteBuffer -&gt; System.out.println&#40;&quot;Buffer received: &quot; + byteBuffer&#41;,
 *         error -&gt; System.err.printf&#40;&quot;Error occurred while downloading: %s%n&quot;, error&#41;,
 *         &#40;&#41; -&gt; System.out.println&#40;&quot;Completed download operation.&quot;&#41;&#41;;
 *
 * System.out.println&#40;&quot;Press enter to stop downloading.&quot;&#41;;
 * System.in.read&#40;&#41;;
 *
 * &#47;&#47; We block here because it is the end of the main Program function. A real-life program may chain this
 * &#47;&#47; with some other close operations like save download&#47;program state, etc.
 * resource.closeAsync&#40;&#41;.block&#40;&#41;;
 * </pre>
 * <!-- end com.azure.core.util.AsyncCloseable.closeAsync -->
 */
public interface AsyncCloseable {
    /**
     * Begins the close operation. If one is in progress, will return that existing close operation. If the close
     * operation is unsuccessful, the Mono completes with an error.
     *
     * @return A Mono representing the close operation. If the close operation is unsuccessful, the Mono completes with
     *     an error.
     */
    Mono<Void> closeAsync();
}
