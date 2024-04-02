// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.okhttp;

import com.generic.core.http.client.HttpClient;
import com.generic.core.http.models.HttpHeader;
import com.generic.core.http.models.HttpHeaderName;
import com.generic.core.http.models.HttpHeaders;
import com.generic.core.http.models.HttpMethod;
import com.generic.core.http.models.HttpRequest;
import com.generic.core.http.models.Response;
import com.generic.core.http.models.ResponseHandlingMode;
import com.generic.core.http.models.ServerSentEventListener;
import com.generic.core.http.okhttp.implementation.OkHttpFileRequestBody;
import com.generic.core.http.okhttp.implementation.OkHttpInputStreamRequestBody;
import com.generic.core.http.okhttp.implementation.OkHttpResponse;
import com.generic.core.implementation.util.ServerSentEventUtil;
import com.generic.core.util.ClientLogger;
import com.generic.core.util.binarydata.BinaryData;
import com.generic.core.util.binarydata.FileBinaryData;
import com.generic.core.util.binarydata.InputStreamBinaryData;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.io.UncheckedIOException;

import static com.generic.core.http.models.ContentType.APPLICATION_OCTET_STREAM;
import static com.generic.core.http.models.HttpHeaderName.CONTENT_TYPE;
import static com.generic.core.http.models.ResponseHandlingMode.BUFFER;
import static com.generic.core.http.models.ResponseHandlingMode.STREAM;
import static com.generic.core.implementation.util.ServerSentEventUtil.processTextEventStream;
/**
 * HttpClient implementation for OkHttp.
 */
class OkHttpHttpClient implements HttpClient {
    private static final ClientLogger LOGGER = new ClientLogger(OkHttpHttpClient.class);
    private static final byte[] EMPTY_BODY = new byte[0];
    private static final RequestBody EMPTY_REQUEST_BODY = RequestBody.create(EMPTY_BODY);
    final OkHttpClient httpClient;

    OkHttpHttpClient(OkHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public Response<?> send(HttpRequest request) {
        boolean eagerlyConvertHeaders = request.getMetadata().isEagerlyConvertHeaders();
        ResponseHandlingMode responseHandlingMode = request.getMetadata().getResponseHandlingMode();
        Request okHttpRequest = toOkHttpRequest(request);

        try {
            okhttp3.Response okHttpResponse = httpClient.newCall(okHttpRequest).execute();

            return toResponse(request, okHttpResponse, responseHandlingMode, eagerlyConvertHeaders);
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
        }
    }

    /**
     * Converts the given generic-core request to okhttp request.
     *
     * @param request the generic-core request.
     *
     * @return Th eOkHttp request.
     */
    private okhttp3.Request toOkHttpRequest(HttpRequest request) {
        Request.Builder requestBuilder = new Request.Builder()
            .url(request.getUrl());

        if (request.getHeaders() != null) {
            for (HttpHeader hdr : request.getHeaders()) {
                // OkHttp allows for headers with multiple values, but it treats them as separate headers,
                // therefore, we must call rb.addHeader for each value, using the same key for all of them
                hdr.getValues().forEach(value -> requestBuilder.addHeader(hdr.getName().toString(), value));
            }
        }

        if (request.getHttpMethod() == HttpMethod.GET) {
            return requestBuilder.get().build();
        } else if (request.getHttpMethod() == HttpMethod.HEAD) {
            return requestBuilder.head().build();
        }

        RequestBody okHttpRequestBody = toOkHttpRequestBody(request.getBody(), request.getHeaders());

        return requestBuilder.method(request.getHttpMethod().toString(), okHttpRequestBody).build();
    }

    /**
     * Create a Mono of okhttp3.RequestBody from the given BinaryData.
     *
     * @param bodyContent The request body content
     * @param headers the headers associated with the original request
     *
     * @return The Mono emitting okhttp request
     */
    private RequestBody toOkHttpRequestBody(BinaryData bodyContent, HttpHeaders headers) {
        if (bodyContent == null) {
            return EMPTY_REQUEST_BODY;
        }

        String contentType = headers.getValue(HttpHeaderName.CONTENT_TYPE);
        MediaType mediaType = (contentType == null) ? null : MediaType.parse(contentType);

        if (bodyContent instanceof InputStreamBinaryData) {
            long effectiveContentLength = getRequestContentLength(bodyContent, headers);

            // The OkHttpInputStreamRequestBody doesn't read bytes until it's triggered by OkHttp dispatcher.
            return new OkHttpInputStreamRequestBody((InputStreamBinaryData) bodyContent, effectiveContentLength,
                mediaType);
        } else if (bodyContent instanceof FileBinaryData) {
            long effectiveContentLength = getRequestContentLength(bodyContent, headers);

            // The OkHttpFileRequestBody doesn't read bytes until it's triggered by OkHttp dispatcher.
            return new OkHttpFileRequestBody((FileBinaryData) bodyContent, effectiveContentLength, mediaType);
        } else {
            return RequestBody.create(bodyContent.toBytes(), mediaType);
        }
    }

    private static long getRequestContentLength(BinaryData content, HttpHeaders headers) {
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

    private Response<?> toResponse(HttpRequest request, okhttp3.Response response,
                                   ResponseHandlingMode responseHandlingMode, boolean eagerlyConvertHeaders) throws IOException {
        okhttp3.Headers responseHeaders = response.headers();

        if (isTextEventStream(responseHeaders) && response.body() != null) {
            ServerSentEventListener listener = request.getServerSentEventListener();

            if (listener != null) {
                processTextEventStream(request, this::send, response.body().byteStream(), listener, LOGGER);
            } else {
                throw LOGGER.logThrowableAsError(new RuntimeException(ServerSentEventUtil.NO_LISTENER_ERROR_MESSAGE));
            }

            return new OkHttpResponse(response, request, eagerlyConvertHeaders, BinaryData.fromBytes(EMPTY_BODY));
        }

        return processResponse(request, response, responseHandlingMode, eagerlyConvertHeaders);
    }

    private Response<?> processResponse(HttpRequest request, okhttp3.Response response,
                                        ResponseHandlingMode responseHandlingMode, boolean eagerlyConvertHeaders) throws IOException {
        if (responseHandlingMode == null) {
            String contentType = response.headers().get(CONTENT_TYPE.getCaseInsensitiveName());

            if (contentType != null
                && APPLICATION_OCTET_STREAM.regionMatches(true, 0, contentType, 0, APPLICATION_OCTET_STREAM.length())) {

                responseHandlingMode = STREAM;
            } else {
                responseHandlingMode = BUFFER;
            }

            request.getMetadata().setResponseHandlingMode(responseHandlingMode);
        }

        BinaryData body = null;

        switch (responseHandlingMode) {
            case IGNORE:
                if (response.body() != null) {
                    response.body().close();
                }

                break;
            case STREAM:
                if (response.body() != null && response.body().contentLength() != 0) {
                    body = BinaryData.fromStream(response.body().byteStream());
                }

                break;
            case BUFFER:
            case DESERIALIZE:
            default:
                try (ResponseBody responseBody = response.body()) {
                    if (responseBody != null && responseBody.contentLength() != 0) {
                        body = BinaryData.fromBytes(responseBody.bytes());
                    }
                }
        }

        return new OkHttpResponse(response, request, eagerlyConvertHeaders,
            body == null ? BinaryData.fromBytes(EMPTY_BODY) : body);
    }

    private static boolean isTextEventStream(okhttp3.Headers responseHeaders) {
        if (responseHeaders != null) {
            return ServerSentEventUtil
                .isTextEventStreamContentType(responseHeaders.get(HttpHeaderName.CONTENT_TYPE.toString()));
        }

        return false;
    }
}
