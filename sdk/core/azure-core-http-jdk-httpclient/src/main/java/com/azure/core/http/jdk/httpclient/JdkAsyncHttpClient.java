// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.jdk.httpclient;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import reactor.adapter.JdkFlowAdapter;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URISyntaxException;
import java.net.http.HttpRequest.BodyPublisher;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Flow;

import static java.net.http.HttpRequest.BodyPublishers.fromPublisher;
import static java.net.http.HttpRequest.BodyPublishers.noBody;
import static java.net.http.HttpResponse.BodyHandlers.ofPublisher;

/**
 * HttpClient implementation for the JDK HttpClient.
 */
class JdkAsyncHttpClient implements HttpClient {
    private final ClientLogger logger = new ClientLogger(JdkAsyncHttpClient.class);

    private final java.net.http.HttpClient jdkHttpClient;

    private final Set<String> restrictedHeaders;

    JdkAsyncHttpClient(java.net.http.HttpClient httpClient, Set<String> restrictedHeaders) {
        this.jdkHttpClient = httpClient;
        int javaVersion = getJavaVersion();
        if (javaVersion <= 11) {
            throw logger.logExceptionAsError(
                new UnsupportedOperationException("JdkAsyncHttpClient is not supported in Java version 11 and below."));
        }

        this.restrictedHeaders = restrictedHeaders;
        logger.verbose("Effective restricted headers: {}", restrictedHeaders);
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request) {
        return send(request, Context.NONE);
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request, Context context) {
        boolean eagerlyReadResponse = (boolean) context.getData("azure-eagerly-read-response").orElse(false);

        return toJdkHttpRequest(request)
            .flatMap(jdkRequest -> Mono.fromCompletionStage(jdkHttpClient.sendAsync(jdkRequest, ofPublisher()))
                .flatMap(innerResponse -> {
                    if (eagerlyReadResponse) {
                        int statusCode = innerResponse.statusCode();
                        HttpHeaders headers = fromJdkHttpHeaders(innerResponse.headers());

                        return FluxUtil.collectBytesFromNetworkResponse(JdkFlowAdapter
                            .flowPublisherToFlux(innerResponse.body())
                            .flatMapSequential(Flux::fromIterable), headers)
                            .map(bytes -> new BufferedJdkHttpResponse(request, statusCode, headers, bytes));
                    } else {
                        return Mono.just(new JdkHttpResponse(request, innerResponse));
                    }
                }));
    }

    /**
     * Converts the given azure-core request to the JDK HttpRequest type.
     *
     * @param request the azure-core request
     * @return the Mono emitting HttpRequest
     */
    private Mono<java.net.http.HttpRequest> toJdkHttpRequest(HttpRequest request) {
        return Mono.fromCallable(() -> {
            final java.net.http.HttpRequest.Builder builder = java.net.http.HttpRequest.newBuilder();
            try {
                builder.uri(request.getUrl().toURI());
            } catch (URISyntaxException e) {
                throw logger.logExceptionAsError(Exceptions.propagate(e));
            }
            final HttpHeaders headers = request.getHeaders();
            if (headers != null) {
                for (HttpHeader header : headers) {
                    final String headerName = header.getName();
                    if (!restrictedHeaders.contains(headerName)) {
                        final String headerValue = header.getValue();
                        builder.setHeader(headerName, headerValue);
                    } else {
                        logger.warning("The header '" + headerName + "' is restricted by default in JDK HttpClient 12 "
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
                    final BodyPublisher bodyPublisher = toBodyPublisher(request.getBody(), contentLength);
                    return builder.method(request.getHttpMethod().toString(), bodyPublisher).build();
            }
        });
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
                return fromPublisher(bbFlowPublisher);
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
            throw logger.logExceptionAsError(new RuntimeException("Can't find 'java.version' system property."));
        }
        if (version.startsWith("1.")) {
            if (version.length() < 3) {
                throw logger.logExceptionAsError(new RuntimeException("Can't parse 'java.version':" + version));
            }
            try {
                return Integer.parseInt(version.substring(2, 3));
            } catch (Throwable t) {
                throw logger.logExceptionAsError(new RuntimeException("Can't parse 'java.version':" + version, t));
            }
        } else {
            int idx = version.indexOf(".");

            if (idx == -1) {
                return Integer.parseInt(version);
            }
            try {
                return Integer.parseInt(version.substring(0, idx));
            } catch (Throwable t) {
                throw logger.logExceptionAsError(new RuntimeException("Can't parse 'java.version':" + version, t));
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

        for (final String key : headers.map().keySet()) {
            final List<String> values = headers.allValues(key);
            if (CoreUtils.isNullOrEmpty(values)) {
                continue;
            }

            httpHeaders.set(key, values);
        }

        return httpHeaders;
    }
}
