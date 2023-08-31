package com.azure.core.http.httpurlconnection;

import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.Context;
import com.azure.core.util.Contexts;
import com.azure.core.util.ProgressReporter;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * This client provides asynchronous capabilities to send HTTP requests using HttpUrlConnection.
 * It wraps around the synchronous `HttpUrlConnectionClient` and returns a `CompletableFuture`.
 */
public class AsyncHttpUrlConnectionClient {
    private static final String AZURE_EAGERLY_READ_RESPONSE = "azure-eagerly-read-response";
    private static final String AZURE_IGNORE_RESPONSE_BODY = "azure-ignore-response-body";
    private static final String AZURE_EAGERLY_CONVERT_HEADERS = "azure-eagerly-convert-headers";

    // The synchronous HttpUrlConnection client that performs the actual HTTP calls.
    private final com.azure.core.http.httpurlconnection.HttpUrlConnectionClient httpUrlConnectionClient;

    // Constructor initializes the underlying synchronous HttpUrlConnection client.
    public AsyncHttpUrlConnectionClient() {
        this.httpUrlConnectionClient = new com.azure.core.http.httpurlconnection.HttpUrlConnectionClient();
    }

    /**
     * Sends an asynchronous HTTP request.
     *
     * @param /httpRequest The HTTP request to be sent.
     * @return A CompletableFuture that represents the HttpResponse. This allows the caller
     *         to work with the response once it's available without blocking the main thread.
     */

    public Mono<HttpResponse> send(HttpRequest request) {
        return send(request, Context.NONE);
    }

    public Mono<HttpResponse> send(HttpRequest request, Context context) {
        // boolean eagerlyReadResponse = (boolean) context.getData(AZURE_EAGERLY_READ_RESPONSE).orElse(false);
        // boolean ignoreResponseBody = (boolean) context.getData(AZURE_IGNORE_RESPONSE_BODY).orElse(false);
        // boolean eagerlyConvertHeaders = (boolean) context.getData(AZURE_EAGERLY_CONVERT_HEADERS).orElse(false);

        // ProgressReporter progressReporter = Contexts.with(context).getHttpRequestProgressReporter();

        CompletableFuture<HttpResponse> responseFuture
            = CompletableFuture.supplyAsync(() -> httpUrlConnectionClient.sendSynchronous(request));
        return Mono.fromFuture(responseFuture);
    }
}


