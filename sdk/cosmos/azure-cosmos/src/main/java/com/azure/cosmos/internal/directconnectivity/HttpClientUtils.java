// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.internal.directconnectivity;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosClientException;
import com.azure.cosmos.CosmosError;
import com.azure.cosmos.internal.HttpConstants;
import com.azure.cosmos.internal.RxDocumentServiceResponse;
import com.azure.cosmos.internal.http.HttpRequest;
import com.azure.cosmos.internal.http.HttpResponse;
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
