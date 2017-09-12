/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

import com.microsoft.rest.v2.policy.RequestPolicy;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import io.reactivex.netty.protocol.http.client.HttpClientResponse;
import rx.Observable;
import rx.Observer;
import rx.Single;
import rx.functions.Func1;
import rx.observables.SyncOnSubscribe;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
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
    public Single<? extends HttpResponse> sendRequestInternalAsync(HttpRequest request) {
        Single<? extends HttpResponse> result;

        try {
            final URI uri = new URI(request.url());

            Map<String, Set<Object>> rxnHeaders = new HashMap<>();
            for (HttpHeader header : request.headers()) {
                rxnHeaders.put(header.name(), Collections.<Object>singleton(header.value()));
            }

            String mimeType = request.mimeType();
            if (mimeType != null) {
                rxnHeaders.put("Content-Type", Collections.<Object>singleton(mimeType));
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

            HttpClientRequest<ByteBuf, ByteBuf> rxnReq = rxnClient
                    .createRequest(HttpMethod.valueOf(request.httpMethod()), uri.toASCIIString())
                    .addHeaders(rxnHeaders);

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
}
