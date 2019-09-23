package com.azure.storage.blob.specialized;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

final class BatchClient implements HttpClient {
    private static final String BATCH_BOUNDARY_TEMPLATE = "batch_%s";
    private static final String CONTENT_TYPE = "Content-Type: application/http";
    private static final String CONTENT_TRANSFER_ENCODING = "Content-Transfer-Encoding: binary";
    private static final String CONTENT_ID_TEMPLATE = "Content-ID: %d";
    private static final String HTTP_VERSION = "HTTP/1.1";
    private static final String OPERATION_TEMPLATE = "%s %s %s";

    private final AtomicInteger contentId;
    private final BiConsumer<String, Integer> sendCallback;
    private final String batchBoundary;

    BatchClient(BiConsumer<String, Integer> sendCallback) {
        this.contentId = new AtomicInteger(0);
        this.sendCallback = sendCallback;
        this.batchBoundary = String.format(BATCH_BOUNDARY_TEMPLATE, UUID.randomUUID());
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request) {
        int id = contentId.getAndIncrement();

        StringBuilder batchRequestBuilder = new StringBuilder();
        batchRequestBuilder
            .append("--")
            .append(batchBoundary)
            .append(CONTENT_TYPE)
            .append(CONTENT_TRANSFER_ENCODING)
            .append(String.format(CONTENT_ID_TEMPLATE, id))
            .append('\n');

        String method = request.getHttpMethod().toString();
        String urlPath = request.getUrl().getPath();
        batchRequestBuilder.append(String.format(OPERATION_TEMPLATE, method, urlPath, HTTP_VERSION));

        request.getHeaders().stream().forEach(header -> batchRequestBuilder
            .append(String.format("%s: %s", header.getName(), header.getValue())));

        sendCallback.accept(batchRequestBuilder.toString(), id);
        return Mono.empty();
    }
}
