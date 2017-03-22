/**
 * The MIT License (MIT)
 * Copyright (c) 2016 Microsoft Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.microsoft.azure.documentdb.rx.internal;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.azure.documentdb.ConnectionPolicy;
import com.microsoft.azure.documentdb.ConsistencyLevel;
import com.microsoft.azure.documentdb.DocumentClientException;
import com.microsoft.azure.documentdb.Error;
import com.microsoft.azure.documentdb.internal.DocumentServiceResponse;
import com.microsoft.azure.documentdb.internal.EndpointManager;
import com.microsoft.azure.documentdb.internal.HttpConstants;
import com.microsoft.azure.documentdb.internal.OperationType;
import com.microsoft.azure.documentdb.internal.QueryCompatibilityMode;
import com.microsoft.azure.documentdb.internal.RuntimeConstants;
import com.microsoft.azure.documentdb.internal.UserAgentContainer;
import com.microsoft.azure.documentdb.internal.directconnectivity.StoreResponse;
import com.microsoft.azure.documentdb.rx.AsyncDocumentClient;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.netty.protocol.http.client.HttpClient;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import io.reactivex.netty.protocol.http.client.HttpClientResponse;
import io.reactivex.netty.protocol.http.client.HttpResponseHeaders;
import rx.Observable;
import rx.exceptions.Exceptions;

/**
 * Used internally to provide functionality to communicate and process response from Gateway in the Azure DocumentDB database service.
 */
class RxGatewayStoreModel implements RxStoreModel {

    private final Logger logger = LoggerFactory.getLogger(RxGatewayStoreModel.class);
    private final Map<String, String> defaultHeaders;
    private final HttpClient<ByteBuf, ByteBuf> httpClient;
    private final QueryCompatibilityMode queryCompatibilityMode;
    private final EndpointManager globalEndpointManager;

    public RxGatewayStoreModel(ConnectionPolicy connectionPolicy,
            ConsistencyLevel consistencyLevel,
            QueryCompatibilityMode queryCompatibilityMode,
            String masterKey,
            Map<String, String> resourceTokens,
            UserAgentContainer userAgentContainer,
            EndpointManager globalEndpointManager,
            HttpClient<ByteBuf, ByteBuf> httpClient) {
        this.defaultHeaders = new HashMap<String, String>();
        this.defaultHeaders.put(HttpConstants.HttpHeaders.CACHE_CONTROL,
                "no-cache");
        this.defaultHeaders.put(HttpConstants.HttpHeaders.VERSION,
                HttpConstants.Versions.CURRENT_VERSION);

        if (userAgentContainer == null) {
            userAgentContainer = new UserAgentContainer();
        }

        this.defaultHeaders.put(HttpConstants.HttpHeaders.USER_AGENT, userAgentContainer.getUserAgent());

        if (consistencyLevel != null) {
            this.defaultHeaders.put(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL,
                    consistencyLevel.toString());
        }

        this.globalEndpointManager = globalEndpointManager;
        this.queryCompatibilityMode = queryCompatibilityMode;

        this.httpClient = httpClient;
    }

    public Observable<DocumentServiceResponse> doCreate(RxDocumentServiceRequest request) {
        return this.performRequest(request, HttpMethod.POST);
    }

    public Observable<DocumentServiceResponse> doUpsert(RxDocumentServiceRequest request) {
        return this.performRequest(request, HttpMethod.POST);
    }

    public Observable<DocumentServiceResponse> doRead(RxDocumentServiceRequest request) {
        return this.performRequest(request, HttpMethod.GET);
    }

    public Observable<DocumentServiceResponse> doReplace(RxDocumentServiceRequest request) {
        return this.performRequest(request, HttpMethod.PUT);
    }

    public Observable<DocumentServiceResponse> doDelete(RxDocumentServiceRequest request) {
        return this.performRequest(request, HttpMethod.DELETE);
    }

    public Observable<DocumentServiceResponse> doExecute(RxDocumentServiceRequest request) {
        return this.performRequest(request, HttpMethod.POST);
    }

    public Observable<DocumentServiceResponse> doReadFeed(RxDocumentServiceRequest request) {
        return this.performRequest(request, HttpMethod.GET);
    }

    public Observable<DocumentServiceResponse> doQuery(RxDocumentServiceRequest request) {
        request.getHeaders().put(HttpConstants.HttpHeaders.IS_QUERY, "true");

        switch (this.queryCompatibilityMode) {
        case SqlQuery:
            request.getHeaders().put(HttpConstants.HttpHeaders.CONTENT_TYPE,
                    RuntimeConstants.MediaTypes.SQL);
            break;
        case Default:
        case Query:
        default:
            request.getHeaders().put(HttpConstants.HttpHeaders.CONTENT_TYPE,
                    RuntimeConstants.MediaTypes.QUERY_JSON);
            break;
        }
        return this.performRequest(request, HttpMethod.POST);
    }

    /**
     * Given the request it creates an observable which upon subscription issues HTTP call and emits one DocumentServiceResponse.
     * 
     * @param request 
     * @param method
     * @return Observable<DocumentServiceResponse> 
     */
    public Observable<DocumentServiceResponse> performRequest(RxDocumentServiceRequest request, HttpMethod method) {

        URI uri = getUri(request);

        HttpClientRequest<ByteBuf> httpRequest = HttpClientRequest.create(method, uri.toString());

        this.fillHttpRequestBaseWithHeaders(request.getHeaders(), httpRequest);
        try {

            if (request.getContentObservable() != null) {

                // TODO validate this
                // convert byte[] to ByteBuf
                // why not use Observable<byte[]> directly?
                Observable<ByteBuf> byteBufObservable = request.getContentObservable()
                        .map(bytes ->  Unpooled.wrappedBuffer(bytes));

                httpRequest.withContentSource(byteBufObservable);
            } else if (request.getContent() != null){
                httpRequest.withContent(request.getContent());
            }

        } catch (Exception e) {
            return Observable.error(e);
        }

        Observable<HttpClientResponse<ByteBuf>> clientResponseObservable = this.httpClient.submit(httpRequest);

        return toDocumentServiceResponse(clientResponseObservable, request);
    }

    private void fillHttpRequestBaseWithHeaders(Map<String, String> headers, HttpClientRequest<ByteBuf> req) {
        // Add default headers.
        for (Map.Entry<String, String> entry : this.defaultHeaders.entrySet()) {
            req.withHeader(entry.getKey(), entry.getValue());
        }
        // Add override headers.
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                req.withHeader(entry.getKey(), entry.getValue());
            }
        }
    }

    private URI getUri(RxDocumentServiceRequest request) {
        URI rootUri = request.getEndpointOverride();
        if (rootUri == null) {
            if (request.getIsMedia()) {
                // For media read request, always use the write endpoint.
                rootUri = this.globalEndpointManager.getWriteEndpoint();
            } else {
                rootUri = this.globalEndpointManager.resolveServiceEndpoint(request.getOperationType());
            }
        }
        URI uri;
        try {
            uri = new URI("https",
                    null,
                    rootUri.getHost(),
                    rootUri.getPort(),
                    request.getPath(),
                    null,  // Query string not used.
                    null);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Incorrect uri from request.", e);
        }

        return uri;
    }

    private StoreResponse fromHttpResponse(HttpResponseStatus httpResponseStatus,
            HttpResponseHeaders httpResponseHeaders, InputStream contentInputStream) throws IOException {

        List<Entry<String, String>> headerEntries = httpResponseHeaders.entries();

        String[] headers = new String[headerEntries.size()];
        String[] values = new String[headerEntries.size()];

        int i = 0;

        for(Entry<String, String> headerEntry: headerEntries) {
            headers[i] = headerEntry.getKey();
            values[i] = headerEntry.getValue();
            i++;
        }

        StoreResponse storeResponse = new StoreResponse(
                headers,
                values,
                httpResponseStatus.code(),
                contentInputStream);

        return storeResponse;
    }

    private Observable<InputStream> toInputStream(Observable<ByteBuf> contentObservable) {
        // TODO: this is a naive approach for converting to InputStream
        // this first reads and buffers everything in memory and then translate that to an input stream
        // this means 
        // 1) there is some performance implication
        // 2) this may result in OutOfMemoryException if used for reading huge content, e.g., a media
        //
        // see this: https://github.com/ReactiveX/RxNetty/issues/391 for some similar discussion on how to 
        // convert to an input stream
        return contentObservable
                .reduce(
                        new ByteArrayOutputStream(),
                        (out, bb) -> {
                            try {
                                bb.readBytes(out, bb.readableBytes());
                                return out;
                            }
                            catch (java.io.IOException e) {
                                throw new RuntimeException(e);
                            }
                        })
                .map(out -> {
                    return new ByteArrayInputStream(out.toByteArray());
                });
    }

    /**
     * Transforms the rxNetty's client response Observable to DocumentServiceResponse Observable.
     * 
     * 
     * Once the the customer code subscribes to the observable returned by the {@link AsyncDocumentClient} CRUD APIs,
     * the subscription goes up till it reaches the source rxNetty's observable, and at that point the HTTP invocation will be made.
     * 
     * @param clientResponseObservable
     * @param request
     * @return {@link Observable}
     */
    private Observable<DocumentServiceResponse> toDocumentServiceResponse(Observable<HttpClientResponse<ByteBuf>> clientResponseObservable, 
            RxDocumentServiceRequest request) {

        return clientResponseObservable.flatMap(clientResponse -> {
            
            // header key/value pairs
            HttpResponseHeaders httpResponseHeaders = clientResponse.getHeaders();
            HttpResponseStatus httpResponseStatus = clientResponse.getStatus();

            Observable<InputStream> inputStreamObservable;

            if (request.getOperationType() == OperationType.Delete) {
                // for delete we don't expect any body
                inputStreamObservable = Observable.just(null);
            } else {
                // transforms the observable<ByteBuf> to Observable<InputStream>
                inputStreamObservable = toInputStream(clientResponse.getContent());
            }

            Observable<StoreResponse> storeResponseObservable = inputStreamObservable
                    .map(contentInputStream -> {
                        try {
                            // If there is any error in the header response this throws exception
                            validateOrThrow(request, httpResponseStatus, httpResponseHeaders, contentInputStream);

                            // transforms to Observable<DocumentServiceResponse>
                            return fromHttpResponse(httpResponseStatus, httpResponseHeaders, contentInputStream);
                        } catch (RuntimeException e) {
                            throw e;
                        } catch (Exception e) {
                            // wrap the exception in runtime exception
                            throw Exceptions.propagate(e);
                        }
                    });

            return storeResponseObservable;

        }).map(storeResponse -> new DocumentServiceResponse(storeResponse));
    }

    private void validateOrThrow(RxDocumentServiceRequest request, HttpResponseStatus status, HttpResponseHeaders headers,
            InputStream inputStream) throws DocumentClientException {

        int statusCode = status.code();

        if (statusCode >= HttpConstants.StatusCodes.MINIMUM_STATUSCODE_AS_ERROR_GATEWAY) {
            String body = null;
            if (inputStream != null) {
                try {
                    body = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                } catch (ParseException | IOException e) {
                    logger.error("Failed to get content from the http response", e);
                    throw new IllegalStateException("Failed to get content from the http response", e);
                } finally {
                    IOUtils.closeQuietly(inputStream);
                }
            }

            Map<String, String> responseHeaders = new HashMap<String, String>();
            for (Entry<String, String> header : headers.entries()) {
                responseHeaders.put(header.getKey(), header.getValue());
            }

            String statusCodeString = status.reasonPhrase() != null
                    ? status.reasonPhrase().replace(" ", "")
                            : "";
                    Error error = null;
                    error = (body != null)? new Error(body): new Error();
                    error = new Error(statusCodeString,
                            String.format("%s, StatusCode: %s", error.getMessage(), statusCodeString),
                            error.getPartitionedQueryExecutionInfo());

                    throw new DocumentClientException(statusCode, error, responseHeaders);
        }
    }

    @Override
    public Observable<DocumentServiceResponse> processMessage(RxDocumentServiceRequest request)  {
        switch (request.getOperationType()) {
        case Create:
            return this.doCreate(request);
        case Upsert:
            return this.doUpsert(request);
        case Delete:
            return this.doDelete(request);
        case ExecuteJavaScript:
            return this.doExecute(request);
        case Read:
            return this.doRead(request);
        case ReadFeed:
            return this.doReadFeed(request);
        case Replace:
            return this.doReplace(request);
        case SqlQuery:
        case Query:
            return this.doQuery(request);
        default:
            throw new IllegalStateException("Unknown operation type " + request.getOperationType());
        }
    }
}
