// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.okhttp;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.ProxyOptions;
import com.azure.core.http.okhttp.implementation.BinaryDataRequestBody;
import com.azure.core.http.okhttp.implementation.OkHttpAsyncBufferedResponse;
import com.azure.core.http.okhttp.implementation.OkHttpAsyncResponse;
import com.azure.core.http.okhttp.implementation.OkHttpFluxRequestBody;
import com.azure.core.http.okhttp.implementation.OkHttpProgressReportingRequestBody;
import com.azure.core.http.okhttp.implementation.PerCallTimeoutCall;
import com.azure.core.http.okhttp.implementation.ResponseTimeoutListenerFactory;
import com.azure.core.implementation.util.BinaryDataContent;
import com.azure.core.implementation.util.BinaryDataHelper;
import com.azure.core.implementation.util.FluxByteBufferContent;
import com.azure.core.implementation.util.HttpUtils;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.Contexts;
import com.azure.core.util.ProgressReporter;
import com.azure.core.util.logging.ClientLogger;
import okhttp3.Call;
import okhttp3.EventListener;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.UncheckedIOException;
import java.time.Duration;

/**
 * This class provides a OkHttp-based implementation for the {@link HttpClient} interface. Creating an instance of this
 * class can be achieved by using the {@link OkHttpAsyncHttpClientBuilder} class, which offers OkHttp-specific API for
 * features such as {@link OkHttpAsyncHttpClientBuilder#proxy(ProxyOptions) setProxy configuration}, and much more.
 *
 * <p>
 * <strong>Sample: Construct OkHttpAsyncHttpClient with Default Configuration</strong>
 * </p>
 *
 * <p>
 * The following code sample demonstrates the creation of a OkHttp HttpClient that uses port 80 and has no proxy.
 * </p>
 *
 * <!-- src_embed com.azure.core.http.okhttp.instantiation-simple -->
 * <pre>
 * HttpClient client = new OkHttpAsyncHttpClientBuilder&#40;&#41;
 *         .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.core.http.okhttp.instantiation-simple -->
 *
 * <p>
 * For more ways to instantiate OkHttpAsyncHttpClient, refer to {@link OkHttpAsyncHttpClientBuilder}.
 * </p>
 *
 * @see com.azure.core.http.okhttp
 * @see OkHttpAsyncHttpClientBuilder
 * @see HttpClient
 */
class OkHttpAsyncHttpClient implements HttpClient {

    private static final ClientLogger LOGGER = new ClientLogger(OkHttpAsyncHttpClient.class);
    private static final byte[] EMPTY_BODY = new byte[0];
    private static final RequestBody EMPTY_REQUEST_BODY = RequestBody.create(EMPTY_BODY);

    final OkHttpClient httpClient;

    private final Duration responseTimeout;

    OkHttpAsyncHttpClient(OkHttpClient httpClient, Duration responseTimeout) {
        EventListener.Factory factory = httpClient.eventListenerFactory();
        this.httpClient
            = httpClient.newBuilder().eventListenerFactory(new ResponseTimeoutListenerFactory(factory)).build();
        this.responseTimeout = responseTimeout;
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request) {
        return send(request, Context.NONE);
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request, Context context) {
        boolean eagerlyReadResponse = (boolean) context.getData(HttpUtils.AZURE_EAGERLY_READ_RESPONSE).orElse(false);
        boolean ignoreResponseBody = (boolean) context.getData(HttpUtils.AZURE_IGNORE_RESPONSE_BODY).orElse(false);
        boolean eagerlyConvertHeaders
            = (boolean) context.getData(HttpUtils.AZURE_EAGERLY_CONVERT_HEADERS).orElse(false);
        Duration perCallTimeout = (Duration) context.getData(HttpUtils.AZURE_RESPONSE_TIMEOUT)
            .filter(timeoutDuration -> timeoutDuration instanceof Duration)
            .orElse(responseTimeout);

        ProgressReporter progressReporter = Contexts.with(context).getHttpRequestProgressReporter();

        return Mono.create(sink -> sink.onRequest(value -> {
            // Using MonoSink::onRequest for back pressure support.

            // The blocking behavior toOkHttpRequest(r).subscribe call:
            //
            // The okhttp3.Request emitted by toOkHttpRequest(r) is chained from the body of request Flux<ByteBuffer>:
            // 1. If Flux<ByteBuffer> synchronous and send(r) caller does not apply subscribeOn then
            // subscribe block on caller thread.
            // 2. If Flux<ByteBuffer> synchronous and send(r) caller apply subscribeOn then
            // does not block caller thread but block on scheduler thread.
            // 3. If Flux<ByteBuffer> asynchronous then subscribe does not block caller thread
            // but block on the thread backing flux. This ignore any subscribeOn applied to send(r)
            //
            Mono.fromCallable(() -> toOkHttpRequest(request, progressReporter, perCallTimeout))
                .subscribe(okHttpRequest -> {
                    try {
                        Call call = httpClient.newCall(okHttpRequest);
                        call.enqueue(new OkHttpCallback(sink, request, eagerlyReadResponse, ignoreResponseBody,
                            eagerlyConvertHeaders));
                        sink.onCancel(call::cancel);
                    } catch (Exception ex) {
                        sink.error(ex);
                    }
                }, sink::error);
        }));
    }

    @Override
    public HttpResponse sendSync(HttpRequest request, Context context) {
        boolean eagerlyReadResponse = (boolean) context.getData(HttpUtils.AZURE_EAGERLY_READ_RESPONSE).orElse(false);
        boolean ignoreResponseBody = (boolean) context.getData(HttpUtils.AZURE_IGNORE_RESPONSE_BODY).orElse(false);
        boolean eagerlyConvertHeaders
            = (boolean) context.getData(HttpUtils.AZURE_EAGERLY_CONVERT_HEADERS).orElse(false);
        Duration perCallTimeout = (Duration) context.getData(HttpUtils.AZURE_RESPONSE_TIMEOUT)
            .filter(timeoutDuration -> timeoutDuration instanceof Duration)
            .orElse(responseTimeout);

        ProgressReporter progressReporter = Contexts.with(context).getHttpRequestProgressReporter();

        Request okHttpRequest = toOkHttpRequest(request, progressReporter, perCallTimeout);
        Call call = null;
        try {
            call = httpClient.newCall(okHttpRequest);
            Response okHttpResponse = call.execute();
            return toHttpResponse(request, okHttpResponse, eagerlyReadResponse, ignoreResponseBody,
                eagerlyConvertHeaders);
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(mapIOException(e, call)));
        }
    }

    /**
     * Current design for response timeout uses call cancellation which throws an IOException with message "canceled".
     * This isn't what we want, we want an InterruptedIOException with message "timeout". Use information stored on the
     * call to determine if the IOException should be mapped to an InterruptedIOException.
     *
     * @param e the IOException to map
     * @param call the Call to associate with the new IOException
     * @return the new IOException
     */
    private static IOException mapIOException(IOException e, Call call) {
        if (call == null) {
            return e;
        }

        PerCallTimeoutCall perCallTimeoutCall = call.request().tag(PerCallTimeoutCall.class);
        if (perCallTimeoutCall != null && perCallTimeoutCall.isTimedOut()) {
            InterruptedIOException i = new InterruptedIOException("timedout");
            i.addSuppressed(e);
            return i;
        }

        return e;
    }

    /**
     * Converts the given azure-core request to okhttp request.
     *
     * @param request the azure-core request
     * @param progressReporter the {@link ProgressReporter}. Can be null.
     * @param perCallTimeout the per call timeout
     * @return the okhttp request
     */
    private okhttp3.Request toOkHttpRequest(HttpRequest request, ProgressReporter progressReporter,
        Duration perCallTimeout) {
        Request.Builder requestBuilder = new Request.Builder().url(request.getUrl());

        if (perCallTimeout != null) {
            requestBuilder.tag(PerCallTimeoutCall.class, new PerCallTimeoutCall(perCallTimeout.toMillis()));
        }

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
        if (progressReporter != null) {
            okHttpRequestBody = new OkHttpProgressReportingRequestBody(okHttpRequestBody, progressReporter);
        }
        return requestBuilder.method(request.getHttpMethod().toString(), okHttpRequestBody).build();
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

        String contentType = headers.getValue(HttpHeaderName.CONTENT_TYPE);
        MediaType mediaType = (contentType == null) ? null : MediaType.parse(contentType);

        BinaryDataContent content = BinaryDataHelper.getContent(bodyContent);

        long effectiveContentLength = getRequestContentLength(content, headers);
        if (content instanceof FluxByteBufferContent) {
            // The OkHttpFluxRequestBody doesn't read bytes until it's triggered by OkHttp dispatcher.
            // TODO (alzimmer): Is this still required? Specifically find out if the timeout is needed.
            return new OkHttpFluxRequestBody(content, effectiveContentLength, mediaType,
                httpClient.callTimeoutMillis());
        } else {
            // Default is to use a generic BinaryData RequestBody.
            return new BinaryDataRequestBody(bodyContent, mediaType, effectiveContentLength);
        }
    }

    private static long getRequestContentLength(BinaryDataContent content, HttpHeaders headers) {
        Long contentLength = content.getLength();
        if (contentLength == null) {
            String contentLengthHeaderValue = headers.getValue(HttpHeaderName.CONTENT_LENGTH);
            if (contentLengthHeaderValue != null) {
                contentLength = Long.parseLong(contentLengthHeaderValue);
            } else {
                // -1 means that content length is unknown.
                contentLength = -1L;
            }
        }
        return contentLength;
    }

    private static HttpResponse toHttpResponse(HttpRequest request, okhttp3.Response response,
        boolean eagerlyReadResponse, boolean ignoreResponseBody, boolean eagerlyConvertHeaders) throws IOException {
        // For now, eagerlyReadResponse and ignoreResponseBody works the same.
        // if (ignoreResponseBody) {
        // ResponseBody body = response.body();
        // if (body != null) {
        // if (body.contentLength() > 0) {
        // LOGGER.log(LogLevel.WARNING, () -> "Received HTTP response body when one wasn't expected. "
        // + "Response body will be ignored as directed.");
        // }
        // body.close();
        // }
        //
        // return new OkHttpAsyncBufferedResponse(response, request, EMPTY_BODY, eagerlyConvertHeaders);
        // }

        /*
         * Use a buffered response when we are eagerly reading the response from the network and the body isn't
         * empty.
         */
        if (eagerlyReadResponse || ignoreResponseBody) {
            try (ResponseBody body = response.body()) {
                byte[] bytes = (body != null) ? body.bytes() : EMPTY_BODY;
                return new OkHttpAsyncBufferedResponse(response, request, bytes, eagerlyConvertHeaders);
            }
        } else {
            return new OkHttpAsyncResponse(response, request, eagerlyConvertHeaders);
        }
    }

    private static class OkHttpCallback implements okhttp3.Callback {
        private final MonoSink<HttpResponse> sink;
        private final HttpRequest request;
        private final boolean eagerlyReadResponse;
        private final boolean ignoreResponseBody;
        private final boolean eagerlyConvertHeaders;

        OkHttpCallback(MonoSink<HttpResponse> sink, HttpRequest request, boolean eagerlyReadResponse,
            boolean ignoreResponseBody, boolean eagerlyConvertHeaders) {
            this.sink = sink;
            this.request = request;
            this.eagerlyReadResponse = eagerlyReadResponse;
            this.ignoreResponseBody = ignoreResponseBody;
            this.eagerlyConvertHeaders = eagerlyConvertHeaders;
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public void onFailure(okhttp3.Call call, IOException e) {
            if (e.getSuppressed().length == 1) {
                // Propagate suppressed exception when there is one.
                // This happens when body emission fails in the middle.
                Throwable suppressed = e.getSuppressed()[0];
                if (suppressed instanceof IOException) {
                    sink.error(mapIOException((IOException) suppressed, call));
                } else {
                    sink.error(suppressed);
                }
            } else {
                sink.error(mapIOException(e, call));
            }
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public void onResponse(okhttp3.Call call, okhttp3.Response response) {
            try {
                sink.success(
                    toHttpResponse(request, response, eagerlyReadResponse, ignoreResponseBody, eagerlyConvertHeaders));
            } catch (IOException ex) {
                // Reading the body bytes may cause an IOException, if it happens propagate it.
                sink.error(ex);
            }
        }
    }
}
