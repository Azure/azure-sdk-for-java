package com.microsoft.rest.v2.http;

import rx.Single;

import java.net.MalformedURLException;
import java.net.URL;

public class UseOtherHostPolicy implements RequestPolicy {
    public static class Factory implements RequestPolicy.Factory {

        @Override
        public RequestPolicy create(RequestPolicy next) {
            return new UseOtherHostPolicy(next);
        }
    }

    private final RequestPolicy next;
    UseOtherHostPolicy(RequestPolicy next) {
        this.next = next;
    }

    @Override
    public Single<HttpResponse> sendAsync(HttpRequest request) {
        URL url;
        try {
           url = new URL(request.url());
        } catch (MalformedURLException e) {
            return Single.error(e);
        }

        String newURL = "https://httpbin.org/" + url.getPath() + url.getQuery();

        HttpRequest newRequest = new HttpRequest(request.callerMethod(), request.httpMethod(), newURL);
        newRequest.withBody(request.body(), request.mimeType());

        return next.sendAsync(newRequest);
    }
}
