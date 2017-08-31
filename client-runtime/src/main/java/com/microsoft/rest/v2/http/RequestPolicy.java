package com.microsoft.rest.v2.http;

import rx.Single;

public interface RequestPolicy {
    Single<HttpResponse> sendAsync(HttpRequest request);

    interface Factory {
        RequestPolicy create(RequestPolicy next);
    }
}
