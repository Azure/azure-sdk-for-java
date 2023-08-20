package com.azure.core.http.httpurlconnection;

import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

/**
 * This client provides asynchronous capabilities to send HTTP requests using HttpUrlConnection.
 * It wraps around the synchronous `HttpUrlConnectionClient` and returns a `CompletableFuture`.
 */
public class AsyncHttpUrlConnectionClient {

    // The synchronous HttpUrlConnection client that performs the actual HTTP calls.
    private final HttpUrlConnectionClient httpUrlConnectionClient;

    // Constructor initializes the underlying synchronous HttpUrlConnection client.
    public AsyncHttpUrlConnectionClient() {
        this.httpUrlConnectionClient = new HttpUrlConnectionClient();
    }

    /**
     * Sends an asynchronous HTTP request.
     *
     * @param httpRequest The HTTP request to be sent.
     * @return A CompletableFuture that represents the HttpResponse. This allows the caller
     *         to work with the response once it's available without blocking the main thread.
     */
    public CompletableFuture<HttpResponse> send(HttpRequest httpRequest) {
        // Convert the synchronous call to asynchronous using Reactor's Mono
        Mono<HttpResponse> responseMono = httpUrlConnectionClient.send(httpRequest);

        // Convert the Mono to CompletableFuture to provide standard Java async capabilities
        return responseMono.toFuture();
    }
}


