// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.jdk.httpclient;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.implementation.util.BinaryDataContent;
import com.azure.core.implementation.util.BinaryDataHelper;
import com.azure.core.implementation.util.ByteArrayContent;
import com.azure.core.implementation.util.FileContent;
import com.azure.core.implementation.util.InputStreamContent;
import com.azure.core.implementation.util.StringContent;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import reactor.adapter.JdkFlowAdapter;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.http.HttpRequest.BodyPublisher;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Flow;

import static java.net.http.HttpRequest.BodyPublishers.fromPublisher;
import static java.net.http.HttpRequest.BodyPublishers.noBody;
import static java.net.http.HttpRequest.BodyPublishers.ofByteArray;
import static java.net.http.HttpRequest.BodyPublishers.ofFile;
import static java.net.http.HttpRequest.BodyPublishers.ofString;
import static java.net.http.HttpResponse.BodyHandlers.ofPublisher;

/**
 * HttpClient implementation for the JDK HttpClient.
 */
class JdkAsyncHttpClient implements HttpClient {
    private static final ClientLogger LOGGER = new ClientLogger(JdkAsyncHttpClient.class);

    private final java.net.http.HttpClient jdkHttpClient;

    private final Set<String> restrictedHeaders;

    JdkAsyncHttpClient(java.net.http.HttpClient httpClient, Set<String> restrictedHeaders) {
        this.jdkHttpClient = httpClient;
        // Since this library requires Java 11 to compile use Runtime.version().feature() which was added in Java 10
        // which gets the Java major version.
        if (Runtime.version().feature() <= 11) {
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
                    final BodyPublisher bodyPublisher = toBodyPublisher(request.getContent(), contentLength);
                    return builder.method(request.getHttpMethod().toString(), bodyPublisher).build();
            }
        });
    }

    /**
     * Create BodyPublisher from the given java.nio.ByteBuffer publisher.
     *
     * @param requestContent The {@link BinaryData} that represents the request content.
     * @return the request BodyPublisher
     */
    private static BodyPublisher toBodyPublisher(BinaryData requestContent, String contentLength)
        throws FileNotFoundException {
        if (requestContent == null) {
            return noBody();
        }

        BinaryDataContent binaryDataContent = BinaryDataHelper.getContent(requestContent);
        if (binaryDataContent instanceof ByteArrayContent) {
            return ofByteArray(binaryDataContent.toBytes());
        } else if (binaryDataContent instanceof FileContent) {
            FileContent fileContent = (FileContent) binaryDataContent;
            // This won't be right all the time as we may be sending only a partial view of the file.
            // TODO (alzimmer): support ranges in FileContent
            return ofFile(fileContent.getFile());
        } else if (binaryDataContent instanceof StringContent) {
            return ofString(binaryDataContent.toString());
        } else if (binaryDataContent instanceof InputStreamContent) {
            return java.net.http.HttpRequest.BodyPublishers.ofInputStream(binaryDataContent::toStream);
        } else {
            final Flow.Publisher<ByteBuffer> bbFlowPublisher = JdkFlowAdapter.publisherToFlowPublisher(binaryDataContent
                .toFluxByteBuffer());
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
