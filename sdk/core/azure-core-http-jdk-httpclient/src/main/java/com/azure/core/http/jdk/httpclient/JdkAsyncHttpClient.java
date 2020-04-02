// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.jdk.httpclient;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import reactor.adapter.JdkFlowAdapter;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import java.net.http.HttpRequest.BodyPublisher;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Flow;

import static java.net.http.HttpResponse.BodyHandlers.ofPublisher;
import static java.net.http.HttpRequest.BodyPublishers.fromPublisher;
import static java.net.http.HttpRequest.BodyPublishers.noBody;

/**
 * HttpClient implementation for the JDK HttpClient.
 */
class JdkAsyncHttpClient implements HttpClient {
    private final ClientLogger logger = new ClientLogger(JdkAsyncHttpClient.class);
    private final java.net.http.HttpClient jdkHttpClient;
    private final int javaVersion;

    // These headers are restricted by default in native JDK12 HttpClient.
    // These headers can be whitelisted by setting jdk.httpclient.allowRestrictedHeaders
    // property in the network properties file: 'JAVA_HOME/conf/net.properties'
    // e.g white listing 'host' header.
    //
    // jdk.httpclient.allowRestrictedHeaders=host
    //
    private static final Set<String> JDK12_RESTRICTED_HEADERS;
    static {
        TreeSet<String> treeSet = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        treeSet.addAll(Set.of("connection",
            "content-length",
            "expect",
            "host",
            "upgrade"));
        JDK12_RESTRICTED_HEADERS = Collections.unmodifiableSet(treeSet);
    }

    JdkAsyncHttpClient(java.net.http.HttpClient httpClient) {
        this.jdkHttpClient = httpClient;
        this.javaVersion = getJavaVersion();
        if (javaVersion <= 11) {
            logger.logExceptionAsError(
                new RuntimeException("JdkAsyncHttpClient is not supported in Java version 11 and below."));
        }
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request) {
        return toJdkHttpRequest(request)
            .flatMap(jdkRequest -> Mono.fromCompletionStage(jdkHttpClient.sendAsync(jdkRequest, ofPublisher()))
                .map(innerResponse -> new JdkHttpResponse(request, innerResponse)));
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
                    if (!JDK12_RESTRICTED_HEADERS.contains(headerName)) {
                        final String headerValue = header.getValue();
                        builder.setHeader(headerName, headerValue);
                    } else {
                        logger.logExceptionAsError(
                            new IllegalArgumentException("The header "
                                + "'" + headerName
                                + "' is restricted by default in JDK HttpClient 12 and above."
                                + "(unless it is whitelisted in JAVA_HOME/conf/net.properties)"));
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
                throw logger.logExceptionAsError(new RuntimeException("Can't parse 'java.version':" + version));
            }
            try {
                return Integer.parseInt(version.substring(0, idx));
            } catch (Throwable t) {
                throw logger.logExceptionAsError(new RuntimeException("Can't parse 'java.version':" + version, t));
            }
        }
    }

    private static class JdkHttpResponse extends HttpResponse {
        private final int statusCode;
        private final HttpHeaders headers;
        private final Flux<ByteBuffer> contentFlux;
        private volatile boolean disposed = false;

        protected JdkHttpResponse(final HttpRequest request,
                                  java.net.http.HttpResponse<Flow.Publisher<List<ByteBuffer>>> innerResponse) {
            super(request);
            this.statusCode = innerResponse.statusCode();
            this.headers = fromJdkHttpHeaders(innerResponse.headers());
            this.contentFlux = JdkFlowAdapter.flowPublisherToFlux(innerResponse.body())
                .flatMapSequential(Flux::fromIterable);
        }

        @Override
        public int getStatusCode() {
            return this.statusCode;
        }

        @Override
        public String getHeaderValue(String name) {
            return this.headers.getValue(name);
        }

        @Override
        public HttpHeaders getHeaders() {
            return this.headers;
        }

        @Override
        public Flux<ByteBuffer> getBody() {
            return this.contentFlux
                .doFinally(signalType -> disposed = true);
        }

        @Override
        public Mono<byte[]> getBodyAsByteArray() {
            return FluxUtil.collectBytesInByteBufferStream(getBody())
                .flatMap(bytes -> bytes.length == 0 ? Mono.empty() : Mono.just(bytes));
        }

        @Override
        public Mono<String> getBodyAsString() {
            return getBodyAsByteArray()
                .map(bytes -> new String(bytes, StandardCharsets.UTF_8));
        }

        @Override
        public Mono<String> getBodyAsString(Charset charset) {
            return getBodyAsByteArray()
                .map(bytes -> new String(bytes, charset));
        }

        @Override
        public void close() {
            if (!this.disposed) {
                this.disposed = true;
                this.contentFlux
                    .subscribe()
                    .dispose();
            }
        }

        /**
         * Converts the given JDK Http headers to azure-core Http header.
         *
         * @param headers the JDK Http headers
         * @return the azure-core Http headers
         */
        private static HttpHeaders fromJdkHttpHeaders(java.net.http.HttpHeaders headers) {
            final HttpHeaders httpHeaders = new HttpHeaders();
            for (final String key : headers.map().keySet()) {
                final List<String> values = headers.allValues(key);
                if (values == null || values.size() == 0) {
                    continue;
                } else if (values.size() == 1) {
                    httpHeaders.put(key, values.get(0));
                } else {
                    httpHeaders.put(key, String.join(",", values));
                }
            }
            return httpHeaders;
        }
    }
}
