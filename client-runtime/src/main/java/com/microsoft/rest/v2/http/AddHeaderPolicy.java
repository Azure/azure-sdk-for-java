package com.microsoft.rest.v2.http;

import rx.Single;

public class AddHeaderPolicy implements RequestPolicy {
    private final RequestPolicy next;
    public AddHeaderPolicy(RequestPolicy next) {
        this.next = next;
    }

    static class Factory implements RequestPolicy.Factory {
        @Override
        public RequestPolicy create(RequestPolicy next) {
            return new AddHeaderPolicy(next);
        }
    }

    @Override
    public Single<HttpResponse> sendAsync(HttpRequest request) {

        request.headers().add("x-my-header", "42");
        return next.sendAsync(request);
    }
}
