// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.okhttp.implementation;

import com.generic.core.http.models.HttpRequest;
import com.generic.core.http.models.HttpResponse;
import com.generic.core.models.BinaryData;
import com.generic.core.models.HeaderName;
import okhttp3.Headers;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Base response class for OkHttp with implementations for response metadata.
 */
public class OkHttpResponse extends HttpResponse<BinaryData> {
    private final ResponseBody responseBody;

    public OkHttpResponse(Response response, HttpRequest request, boolean eagerlyConvertHeaders, byte[] bodyBytes) {
        super(request,
            response.code(),
            eagerlyConvertHeaders
                ? fromOkHttpHeaders(response.headers())
                : new OkHttpToAzureCoreHttpHeadersWrapper(response.headers()),
            bodyBytes == null
                ? response.body() == null
                    ? EMPTY_BODY
                    : BinaryData.fromStream(response.body().byteStream())
                : BinaryData.fromBytes(bodyBytes),
            true);

        // innerResponse.body() getter will not return null for server returned responses.
        // It can be null:
        // [a]. if response is built manually with null body (e.g. for mocking)
        // [b]. for the cases described here
        // [ref](https://square.github.io/okhttp/4.x/okhttp/okhttp3/-response/body/).
        this.responseBody = response.body();
    }

    /**
     * Creates {@link com.generic.core.models.Headers Generic Core's headers} from {@link Headers OkHttp headers}.
     *
     * @param okHttpHeaders {@link Headers OkHttp headers}.
     *
     * @return {@link com.generic.core.models.Headers Generic Core's headers}.
     */
    static com.generic.core.models.Headers fromOkHttpHeaders(Headers okHttpHeaders) {
        /*
         * While OkHttp's Headers class offers a method which converts the headers into a Map<String, List<String>>,
         * which matches one of the setters in our HttpHeaders, the method implicitly lower cases header names while
         * doing the conversion. This is fine when working purely with HTTPs request-response structure as headers are
         * case-insensitive per their definition RFC but this could cause issues when/if the headers are used in
         * serialization or deserialization as casing may matter.
         */
        com.generic.core.models.Headers httpHeaders =
            new com.generic.core.models.Headers((int) (okHttpHeaders.size() / 0.75F));

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
    public void close() {
        if (this.responseBody != null) {
            // It's safe to invoke close() multiple times, additional calls will be ignored.
            this.responseBody.close();
        }
    }
}
