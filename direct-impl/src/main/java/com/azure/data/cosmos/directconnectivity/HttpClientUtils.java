/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.azure.data.cosmos.directconnectivity;

import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.Error;
import com.azure.data.cosmos.internal.HttpConstants;
import com.azure.data.cosmos.internal.RxDocumentServiceResponse;
import com.azure.data.cosmos.internal.http.HttpRequest;
import com.azure.data.cosmos.internal.http.HttpResponse;
import reactor.core.publisher.Mono;

public class HttpClientUtils {

    static Mono<RxDocumentServiceResponse> parseResponseAsync(Mono<HttpResponse> httpResponse, HttpRequest httpRequest) {
        return httpResponse.flatMap(response -> {
            if (response.statusCode() < HttpConstants.StatusCodes.MINIMUM_STATUSCODE_AS_ERROR_GATEWAY) {

                return ResponseUtils.toStoreResponse(response, httpRequest).map(RxDocumentServiceResponse::new);

                // TODO: to break the dependency between RxDocumentServiceResponse and StoreResponse
                // we should factor out the  RxDocumentServiceResponse(StoreResponse) constructor to a helper class

            } else {
                return HttpClientUtils
                        .createDocumentClientException(response).flatMap(Mono::error);
            }
        });
    }

    private static Mono<CosmosClientException> createDocumentClientException(HttpResponse httpResponse) {
        Mono<String> readStream = ResponseUtils.toString(httpResponse.body());

        return readStream.map(body -> {
            Error error = new Error(body);

            // TODO: we should set resource address in the Document Client Exception
            return new CosmosClientException(httpResponse.statusCode(), error,
                    httpResponse.headers().toMap());
        });
    }
}
