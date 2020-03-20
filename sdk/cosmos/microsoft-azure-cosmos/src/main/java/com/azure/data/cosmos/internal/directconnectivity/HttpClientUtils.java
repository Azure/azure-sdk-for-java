// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity;

import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.CosmosError;
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
        Mono<String> readStream = httpResponse.bodyAsString();

        return readStream.map(body -> {
            CosmosError cosmosError = BridgeInternal.createCosmosError(body);

            // TODO: we should set resource address in the Document Client Exception
            return BridgeInternal.createCosmosClientException(httpResponse.statusCode(), cosmosError,
                    httpResponse.headers().toMap());
        });
    }
}
