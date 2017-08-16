package com.microsoft.rest.v2.http;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpMethod;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import io.reactivex.netty.protocol.http.client.HttpClientResponse;
import rx.Observable;
import rx.Single;
import rx.functions.Func1;

import java.net.URI;
import java.net.URISyntaxException;

public class RxNettyClient extends HttpClient {
    @Override
    public Single<? extends HttpResponse> sendRequestAsync(HttpRequest request) {
        URI uri;
        try {
            uri = new URI(request.getURL());
        } catch (URISyntaxException e) {
            return Single.error(e);
        }

        HttpClientRequest<ByteBuf, ByteBuf> rxnReq = io.reactivex.netty.protocol.http.client.HttpClient
                .newClient(uri.getHost(), 80)
                .createRequest(HttpMethod.valueOf(request.getHttpMethod()), uri.toASCIIString());

        for (HttpHeader header : request.getHeaders()) {
            rxnReq = rxnReq.addHeader(header.getName(), header.getValue());
        }

        String mimeType = request.getMIMEType();
        if (mimeType != null) {
            rxnReq = rxnReq.addHeader("Content-Type", mimeType);
        }

        String body = request.getBody();
        Observable<HttpClientResponse<ByteBuf>> obsResponse = rxnReq;
        if (body != null) {
            if (rxnReq.getHeader("Content-Length") == null) {
                rxnReq = rxnReq.addHeader("Content-Length", body.length());
            }

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
