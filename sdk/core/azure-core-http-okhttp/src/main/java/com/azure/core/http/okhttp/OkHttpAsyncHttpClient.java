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
import com.azure.core.implementation.util.BinaryDataContent;
import com.azure.core.implementation.util.BinaryDataHelper;
import com.azure.core.implementation.util.ByteArrayContent;
import com.azure.core.implementation.util.FileContent;
import com.azure.core.implementation.util.InputStreamContent;
import com.azure.core.implementation.util.StringContent;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.StreamUtils;
import com.azure.core.util.logging.ClientLogger;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.ByteString;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * HttpClient implementation for OkHttp.
 */
class OkHttpAsyncHttpClient implements HttpClient {

    private static final ClientLogger LOGGER = new ClientLogger(OkHttpAsyncHttpClient.class);

    final OkHttpClient httpClient;
    //
    private static final Mono<okio.ByteString> EMPTY_BYTE_STRING_MONO = Mono.just(okio.ByteString.EMPTY);

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
            toOkHttpRequest(request).subscribe(okHttpRequest -> {
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

    @Override
    public HttpResponse sendSynchronously(HttpRequest request, Context context) {
        boolean eagerlyReadResponse = (boolean) context.getData("azure-eagerly-read-response").orElse(false);

        Request okHttpRequest = toOkHttpRequestSynchronously(request);
        Call call = httpClient.newCall(okHttpRequest);
        try {
            Response okHttpResponse = call.execute();
            return fromOkHttpResponse(okHttpResponse, request, eagerlyReadResponse);
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(e));
        }
    }

    /**
     * Converts the given azure-core request to okhttp request.
     *
     * @param request the azure-core request
     * @return the Mono emitting okhttp request
     */
    private static Mono<okhttp3.Request> toOkHttpRequest(HttpRequest request) {
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
            return Mono.just(requestBuilder.get().build());
        } else if (request.getHttpMethod() == HttpMethod.HEAD) {
            return Mono.just(requestBuilder.head().build());
        }

        return toOkHttpRequestBody(request.getContent(), request.getHeaders())
            .map(okhttpRequestBody -> requestBuilder.method(request.getHttpMethod().toString(), okhttpRequestBody)
                .build());
    }

    /**
     * Converts the given azure-core request to okhttp request.
     *
     * @param request the azure-core request
     * @return the Mono emitting okhttp request
     */
    private static okhttp3.Request toOkHttpRequestSynchronously(HttpRequest request) {
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

        RequestBody requestBody = toOkHttpRequestBodySynchronously(request.getContent(), request.getHeaders());
        return requestBuilder.method(request.getHttpMethod().toString(), requestBody)
            .build();
    }

    /**
     * Create a Mono of okhttp3.RequestBody from the given java.nio.ByteBuffer Flux.
     *
     * @param bodyContent The BinaryData request body
     * @param headers the headers associated with the original request
     * @return the Mono emitting okhttp3.RequestBody
     */
    private static RequestBody toOkHttpRequestBodySynchronously(BinaryData bodyContent, HttpHeaders headers) {
        String contentType = headers.getValue("Content-Type");
        MediaType mediaType = (contentType == null) ? null : MediaType.parse(contentType);


        if (bodyContent == null) {
            return RequestBody.create(ByteString.EMPTY, mediaType);
        }

        BinaryDataContent content = BinaryDataHelper.getContent(bodyContent);
        if (content instanceof ByteArrayContent || content instanceof StringContent) {
            return RequestBody.create(content.toBytes(), mediaType);
        } else if (content instanceof FileContent) {
            FileContent fileContent = (FileContent) content;
            // This won't be right all the time as we may be sending only a partial view of the file.
            // TODO (alzimmer): support ranges in FileContent
            return RequestBody.create(fileContent.getFile().toFile(), mediaType);
        } else if (content instanceof InputStreamContent) {
            return RequestBody.create(toByteString(content.toStream()), mediaType);
        } else {
            // TODO (kasobol-msft) is there better way than just block? perhaps throw?
            // Perhaps we could consider using one of storage's stream implementation on top of flux?
            // Or maybe implement that OkHttp sink and get rid off reading to string altogether.
            return toByteString(bodyContent.toFluxByteBuffer()).map(bs -> RequestBody.create(bs, mediaType)).block();
        }
    }

    /**
     * Create a Mono of okhttp3.RequestBody from the given java.nio.ByteBuffer Flux.
     *
     * @param bodyContent The BinaryData request body
     * @param headers the headers associated with the original request
     * @return the Mono emitting okhttp3.RequestBody
     */
    private static Mono<RequestBody> toOkHttpRequestBody(BinaryData bodyContent, HttpHeaders headers) {
        String contentType = headers.getValue("Content-Type");
        MediaType mediaType = (contentType == null) ? null : MediaType.parse(contentType);


        if (bodyContent == null) {
            return Mono.defer(() -> Mono.just(RequestBody.create(ByteString.EMPTY, mediaType)));
        }

        BinaryDataContent content = BinaryDataHelper.getContent(bodyContent);
        if (content instanceof ByteArrayContent) {
            return Mono.defer(() -> Mono.just(RequestBody.create(content.toBytes(), mediaType)));
        } else if (content instanceof FileContent) {
            FileContent fileContent = (FileContent) content;
            // This won't be right all the time as we may be sending only a partial view of the file.
            // TODO (alzimmer): support ranges in FileContent
            return Mono.defer(() -> Mono.just(RequestBody.create(fileContent.getFile().toFile(), mediaType)));
        } else if (content instanceof StringContent) {
            return Mono.defer(() -> Mono.just(RequestBody.create(bodyContent.toString(), mediaType)));
        } else {
            return toByteString(bodyContent.toFluxByteBuffer()).map(bs -> RequestBody.create(bs, mediaType));
        }
    }

    /**
     * Aggregate Flux of java.nio.ByteBuffer to single okio.ByteString.
     *
     * Pooled okio.Buffer type is used to buffer emitted ByteBuffer instances. Content of each ByteBuffer will be
     * written (i.e copied) to the internal okio.Buffer slots. Once the stream terminates, the contents of all slots get
     * copied to one single byte array and okio.ByteString will be created referring this byte array. Finally, the
     * initial okio.Buffer will be returned to the pool.
     *
     * @param bbFlux the Flux of ByteBuffer to aggregate
     * @return a mono emitting aggregated ByteString
     */
    private static Mono<ByteString> toByteString(Flux<ByteBuffer> bbFlux) {
        Objects.requireNonNull(bbFlux, "'bbFlux' cannot be null.");
        return Mono.using(okio.Buffer::new,
            buffer -> bbFlux.reduce(buffer, (b, byteBuffer) -> {
                try {
                    b.write(byteBuffer);
                    return b;
                } catch (IOException ioe) {
                    throw Exceptions.propagate(ioe);
                }
            }).map(b -> ByteString.of(b.readByteArray())), okio.Buffer::clear)
            .switchIfEmpty(Mono.defer(() -> EMPTY_BYTE_STRING_MONO));
    }

    /**
     * Aggregate InputStream to single okio.ByteString.
     *
     * @param inputStream the InputStream to aggregate
     * @return Aggregated ByteString
     */
    private static ByteString toByteString(InputStream inputStream) {
        try (InputStream closeableInputStream = inputStream) {
            byte[] content = StreamUtils.INSTANCE.readAllBytes(closeableInputStream);
            return ByteString.of(content);
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(e));
        }
    }

    private static HttpResponse fromOkHttpResponse(
        okhttp3.Response response, HttpRequest request, boolean eagerlyReadResponse) throws IOException {
        /*
         * Use a buffered response when we are eagerly reading the response from the network and the body isn't
         * empty.
         */
        if (eagerlyReadResponse) {
            ResponseBody body = response.body();
            if (Objects.nonNull(body)) {
                byte[] bytes = body.bytes();
                body.close();
                return new OkHttpAsyncBufferedResponse(response, request, bytes);
            } else {
                // Body is null, use the non-buffering response.
                return new OkHttpAsyncResponse(response, request);
            }
        } else {
            return new OkHttpAsyncResponse(response, request);
        }
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
            sink.error(e);
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public void onResponse(okhttp3.Call call, okhttp3.Response response) {
            try {
                HttpResponse httpResponse = fromOkHttpResponse(response, request, eagerlyReadResponse);
                sink.success(httpResponse);
            } catch (IOException ex) {
                // Reading the body bytes may cause an IOException, if it happens propagate it.
                sink.error(ex);
            }
        }
    }
}
