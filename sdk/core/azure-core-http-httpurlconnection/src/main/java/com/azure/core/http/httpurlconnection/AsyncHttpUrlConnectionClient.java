package com.azure.core.http.httpurlconnection;

import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

public class AsyncHttpUrlConnectionClient {
    private final HttpUrlConnectionClient httpUrlConnectionClient;

    public AsyncHttpUrlConnectionClient() {
        this.httpUrlConnectionClient = new HttpUrlConnectionClient();
    }

    public CompletableFuture<HttpResponse> send(HttpRequest httpRequest) {
        Mono<HttpResponse> responseMono = httpUrlConnectionClient.send(httpRequest);
        return responseMono.toFuture();
    }
}


