// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.CosmosError;
import com.azure.cosmos.implementation.DiagnosticsClientContext;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.RxDocumentServiceResponse;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.http.HttpRequest;
import com.azure.cosmos.implementation.http.HttpResponse;
import reactor.core.publisher.Mono;

public class HttpClientUtils {

    static Mono<RxDocumentServiceResponse> parseResponseAsync(RxDocumentServiceRequest request,
                                                              DiagnosticsClientContext diagnosticsClientContext,
                                                              Mono<HttpResponse> httpResponse,
                                                              HttpRequest httpRequest) {
        return httpResponse.flatMap(response -> {
            if (response.statusCode() < HttpConstants.StatusCodes.MINIMUM_STATUSCODE_AS_ERROR_GATEWAY) {

                return ResponseUtils.toStoreResponse(response, httpRequest).map(rsp -> new RxDocumentServiceResponse(diagnosticsClientContext, rsp));

                // TODO: to break the dependency between RxDocumentServiceResponse and StoreResponse
                // we should factor out the  RxDocumentServiceResponse(StoreResponse) constructor to a helper class

            } else {
                return HttpClientUtils
                        .createDocumentClientException(request, response).flatMap(Mono::error);
            }
        });
    }

    private static Mono<CosmosException> createDocumentClientException(RxDocumentServiceRequest request, HttpResponse httpResponse) {
        Mono<String> readStream = httpResponse.bodyAsString().switchIfEmpty(Mono.just(StringUtils.EMPTY));

        return readStream.map(body -> {
            CosmosError cosmosError = new CosmosError(body);

            return BridgeInternal.createCosmosException(request.requestContext.resourcePhysicalAddress, httpResponse.statusCode(),
                cosmosError, httpResponse.headers().toMap());
        });
    }
}
