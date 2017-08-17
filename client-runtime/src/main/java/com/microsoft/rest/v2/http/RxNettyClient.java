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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RxNettyClient extends HttpClient {
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

        HttpClientRequest<ByteBuf, ByteBuf> rxnReq = io.reactivex.netty.protocol.http.client.HttpClient
                .newClient(uri.getHost(), 80)
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
