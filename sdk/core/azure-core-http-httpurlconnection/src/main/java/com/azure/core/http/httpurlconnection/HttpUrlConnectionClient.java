package com.azure.core.http.httpurlconnection;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;

import reactor.core.publisher.Mono;

public class HttpUrlConnectionClient implements HttpClient {

    @Override
    public Mono<HttpResponse> send(HttpRequest httpRequest) {
        return null;
    }
}
