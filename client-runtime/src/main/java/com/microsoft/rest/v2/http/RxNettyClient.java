/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpMethod;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import io.reactivex.netty.protocol.http.client.HttpClientResponse;
import io.reactivex.netty.ssl.SslCodec;
import rx.Observable;
import rx.Single;
import rx.functions.Func1;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A HttpClient that is implemented using RxNetty.
 */
public class RxNettyClient extends HttpClient {
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
    public Single<? extends HttpResponse> sendRequestAsync(HttpRequest request) {
        URI uri;
        try {
            uri = new URI(request.url());
        } catch (URISyntaxException e) {
            return Single.error(e);
        }

        Map<String, Set<Object>> rxnHeaders = new HashMap<>();
        for (HttpHeader header : request.headers()) {
            rxnHeaders.put(header.name(), Collections.<Object>singleton(header.value()));
        }

        String mimeType = request.mimeType();
        if (mimeType != null) {
            rxnHeaders.put("Content-Type", Collections.<Object>singleton(mimeType));
        }

        String body = request.body();
        if (body != null) {
            rxnHeaders.put("Content-Length", Collections.<Object>singleton(String.valueOf(body.length())));
        }

        boolean isSecure = "https".equalsIgnoreCase(uri.getScheme());
        io.reactivex.netty.protocol.http.client.HttpClient<ByteBuf, ByteBuf> rxnClient =
                io.reactivex.netty.protocol.http.client.HttpClient.newClient(uri.getHost(), isSecure ? 443 : 80);

        if (isSecure) {
            rxnClient = rxnClient.secure(getSSLEngine(uri.getHost()));
        }

        HttpClientRequest<ByteBuf, ByteBuf> rxnReq = rxnClient
                .createRequest(HttpMethod.valueOf(request.httpMethod()), uri.toASCIIString())
                .addHeaders(rxnHeaders);

        Observable<HttpClientResponse<ByteBuf>> obsResponse = rxnReq;
        if (body != null) {
            obsResponse = rxnReq.writeStringContent(Observable.just(body));
        }

        return obsResponse
                .map(new Func1<HttpClientResponse<ByteBuf>, HttpResponse>() {
                    @Override
                    public HttpResponse call(HttpClientResponse<ByteBuf> rxnRes) {
                        return new RxNettyResponse(rxnRes);
                    }
                })
                .toSingle();
    }
}
