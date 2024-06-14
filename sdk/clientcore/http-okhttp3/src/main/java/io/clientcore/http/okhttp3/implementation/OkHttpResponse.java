// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.okhttp3.implementation;

import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.HttpResponse;
import io.clientcore.core.util.binarydata.BinaryData;
import okhttp3.Headers;
import okhttp3.ResponseBody;

/**
 * Base response class for OkHttp with implementations for response metadata.
 */
public class OkHttpResponse extends HttpResponse<BinaryData> {
    private final ResponseBody responseBody;
    private BinaryData body;

    public OkHttpResponse(okhttp3.Response response, HttpRequest request, BinaryData body) {
        super(request, response.code(), new OkHttpToCoreHttpHeadersWrapper(response.headers()), null);

        // innerResponse.body() getter will not return null for server returned responses.
        // It can be null:
        // [a]. if response is built manually with null body (e.g. for mocking)
        // [b]. for the cases described here
        // [ref](https://square.github.io/okhttp/4.x/okhttp/okhttp3/-response/body/).
        this.responseBody = response.body();
        this.body = body;
    }

    /**
     * Creates {@link HttpHeaders Generic Core's headers} from {@link Headers OkHttp headers}.
     *
     * @param okHttpHeaders {@link Headers OkHttp headers}.
     *
     * @return {@link HttpHeaders Generic Core's headers}.
     */
    static HttpHeaders fromOkHttpHeaders(Headers okHttpHeaders) {
        /*
         * While OkHttp's Headers class offers a method which converts the headers into a Map<String, List<String>>,
         * which matches one of the setters in our HttpHeaders, the method implicitly lower cases header names while
         * doing the conversion. This is fine when working purely with HTTPs request-response structure as headers are
         * case-insensitive per their definition RFC but this could cause issues when/if the headers are used in
         * serialization or deserialization as casing may matter.
         */
        HttpHeaders httpHeaders = new HttpHeaders((int) (okHttpHeaders.size() / 0.75F));

        /*
         * Use OkHttp's Headers.forEach() instead of the names and values approach. forEach() allows for a single
         * iteration over the internal array of header values whereas names and values will iterate over the internal
         * array of header values for each name. With the new approach we also use Generic Core's Headers.add() method.
         * Overall, this is much better performing as almost all headers will have a single value.
         */
        okHttpHeaders.forEach(nameValuePair ->
            httpHeaders.add(HttpHeaderName.fromString(nameValuePair.getFirst()), nameValuePair.getSecond()));

        return httpHeaders;
    }

    /**
     * Gets the {@link BinaryData} that represents the body of the response.
     *
     * @return The {@link BinaryData} containing the response body.
     */
    @Override
    public BinaryData getBody() {
        if (body == null) {
            if (super.getValue() == null) {
                body = BinaryData.empty();
            } else {
                body = super.getValue();
            }
        }

        return body;
    }

    @Override
    public void close() {
        if (this.responseBody != null) {
            // It's safe to invoke close() multiple times, additional calls will be ignored.
            this.responseBody.close();
        }
    }
}
