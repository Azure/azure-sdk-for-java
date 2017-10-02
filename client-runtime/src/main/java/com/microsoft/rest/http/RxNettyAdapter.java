/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.http;

import com.microsoft.rest.policy.RequestPolicy;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.handler.proxy.HttpProxyHandler;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import io.reactivex.netty.protocol.http.client.HttpClientResponse;
import rx.Observable;
import rx.Observer;
import rx.Single;
import rx.exceptions.Exceptions;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.observables.SyncOnSubscribe;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.io.IOException;
import java.io.InputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A HttpClient that is implemented using RxNetty.
 */
public class RxNettyAdapter extends HttpClient {
    private final List<ChannelHandlerConfig> handlerConfigs;
    private final CookieHandler cookies = new CookieManager();

    /**
     * Creates RxNettyClient.
     * @param policyFactories the sequence of RequestPolicies to apply when sending HTTP requests.
     * @param handlerConfigs the Netty ChannelHandler configurations.
     */
    public RxNettyAdapter(List<RequestPolicy.Factory> policyFactories, List<ChannelHandlerConfig> handlerConfigs) {
        super(policyFactories);
        this.handlerConfigs = handlerConfigs;
    }

    private SSLEngine getSSLEngine(String host) {
        SSLContext sslCtx;
        try {
            sslCtx = SSLContext.getDefault();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        SSLEngine engine = sslCtx.createSSLEngine(host, 443);
        engine.setUseClientMode(true);
        return engine;
    }

    @Override
    public Single<HttpResponse> sendRequestInternalAsync(final HttpRequest request) {
        Single<HttpResponse> result;

        try {
            final URI uri = new URI(request.url());

            // Unfortunately necessary due to conflicting APIs
            Map<String, List<String>> cookieHeaders = new HashMap<>();
            Map<String, Set<Object>> rxnHeaders = new HashMap<>();
            for (HttpHeader header : request.headers()) {
                cookieHeaders.put(header.name(), Arrays.asList(request.headers().values(header.name())));
                rxnHeaders.put(header.name(), Collections.<Object>singleton(header.value()));
            }

            final boolean isSecure = "https".equalsIgnoreCase(uri.getScheme());
            final int port;
            if (uri.getPort() != -1) {
                port = uri.getPort();
            } else {
                port = isSecure ? 443 : 80;
            }

            io.reactivex.netty.protocol.http.client.HttpClient<ByteBuf, ByteBuf> rxnClient =
                    io.reactivex.netty.protocol.http.client.HttpClient.newClient(uri.getHost(), port);

            for (int i = 0; i < handlerConfigs.size(); i++) {
                ChannelHandlerConfig config = handlerConfigs.get(i);
                if (config.mayBlock()) {
                    EventExecutorGroup executorGroup = new DefaultEventExecutorGroup(1);
                    rxnClient = rxnClient.addChannelHandlerLast(executorGroup, "az-client-handler-" + i, config.factory());
                } else {
                    rxnClient = rxnClient.addChannelHandlerLast("az-client-handler-" + i, config.factory());
                }
            }

            if (isSecure) {
                rxnClient = rxnClient.secure(getSSLEngine(uri.getHost()));
            }

            Map<String, List<String>> requestCookies = cookies.get(uri, cookieHeaders);
            Map<String, Iterable<Object>> rxnCookies = new HashMap<>();
            for (Map.Entry<String, List<String>> entry : requestCookies.entrySet()) {
                if (entry.getValue().size() != 0) {
                    List<Object> cookieValues = new ArrayList<>();
                    cookieValues.addAll(entry.getValue());
                    rxnCookies.put(entry.getKey(), cookieValues);
                }
            }

            final HttpClientRequest<ByteBuf, ByteBuf> rxnReq = rxnClient
                    .createRequest(HttpMethod.valueOf(request.httpMethod()), uri.toASCIIString())
                    .addHeaders(rxnHeaders)
                    .addHeaders(rxnCookies);

            Observable<HttpClientResponse<ByteBuf>> obsResponse = rxnReq;

            final HttpRequestBody body = request.body();
            if (body != null) {
                try (final InputStream bodyStream = body.createInputStream()) {
                    obsResponse = rxnReq.writeBytesContent(toByteArrayObservable(bodyStream));
                }
            }


            result = obsResponse
                    .map(new Func1<HttpClientResponse<ByteBuf>, HttpResponse>() {
                        @Override
                        public HttpResponse call(HttpClientResponse<ByteBuf> rxnRes) {
                            Map<String, List<String>> responseHeaders = new HashMap<>();
                            for (String headerName : rxnRes.getHeaderNames()) {
                                responseHeaders.put(headerName, rxnRes.getAllHeaderValues(headerName));
                            }
                            try {
                                cookies.put(uri, responseHeaders);
                            } catch (IOException e) {
                                throw Exceptions.propagate(e);
                            }

                            return new RxNettyResponse(rxnRes);
                        }
                    })
                    .toSingle();
        } catch (URISyntaxException | IOException e) {
            result = Single.error(e);
        }

        return result;
    }

    // This InputStream to Observable<byte[]> conversion comes from rxjava-string
    // (https://github.com/ReactiveX/RxJavaString). We can't just take a dependency on
    // rxjava-string, however, because they require an older version of rxjava (1.1.1).
    private static Observable<byte[]> toByteArrayObservable(InputStream inputStream) {
        return Observable.create(new OnSubscribeInputStream(inputStream, 8 * 1024));
    }

    private static final class OnSubscribeInputStream extends SyncOnSubscribe<InputStream, byte[]> {
        private final InputStream is;
        private final int size;

        OnSubscribeInputStream(InputStream is, int size) {
            this.is = is;
            this.size = size;
        }

        @Override
        protected InputStream generateState() {
            return this.is;
        }

        @Override
        protected InputStream next(InputStream state, Observer<? super byte[]> observer) {
            byte[] buffer = new byte[size];
            try {
                int count = state.read(buffer);
                if (count == -1) {
                    observer.onCompleted();
                } else if (count < size) {
                    observer.onNext(Arrays.copyOf(buffer, count));
                } else {
                    observer.onNext(buffer);
                }
            } catch (IOException e) {
                observer.onError(e);
            }
            return state;
        }
    }

    /**
     * The builder class for building a RxNettyAdapter.
     */
    public static class Builder {
        private final List<RequestPolicy.Factory> requestPolicyFactories = new ArrayList<>();
        private final List<ChannelHandlerConfig> channelHandlerConfigs = new ArrayList<>();

        /**
         * Add the provided RequestPolicy.Factory to this Builder's configuration.
         * @param requestPolicyFactory The RequestPolicy.Factory to add.
         * @return The Builder itself for chaining.
         */
        public Builder withRequestPolicy(RequestPolicy.Factory requestPolicyFactory) {
            requestPolicyFactories.add(requestPolicyFactory);
            return this;
        }

        /**
         * Add the provided RequestPolicy.Factories to this Builder's configuration.
         * @param requestPolicyFactories The RequestPolicy.Factories to add.
         * @return The Builder itself for chaining.
         */
        public Builder withRequestPolicies(Collection<RequestPolicy.Factory> requestPolicyFactories) {
            this.requestPolicyFactories.addAll(requestPolicyFactories);
            return this;
        }

        /**
         * Add the provided ChannelHandlerConfig to this Builder's configuration.
         * @param channelHandlerConfig The ChannelHandlerConfig to add.
         * @return The Builder itself for chaining.
         */
        public Builder withChannelHandler(ChannelHandlerConfig channelHandlerConfig) {
            channelHandlerConfigs.add(channelHandlerConfig);
            return this;
        }

        /**
         * Add a Proxy to the RxNettyAdapter that will be built from this Builder.
         * @param proxy The Proxy to add.
         * @return The Builder itself for chaining.
         */
        public Builder withProxy(final Proxy proxy) {
            return withChannelHandler(new ChannelHandlerConfig(new Func0<ChannelHandler>() {
                @Override
                public ChannelHandler call() {
                    return new HttpProxyHandler(proxy.address());
                }
            },
            false));
        }

        /**
         * Build a RxNettyAdapter using this Builder's configuration.
         * @return An RxNettyAdapter that uses this Builder's configuration.
         */
        public RxNettyAdapter build() {
            return new RxNettyAdapter(requestPolicyFactories, channelHandlerConfigs);
        }
    }
}
