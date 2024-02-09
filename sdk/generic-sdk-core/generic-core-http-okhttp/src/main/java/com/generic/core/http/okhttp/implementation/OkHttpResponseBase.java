// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.okhttp.implementation;

import com.generic.core.models.HeaderName;
import com.generic.core.http.models.HttpRequest;
import com.generic.core.http.models.HttpResponse;
import okhttp3.Headers;
import okhttp3.Response;

/**
 * Base response class for OkHttp with implementations for response metadata.
 */
abstract class OkHttpResponseBase extends HttpResponse {
    private final int statusCode;
    private final com.generic.core.models.Headers headers;

    OkHttpResponseBase(Response response, HttpRequest request, boolean eagerlyConvertHeaders) {
        super(request);

        this.statusCode = response.code();
        this.headers = eagerlyConvertHeaders
            ? fromOkHttpHeaders(response.headers())
            : new OkHttpToAzureCoreHttpHeadersWrapper(response.headers());
    }

    @Override
    public final int getStatusCode() {
        return this.statusCode;
    }


    public final String getHeaderValue(HeaderName headerName) {
        return this.headers.getValue(headerName);
    }

    @Override
    public final com.generic.core.models.Headers getHeaders() {
        return this.headers;
    }

    /**
     * Creates generic-core Headers from OkHttp headers.
     *
     * @param okHttpHeaders OkHttp headers
     *
     * @return generic-core Headers
     */
    static com.generic.core.models.Headers fromOkHttpHeaders(Headers okHttpHeaders) {
        /*
         * While OkHttp's Headers class offers a method which converts the headers into a Map<String, List<String>>,
         * which matches one of the setters in our HttpHeaders, the method implicitly lower cases header names while
         * doing the conversion. This is fine when working purely with HTTPs request-response structure as headers are
         * case-insensitive per their definition RFC but this could cause issues when/if the headers are used in
         * serialization or deserialization as casing may matter.
         */
        com.generic.core.models.Headers httpHeaders = new com.generic.core.models.Headers((int) (okHttpHeaders.size() / 0.75F));

        // Use OkHttp's Headers forEach instead of the previous names and values approach.
        // forEach allows for a single iteration over the internal array of header values whereas names and values
        // will iterate over the internal array of header values for each name. With the new approach use generic-core
        // HttpHeaders add method.
        // Overall, this is much better performing as almost all headers will have a single value.
        okHttpHeaders.forEach(nameValuePair ->
            httpHeaders.add(HeaderName.fromString(nameValuePair.getFirst()), nameValuePair.getSecond()));

        return httpHeaders;
    }
}
