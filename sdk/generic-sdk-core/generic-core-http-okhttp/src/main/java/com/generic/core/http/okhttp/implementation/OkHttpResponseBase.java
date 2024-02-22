// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.okhttp.implementation;

import com.generic.core.http.models.HttpRequest;
import com.generic.core.http.models.HttpResponse;
import com.generic.core.models.BinaryData;
import com.generic.core.models.HeaderName;
import okhttp3.Headers;
import okhttp3.Response;

/**
 * Base response class for OkHttp with implementations for response metadata.
 */
abstract class OkHttpResponseBase extends HttpResponse {

    OkHttpResponseBase(Response response, HttpRequest request, boolean eagerlyConvertHeaders) {
        this(response, request, eagerlyConvertHeaders, null);
    }

    OkHttpResponseBase(Response response, HttpRequest request, boolean eagerlyConvertHeaders, BinaryData value) {
        super(request,
            response.code(),
            eagerlyConvertHeaders
                ? fromOkHttpHeaders(response.headers())
                : new OkHttpToAzureCoreHttpHeadersWrapper(response.headers()),
            value);
    }

    /**
     * Creates generic-core Headers from {@link Headers OkHttp Headers}.
     *
     * @param okHttpHeaders {@link Headers OkHttp Headers}.
     *
     * @return {@link com.generic.core.models.Headers Generic Core Headers}.
     */
    static com.generic.core.models.Headers fromOkHttpHeaders(Headers okHttpHeaders) {
        /*
         * While OkHttp's Headers class offers a method which converts the headers into a Map<String, List<String>>,
         * which matches one of the setters in our Headers, the method implicitly lower cases header names while doing
         * the conversion. This is fine when working purely with HTTP's request-response structure as headers are
         * case-insensitive per their definition RFC but this could cause issues when/if the headers are used in
         * serialization or deserialization as casing may matter.
         */
        com.generic.core.models.Headers headers = new com.generic.core.models.Headers((int) (okHttpHeaders.size() / 0.75F));

        /*
         * Use OkHttp's Headers.forEach() instead of fetching using names and values.
         *
         * forEach() allows for a single iteration over the internal array of header values whereas names and values
         * will iterate over the internal array of header values for each name. This new approach uses generic-core's
         * Headers.add() method.
         *
         * Overall, this is much better performing as almost all headers will have a single value.
         */
        okHttpHeaders.forEach(nameValuePair ->
            headers.add(HeaderName.fromString(nameValuePair.getFirst()), nameValuePair.getSecond()));

        return headers;
    }
}
