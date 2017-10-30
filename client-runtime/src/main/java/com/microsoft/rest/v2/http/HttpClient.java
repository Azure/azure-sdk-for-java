/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

import com.microsoft.rest.v2.policy.RequestPolicy;
import rx.Single;

import java.net.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A generic interface for sending HTTP requests and getting responses.
 */
public abstract class HttpClient {
    private final List<RequestPolicy.Factory> policyFactories;

    private final RequestPolicy lastRequestPolicy = new RequestPolicy() {
        @Override
        public Single<HttpResponse> sendAsync(HttpRequest request) {
            return sendRequestInternalAsync(request);
        }
    };

    protected HttpClient() {
        this.policyFactories = Collections.emptyList();
    }

    protected HttpClient(List<? extends RequestPolicy.Factory> policyFactories) {
        this.policyFactories = new ArrayList<>(policyFactories);

        // Reversing the list facilitates the creation of the RequestPolicy linked list per-request.
        Collections.reverse(this.policyFactories);
    }

    /**
     * Send the provided request asynchronously, applying any request policies provided to the HttpClient instance.
     * @param request The HTTP request to send.
     * @return A {@link Single} representing the HTTP response that will arrive asynchronously.
     */
    public final Single<HttpResponse> sendRequestAsync(HttpRequest request) {
        // Builds a linked list starting from the end.
        RequestPolicy next = lastRequestPolicy;
        for (RequestPolicy.Factory factory : policyFactories) {
            next = factory.create(next);
        }
        return next.sendAsync(request);
    }

    /**
     * Send the provided request asynchronously through the concrete HTTP client implementation.
     * @param request The HTTP request to send.
     * @return A {@link Single} representing the HTTP response that will arrive asynchronously.
     */
    protected abstract Single<HttpResponse> sendRequestInternalAsync(HttpRequest request);

    /**
     * The set of parameters used to create an HTTP client.
     */
    public static final class Configuration {
        private final List<RequestPolicy.Factory> policyFactories;
        private final Proxy proxy;

        /**
         * @return The policy factories to use when creating RequestPolicies to intercept requests.
         */
        public List<RequestPolicy.Factory> policyFactories() {
            return policyFactories;
        }

        /**
         * @return The optional proxy to use.
         */
        public Proxy proxy() {
            return proxy;
        }

        /**
         * Creates a Configuration.
         * @param policyFactories The policy factories to use when creating RequestPolicies to intercept requests.
         * @param proxy The optional proxy to use.
         */
        public Configuration(List<RequestPolicy.Factory> policyFactories, Proxy proxy) {
            this.policyFactories = policyFactories;
            this.proxy = proxy;
        }
    }

    /**
     * Creates an HttpClient from a Configuration.
     */
    public interface Factory {
        /**
         * Creates an HttpClient with the given Configuration.
         * @param configuration the configuration.
         * @return the HttpClient.
         */
        HttpClient create(Configuration configuration);
    }
}
