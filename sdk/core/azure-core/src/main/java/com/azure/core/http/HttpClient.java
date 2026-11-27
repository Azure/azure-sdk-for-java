// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

import com.azure.core.implementation.http.HttpClientProviders;
import com.azure.core.util.Context;
import com.azure.core.util.HttpClientOptions;
import reactor.core.publisher.Mono;

/**
 * A generic interface for sending HTTP requests and getting responses.
 */
public interface HttpClient {
    /**
     * Send the provided request asynchronously.
     *
     * @param request The HTTP request to send.
     * @return A {@link Mono} that emits the response asynchronously.
     */
    Mono<HttpResponse> send(HttpRequest request);

    /**
     * Sends the provided request asynchronously with contextual information.
     *
     * @param request The HTTP request to send.
     * @param context Contextual information about the request.
     * @return A {@link Mono} that emits the response asynchronously.
     */
    default Mono<HttpResponse> send(HttpRequest request, Context context) {
        return send(request);
    }

    /**
     * Sends the provided request synchronously with contextual information.
     *
     * @param request The HTTP request to send.
     * @param context Contextual information about the request.
     * @return The response.
     */
    default HttpResponse sendSync(HttpRequest request, Context context) {
        return send(request, context).block();
    }

    /**
     * Gets whether this client can bind requests to a TLS client certificate, for mutual TLS (mTLS)
     * proof-of-possession.
     * <p>
     * A request carries a {@link com.azure.core.credential.ClientCertificateBinding} in its {@link Context} when its
     * access token is bound to a client certificate (see
     * {@link com.azure.core.util.Contexts#getClientCertificateBinding()}). The {@link HttpPipeline} fails such a
     * request without sending it if this method returns false, so that the token is never sent without its
     * certificate.
     * <p>
     * A client that returns true must send each bound request only over a TLS connection that presents the binding's
     * certificate:
     * <ul>
     * <li>Build the TLS context from {@link com.azure.core.credential.ClientCertificateBinding#getKeyManager()}, using
     * the JDK's TLS implementation, since the private key may only be usable through the Java Cryptography
     * Architecture.</li>
     * <li>Never share a connection between requests bound to different certificates, or between bound and unbound
     * requests.</li>
     * <li>Where the client can inspect the TLS connection before sending, fail the request without sending it unless
     * the connection presented the binding's certificate.</li>
     * <li>Don't follow redirects automatically for bound requests.</li>
     * </ul>
     * A client that wraps another client should return the wrapped client's value.
     *
     * @return Whether this client can bind requests to a TLS client certificate. The default implementation returns
     * false.
     */
    default boolean supportsClientCertificateBinding() {
        return false;
    }

    /**
     * Creates a new {@link HttpClient} instance.
     *
     * @return A new {@link HttpClient} instance.
     */
    static HttpClient createDefault() {
        return createDefault(null);
    }

    /**
     * Creates a new {@link HttpClient} instance.
     *
     * @param clientOptions Configuration options applied to the created {@link HttpClient}.
     * @return A new {@link HttpClient} instance.
     */
    static HttpClient createDefault(HttpClientOptions clientOptions) {
        return HttpClientProviders.createInstance(clientOptions);
    }
}
