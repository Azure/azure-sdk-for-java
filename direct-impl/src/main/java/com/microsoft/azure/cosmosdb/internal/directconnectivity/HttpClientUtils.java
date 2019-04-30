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

package com.microsoft.azure.cosmosdb.internal.directconnectivity;

import com.microsoft.azure.cosmosdb.DocumentClientException;
import com.microsoft.azure.cosmosdb.Error;
import com.microsoft.azure.cosmosdb.internal.HttpConstants;
import com.microsoft.azure.cosmosdb.rx.internal.RxDocumentServiceResponse;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.client.HttpClientResponse;
import rx.Single;

public class HttpClientUtils {

    public static Single<RxDocumentServiceResponse> parseResponseAsync(HttpClientResponse<ByteBuf> responseMessage) {

        if (responseMessage.getStatus().code() < HttpConstants.StatusCodes.MINIMUM_STATUSCODE_AS_ERROR_GATEWAY) {

            Single<StoreResponse> storeResponse = ResponseUtils.toStoreResponse(responseMessage);
            return storeResponse.map(sr -> new RxDocumentServiceResponse(sr));

            // TODO: to break the dependency between RxDocumentServiceResponse and StoreResponse
            // we should factor out the  RxDocumentServiceResponse(StoreResponse) constructor to a helper class

        } else {
            return HttpClientUtils.createDocumentClientException(responseMessage).flatMap(e -> Single.error(e));
        }
    }

    private static Single<DocumentClientException> createDocumentClientException(HttpClientResponse<ByteBuf> responseMessage) {
        Single<String> readStream = ResponseUtils.toString(responseMessage.getContent()).toSingle();

        return readStream.map(body -> {
            Error error = new Error(body);

            // TODO: we should set resource address in the Document Client Exception

            return new DocumentClientException(responseMessage.getStatus().code(), error,
                    HttpUtils.asMap(responseMessage.getHeaders()));
        });
    }
}
