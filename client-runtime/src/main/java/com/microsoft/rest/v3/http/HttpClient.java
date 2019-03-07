/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v3.http;

import reactor.core.publisher.Mono;
import java.util.function.Supplier;

/**
 * A generic interface for sending HTTP requests and getting responses.
 */
public abstract class HttpClient {
    /**
     * Send the provided request asynchronously.
     *
     * @param request The HTTP request to send
     * @return A {@link Mono} that emits response asynchronously
     */
    public abstract Mono<HttpResponse> send(HttpRequest request);

    /**
     * Create default HttpClient instance.
     *
     * @return the HttpClient
     */
    public static HttpClient createDefault() {
        return new ReactorNettyClient();
    }

    /**
     * Apply the provided proxy configuration to the HttpClient.
     *
     * @param proxyOptions the proxy configuration supplier
     * @return a HttpClient with proxy applied
     */
    public final HttpClient proxy(Supplier<ProxyOptions> proxyOptions) {
        return this.setProxy(proxyOptions);
    }

    /**
     * Apply or remove a wire logger configuration.
     *
     * @param enableWiretap wiretap config
     * @return a HttpClient with wire logging enabled or disabled
     */
    public final HttpClient wiretap(boolean enableWiretap) {
        return this.setWiretap(enableWiretap);
    }

    /**
     * Set the port that client should connect to.
     *
     * @param port the port
     * @return a HttpClient with port applied
     */
    public final HttpClient port(int port) {
        return this.setPort(port);
    }

    /**
     * Set the proxy.
     *
     * The concrete implementation of HttpClient should override this
     * method and apply the provided configuration.
     *
     * @param proxyOptionsSupplier the proxy configuration supplier
     * @return a HttpClient with proxy applied
     */
    protected HttpClient setProxy(Supplier<ProxyOptions> proxyOptionsSupplier) {
        return this;
    }

    /**
     * Set wiretap.
     *
     * The concrete implementation of HttpClient should override this
     * method and apply the provided configuration.
     *
     * @param enableWiretap wiretap config
     * @return a HttpClient with wire logging enabled or disabled
     */
    protected HttpClient setWiretap(boolean enableWiretap) {
        return this;
    }

    /**
     * Set the port that client should connect to.
     *
     * The concrete implementation of HttpClient should override this
     * method and apply the provided configuration.
     *
     * @param port the port
     * @return a HttpClient with port applied
     */
    protected HttpClient setPort(int port) {
        return this;
    }
}
