package com.microsoft.rest.v2.http;

import rx.Single;

public class RequestPolicyChain extends HttpClient {
    private final RequestPolicy.Factory[] factories;
    public RequestPolicyChain(RequestPolicy.Factory... factories) {
        this.factories = factories;
    }

    public RequestPolicy create() {
        RequestPolicy first = null;
        for (RequestPolicy.Factory factory : factories) {
            first = factory.create(first);
        }
        return first;
    }

    @Override
    public Single<? extends HttpResponse> sendRequestAsync(HttpRequest request) {
        return create().sendAsync(request);
    }
}
