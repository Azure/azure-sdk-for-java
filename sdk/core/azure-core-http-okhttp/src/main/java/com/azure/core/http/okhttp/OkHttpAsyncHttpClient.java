// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.okhttp;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.okhttp.implementation.OkHttpAsyncBufferedResponse;
import com.azure.core.http.okhttp.implementation.OkHttpAsyncResponse;
import com.azure.core.http.okhttp.implementation.OkHttpFileRequestBody;
import com.azure.core.http.okhttp.implementation.OkHttpFluxRequestBody;
import com.azure.core.http.okhttp.implementation.OkHttpInputStreamRequestBody;
import com.azure.core.implementation.util.BinaryDataContent;
import com.azure.core.implementation.util.BinaryDataHelper;
import com.azure.core.implementation.util.ByteArrayContent;
import com.azure.core.implementation.util.FileContent;
import com.azure.core.implementation.util.InputStreamContent;
import com.azure.core.implementation.util.SerializableContent;
import com.azure.core.implementation.util.StringContent;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.io.IOException;
import java.util.Objects;

/**
 * HttpClient implementation for OkHttp.
 */
class OkHttpAsyncHttpClient implements HttpClient {

    private static final RequestBody EMPTY_REQUEST_BODY = RequestBody.create(new byte[0]);

    final OkHttpClient httpClient;

    OkHttpAsyncHttpClient(OkHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request) {
        return send(request, Context.NONE);
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request, Context context) {
        boolean eagerlyReadResponse = (boolean) context.getData("azure-eagerly-read-response").orElse(false);

        return Mono.create(sink -> sink.onRequest(value -> {
            // Using MonoSink::onRequest for back pressure support.

            // The blocking behavior toOkHttpRequest(r).subscribe call:
            //
            // The okhttp3.Request emitted by toOkHttpRequest(r) is chained from the body of request Flux<ByteBuffer>:
            //   1. If Flux<ByteBuffer> synchronous and send(r) caller does not apply subscribeOn then
            //      subscribe block on caller thread.
            //   2. If Flux<ByteBuffer> synchronous and send(r) caller apply subscribeOn then
            //      does not block caller thread but block on scheduler thread.
            //   3. If Flux<ByteBuffer> asynchronous then subscribe does not block caller thread
            //      but block on the thread backing flux. This ignore any subscribeOn applied to send(r)
            //
            Mono.fromCallable(() -> toOkHttpRequest(request)).subscribe(okHttpRequest -> {
                try {
                    Call call = httpClient.newCall(okHttpRequest);
                    call.enqueue(new OkHttpCallback(sink, request, eagerlyReadResponse));
                    sink.onCancel(call::cancel);
                } catch (Exception ex) {
                    sink.error(ex);
                }
            }, sink::error);
        }));
    }

    /**
     * Converts the given azure-core request to okhttp request.
     *
     * @param request the azure-core request
     * @return the okhttp request
     */
    private okhttp3.Request toOkHttpRequest(HttpRequest request) {
        Request.Builder requestBuilder = new Request.Builder()
            .url(request.getUrl());

        if (request.getHeaders() != null) {
            for (HttpHeader hdr : request.getHeaders()) {
                // OkHttp allows for headers with multiple values, but it treats them as separate headers,
                // therefore, we must call rb.addHeader for each value, using the same key for all of them
                hdr.getValuesList().forEach(value -> requestBuilder.addHeader(hdr.getName(), value));
            }
        }

        if (request.getHttpMethod() == HttpMethod.GET) {
            return requestBuilder.get().build();
        } else if (request.getHttpMethod() == HttpMethod.HEAD) {
            return requestBuilder.head().build();
        }

        RequestBody okHttpRequestBody = toOkHttpRequestBody(request.getBodyAsBinaryData(), request.getHeaders());
        return requestBuilder.method(request.getHttpMethod().toString(), okHttpRequestBody)
            .build();
    }

    /**
     * Create a Mono of okhttp3.RequestBody from the given BinaryData.
     *
     * @param bodyContent The request body content
     * @param headers the headers associated with the original request
     * @return the Mono emitting okhttp request
     */
    private RequestBody toOkHttpRequestBody(BinaryData bodyContent, HttpHeaders headers) {
        if (bodyContent == null) {
            return EMPTY_REQUEST_BODY;
        }

        String contentType = headers.getValue("Content-Type");
        MediaType mediaType = (contentType == null) ? null : MediaType.parse(contentType);

        BinaryDataContent content = BinaryDataHelper.getContent(bodyContent);

        if (content instanceof ByteArrayContent
            || content instanceof StringContent
            || content instanceof SerializableContent) {
            return RequestBody.create(content.toBytes(), mediaType);
        } else {
            long effectiveContentLength = getRequestContentLength(content, headers);
            if (content instanceof InputStreamContent) {
                // The OkHttpInputStreamRequestBody doesn't read bytes until it's triggered by OkHttp dispatcher.
                return new OkHttpInputStreamRequestBody(
                    (InputStreamContent) content, effectiveContentLength, mediaType);
            } else if (content instanceof FileContent) {
                // The OkHttpFileRequestBody doesn't read bytes until it's triggered by OkHttp dispatcher.
                return new OkHttpFileRequestBody((FileContent) content, effectiveContentLength, mediaType);
            } else {
                // The OkHttpFluxRequestBody doesn't read bytes until it's triggered by OkHttp dispatcher.
                return new OkHttpFluxRequestBody(
                    content, effectiveContentLength, mediaType, httpClient.callTimeoutMillis());
            }
        }
    }

    private static long getRequestContentLength(BinaryDataContent content, HttpHeaders headers) {
        Long contentLength = content.getLength();
        if (contentLength == null) {
            String contentLengthHeaderValue = headers.getValue("Content-Length");
            if (contentLengthHeaderValue != null) {
                contentLength = Long.parseLong(contentLengthHeaderValue);
            } else {
                // -1 means that content length is unknown.
                contentLength = -1L;
            }
        }
        return contentLength;
    }

    private static class OkHttpCallback implements okhttp3.Callback {
        private final MonoSink<HttpResponse> sink;
        private final HttpRequest request;
        private final boolean eagerlyReadResponse;

        OkHttpCallback(MonoSink<HttpResponse> sink, HttpRequest request, boolean eagerlyReadResponse) {
            this.sink = sink;
            this.request = request;
            this.eagerlyReadResponse = eagerlyReadResponse;
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public void onFailure(okhttp3.Call call, IOException e) {
            if (e.getSuppressed().length == 1) {
                // Propagate suppressed exception when there is one.
                // This happens when body emission fails in the middle.
                sink.error(e.getSuppressed()[0]);
            } else {
                sink.error(e);
            }
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public void onResponse(okhttp3.Call call, okhttp3.Response response) {
            /*
             * Use a buffered response when we are eagerly reading the response from the network and the body isn't
             * empty.
             */
            if (eagerlyReadResponse) {
                ResponseBody body = response.body();
                if (Objects.nonNull(body)) {
                    try {
                        byte[] bytes = body.bytes();
                        body.close();
                        sink.success(new OkHttpAsyncBufferedResponse(response, request, bytes));
                    } catch (IOException ex) {
                        // Reading the body bytes may cause an IOException, if it happens propagate it.
                        sink.error(ex);
                    }
                } else {
                    // Body is null, use the non-buffering response.
                    sink.success(new OkHttpAsyncResponse(response, request));
                }
            } else {
                sink.success(new OkHttpAsyncResponse(response, request));
            }
        }
    }
}
