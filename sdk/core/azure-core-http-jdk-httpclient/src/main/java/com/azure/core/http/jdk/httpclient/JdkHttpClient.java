// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.jdk.httpclient;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.Context;
import com.azure.core.util.Contexts;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.ProgressReporter;
import com.azure.core.util.logging.ClientLogger;
import reactor.adapter.JdkFlowAdapter;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.net.http.HttpRequest.BodyPublisher;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Flow;

import static java.net.http.HttpRequest.BodyPublishers.fromPublisher;
import static java.net.http.HttpRequest.BodyPublishers.noBody;
import static java.net.http.HttpResponse.BodyHandlers.ofInputStream;
import static java.net.http.HttpResponse.BodyHandlers.ofPublisher;

/**
 * HttpClient implementation for the JDK HttpClient.
 */
class JdkHttpClient implements HttpClient {
    private static final ClientLogger LOGGER = new ClientLogger(JdkHttpClient.class);

    private final java.net.http.HttpClient jdkHttpClient;

    private final Set<String> restrictedHeaders;

    JdkHttpClient(java.net.http.HttpClient httpClient, Set<String> restrictedHeaders) {
        this.jdkHttpClient = httpClient;
        int javaVersion = getJavaVersion();
        if (javaVersion <= 11) {
            throw LOGGER.logExceptionAsError(
                new UnsupportedOperationException("JdkAsyncHttpClient is not supported in Java version 11 and below."));
        }

        this.restrictedHeaders = restrictedHeaders;
        LOGGER.verbose("Effective restricted headers: {}", restrictedHeaders);
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request) {
        return send(request, Context.NONE);
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request, Context context) {
        boolean eagerlyReadResponse = (boolean) context.getData("azure-eagerly-read-response").orElse(false);

        return Mono.fromCallable(() -> toJdkHttpRequest(request, context))
            .flatMap(jdkRequest -> Mono.fromCompletionStage(jdkHttpClient.sendAsync(jdkRequest, ofPublisher()))
                .flatMap(jdKResponse -> {
                    if (eagerlyReadResponse) {
                        HttpHeaders headers = fromJdkHttpHeaders(jdKResponse.headers());
                        int statusCode = jdKResponse.statusCode();

                        return FluxUtil.collectBytesFromNetworkResponse(JdkFlowAdapter
                            .flowPublisherToFlux(jdKResponse.body())
                            .flatMapSequential(Flux::fromIterable), headers)
                            .map(bytes -> new JdkSyncHttpResponse(request, statusCode, headers, bytes));
                    }

                    return Mono.just(new JdkAsyncHttpResponse(request, jdKResponse));
                }));
    }

    @Override
    public HttpResponse sendSync(HttpRequest request, Context context) {
        boolean eagerlyReadResponse = (boolean) context.getData("azure-eagerly-read-response").orElse(false);

        java.net.http.HttpRequest jdkRequest = toJdkHttpRequest(request, context);
        try {
            java.net.http.HttpResponse<InputStream> jdKResponse = jdkHttpClient.send(jdkRequest, ofInputStream());
            JdkSyncHttpResponse response = new JdkSyncHttpResponse(request, jdKResponse);
            if (eagerlyReadResponse) {
                response.buffer();
            }

            return response;
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(e));
        } catch (InterruptedException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
    }

    /**
     * Converts the given azure-core request to the JDK HttpRequest type.
     *
     * @param request the azure-core request
     * @return the HttpRequest
     */
    private java.net.http.HttpRequest toJdkHttpRequest(HttpRequest request, Context context) {
        ProgressReporter progressReporter = Contexts.with(context).getHttpRequestProgressReporter();

        final java.net.http.HttpRequest.Builder builder = java.net.http.HttpRequest.newBuilder();
        try {
            builder.uri(request.getUrl().toURI());
        } catch (URISyntaxException e) {
            throw LOGGER.logExceptionAsError(Exceptions.propagate(e));
        }
        final HttpHeaders headers = request.getHeaders();
        if (headers != null) {
            for (HttpHeader header : headers) {
                final String headerName = header.getName();
                if (!restrictedHeaders.contains(headerName)) {
                    header.getValuesList().forEach(headerValue -> builder.header(headerName, headerValue));
                } else {
                    LOGGER.warning("The header '" + headerName + "' is restricted by default in JDK HttpClient 12 "
                        + "and above. This header can be added to allow list in JAVA_HOME/conf/net.properties "
                        + "or in System.setProperty() or in Configuration. Use the key 'jdk.httpclient"
                        + ".allowRestrictedHeaders' and a comma separated list of header names.");
                }
            }
        }
        switch (request.getHttpMethod()) {
            case GET:
                return builder.GET().build();
            case HEAD:
                return builder.method("HEAD", noBody()).build();
            default:
                final String contentLength = request.getHeaders().getValue("content-length");
                Flux<ByteBuffer> body = request.getBody();
                if (progressReporter != null) {
                    body = body.map(buffer -> {
                        progressReporter.reportProgress(buffer.remaining());
                        return buffer;
                    });
                }
                final BodyPublisher bodyPublisher = toBodyPublisher(body, contentLength);
                return builder.method(request.getHttpMethod().toString(), bodyPublisher).build();
        }
    }

    /**
     * Create BodyPublisher from the given java.nio.ByteBuffer publisher.
     *
     * @param bbPublisher stream of java.nio.ByteBuffer representing request content
     * @return the request BodyPublisher
     */
    private static BodyPublisher toBodyPublisher(Flux<ByteBuffer> bbPublisher, String contentLength) {
        if (bbPublisher == null) {
            return noBody();
        }
        final Flow.Publisher<ByteBuffer> bbFlowPublisher = JdkFlowAdapter.publisherToFlowPublisher(bbPublisher);
        if (CoreUtils.isNullOrEmpty(contentLength)) {
            return fromPublisher(bbFlowPublisher);
        } else {
            long contentLengthLong = Long.parseLong(contentLength);
            if (contentLengthLong < 1) {
                return noBody();
            } else {
                return fromPublisher(bbFlowPublisher, contentLengthLong);
            }
        }
    }

    /**
     * Get the java runtime major version.
     *
     * @return the java major version
     */
    private int getJavaVersion() {
        // java.version format:
        // 8 and lower: 1.7, 1.8.0
        // 9 and above: 12, 14.1.1
        String version = System.getProperty("java.version");
        if (CoreUtils.isNullOrEmpty(version)) {
            throw LOGGER.logExceptionAsError(new RuntimeException("Can't find 'java.version' system property."));
        }
        if (version.startsWith("1.")) {
            if (version.length() < 3) {
                throw LOGGER.logExceptionAsError(new RuntimeException("Can't parse 'java.version':" + version));
            }
            try {
                return Integer.parseInt(version.substring(2, 3));
            } catch (Exception t) {
                throw LOGGER.logExceptionAsError(new RuntimeException("Can't parse 'java.version':" + version, t));
            }
        } else {
            int idx = version.indexOf(".");

            if (idx == -1) {
                return Integer.parseInt(version);
            }
            try {
                return Integer.parseInt(version.substring(0, idx));
            } catch (Exception t) {
                throw LOGGER.logExceptionAsError(new RuntimeException("Can't parse 'java.version':" + version, t));
            }
        }
    }

    /**
     * Converts the given JDK Http headers to azure-core Http header.
     *
     * @param headers the JDK Http headers
     * @return the azure-core Http headers
     */
    static HttpHeaders fromJdkHttpHeaders(java.net.http.HttpHeaders headers) {
        final HttpHeaders httpHeaders = new HttpHeaders();

        for (Map.Entry<String, List<String>> kvp : headers.map().entrySet()) {
            if (CoreUtils.isNullOrEmpty(kvp.getValue())) {
                continue;
            }

            httpHeaders.set(kvp.getKey(), kvp.getValue());
        }

        return httpHeaders;
    }
}
