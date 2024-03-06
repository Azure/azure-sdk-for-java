// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.okhttp.implementation;

import com.generic.core.http.Response;
import com.generic.core.http.models.HttpRequest;
import com.generic.core.models.BinaryData;
import com.generic.core.models.HeaderName;
import com.generic.core.models.Headers;
import okhttp3.ResponseBody;

import java.util.function.Function;

/**
 * Base response class for OkHttp with implementations for response metadata.
 */
public class OkHttpResponse<T> implements Response<T> {
    private static final BinaryData EMPTY_BODY = BinaryData.fromBytes(new byte[0]);

    private final BinaryData body;
    private final Function<Response<?>, ?> deserializationCallback;
    private final Headers headers;
    private final HttpRequest request;
    private final int statusCode;
    private final ResponseBody responseBody;

    private boolean isBodyDeserialized = false;
    private T value;

    public OkHttpResponse(okhttp3.Response response, HttpRequest request, boolean eagerlyConvertHeaders,
                          byte[] bodyBytes) {
        this.request = request;
        this.statusCode = response.code();
        this.headers = eagerlyConvertHeaders
            ? fromOkHttpHeaders(response.headers())
            : new OkHttpToGenericCoreHttpHeadersWrapper(response.headers());
        // innerResponse.body() getter will not return null for server returned responses.
        // It can be null:
        // [a]. if response is built manually with null body (e.g. for mocking)
        // [b]. for the cases described here
        // [ref](https://square.github.io/okhttp/4.x/okhttp/okhttp3/-response/body/).
        this.responseBody = response.body();
        this.body = bodyBytes == null
            ? responseBody == null
                ? EMPTY_BODY
                : BinaryData.fromStream(responseBody.byteStream())
            : BinaryData.fromBytes(bodyBytes);
        this.deserializationCallback = request == null
            ? (genericCoreResponse -> this.value)
            : request.getResponseBodyDeserializationCallback();
    }

    /**
     * Creates {@link Headers Generic Core's headers} from {@link okhttp3.Headers OkHttp headers}.
     *
     * @param okHttpHeaders {@link okhttp3.Headers OkHttp headers}.
     *
     * @return {@link Headers Generic Core's headers}.
     */
    static Headers fromOkHttpHeaders(okhttp3.Headers okHttpHeaders) {
        /*
         * While OkHttp's Headers class offers a method which converts the headers into a Map<String, List<String>>,
         * which matches one of the setters in our HttpHeaders, the method implicitly lower cases header names while
         * doing the conversion. This is fine when working purely with HTTPs request-response structure as headers are
         * case-insensitive per their definition RFC but this could cause issues when/if the headers are used in
         * serialization or deserialization as casing may matter.
         */
        Headers httpHeaders = new Headers((int) (okHttpHeaders.size() / 0.75F));

        /*
         * Use OkHttp's Headers.forEach() instead of the names and values approach. forEach() allows for a single
         * iteration over the internal array of header values whereas names and values will iterate over the internal
         * array of header values for each name. With the new approach we also use Generic Core's Headers.add() method.
         * Overall, this is much better performing as almost all headers will have a single value.
         */
        okHttpHeaders.forEach(nameValuePair ->
            httpHeaders.add(HeaderName.fromString(nameValuePair.getFirst()), nameValuePair.getSecond()));

        return httpHeaders;
    }

    @Override
    public int getStatusCode() {
        return this.statusCode;
    }

    @Override
    public Headers getHeaders() {
        return this.headers;
    }

    @Override
    public HttpRequest getRequest() {
        return this.request;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T getValue() {
        if (!isBodyDeserialized) {
            value = (T) deserializationCallback.apply(this);
            isBodyDeserialized = true;
        }

        return value;
    }

    @Override
    public BinaryData getBody() {
        return this.body;
    }

    @Override
    public void close() {
        if (this.responseBody != null) {
            // It's safe to invoke close() multiple times, additional calls will be ignored.
            this.responseBody.close();
        }
    }
}
