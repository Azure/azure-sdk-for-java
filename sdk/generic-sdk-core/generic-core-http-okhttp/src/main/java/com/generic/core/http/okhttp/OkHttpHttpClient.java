// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.okhttp;

import com.generic.core.http.client.HttpClient;
import com.generic.core.http.models.HttpHeaderName;
import com.generic.core.http.models.HttpMethod;
import com.generic.core.http.models.HttpRequest;
import com.generic.core.http.models.HttpResponse;
import com.generic.core.http.okhttp.implementation.OkHttpBufferedResponse;
import com.generic.core.http.okhttp.implementation.OkHttpFileRequestBody;
import com.generic.core.http.okhttp.implementation.OkHttpInputStreamRequestBody;
import com.generic.core.http.okhttp.implementation.OkHttpResponse;
import com.generic.core.models.BinaryData;
import com.generic.core.models.FileBinaryData;
import com.generic.core.models.Header;
import com.generic.core.models.Headers;
import com.generic.core.models.InputStreamBinaryData;
import com.generic.core.util.logging.ClientLogger;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * HttpClient implementation for OkHttp.
 */
class OkHttpHttpClient implements HttpClient {

    private static final ClientLogger LOGGER = new ClientLogger(OkHttpHttpClient.class);
    private static final byte[] EMPTY_BODY = new byte[0];
    private static final RequestBody EMPTY_REQUEST_BODY = RequestBody.create(EMPTY_BODY);

    private static final String HTTP_REQUEST_PROGRESS_REPORTER = "com.generic.core.http.request.progress.reporter";

    final OkHttpClient httpClient;

    OkHttpHttpClient(OkHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public HttpResponse send(HttpRequest request) {
        boolean eagerlyConvertHeaders = request.getMetadata().isEagerlyConvertHeaders();
        boolean eagerlyReadResponse = request.getMetadata().isEagerlyReadResponse();
        boolean ignoreResponseBody = request.getMetadata().isIgnoreResponseBody();

        Request okHttpRequest = toOkHttpRequest(request);
        try {
            Response okHttpResponse = httpClient.newCall(okHttpRequest).execute();
            return toHttpResponse(request, okHttpResponse, eagerlyReadResponse, ignoreResponseBody,
                eagerlyConvertHeaders);
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
            for (Header hdr : request.getHeaders()) {
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
    private RequestBody toOkHttpRequestBody(BinaryData bodyContent, Headers headers) {
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

    private static long getRequestContentLength(BinaryData content, Headers headers) {
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
                                               boolean eagerlyReadResponse, boolean ignoreResponseBody,
                                               boolean eagerlyConvertHeaders) throws IOException {
        /*// For now, eagerlyReadResponse and ignoreResponseBody works the same.
        if (ignoreResponseBody) {
            ResponseBody body = response.body();

            if (body != null) {
                if (body.contentLength() > 0) {
                    LOGGER.log(LogLevel.WARNING, () -> "Received HTTP response body when one wasn't expected. "
                        + "Response body will be ignored as directed.");
                }

                body.close();
            }

            return new OkHttpBufferedResponse(response, request, EMPTY_BODY, eagerlyConvertHeaders);
        }*/

        // Use a buffered response when we are eagerly reading the response from the network and the body isn't empty.
        if (eagerlyReadResponse || ignoreResponseBody) {
            try (ResponseBody body = response.body()) {
                byte[] bytes = (body != null) ? body.bytes() : EMPTY_BODY;

                return new OkHttpBufferedResponse(response, request, bytes, eagerlyConvertHeaders);
            }
        } else {
            return new OkHttpResponse(response, request, eagerlyConvertHeaders);
        }
    }
}
