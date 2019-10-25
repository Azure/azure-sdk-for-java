// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.internal.directconnectivity;


import com.azure.cosmos.BadRequestException;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.internal.HttpConstants;
import com.azure.cosmos.internal.RMResources;
import com.azure.cosmos.internal.RxDocumentServiceRequest;
import com.azure.cosmos.internal.RxDocumentServiceResponse;
import com.azure.cosmos.internal.RxStoreModel;
import com.azure.cosmos.internal.Strings;
import org.apache.commons.lang3.EnumUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ServerStoreModel implements RxStoreModel {
    private final StoreClient storeClient;

    public ServerStoreModel(StoreClient storeClient) {
        this.storeClient = storeClient;
    }

    public Flux<RxDocumentServiceResponse> processMessage(RxDocumentServiceRequest request) {
        String requestConsistencyLevelHeaderValue = request.getHeaders().get(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL);

        request.requestContext.originalRequestConsistencyLevel = null;

        if (!Strings.isNullOrEmpty(requestConsistencyLevelHeaderValue)) {
            ConsistencyLevel requestConsistencyLevel;

            if ((requestConsistencyLevel = EnumUtils.getEnum(ConsistencyLevel.class, Strings.fromCamelCaseToUpperCase(requestConsistencyLevelHeaderValue))) == null) {
                return Flux.error(new BadRequestException(
                    String.format(
                        RMResources.InvalidHeaderValue,
                        requestConsistencyLevelHeaderValue,
                        HttpConstants.HttpHeaders.CONSISTENCY_LEVEL)));
            }

            request.requestContext.originalRequestConsistencyLevel = requestConsistencyLevel;
        }

        if (ReplicatedResourceClient.isMasterResource(request.getResourceType())) {
            request.getHeaders().put(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL, ConsistencyLevel.STRONG.toString());
        }

        Mono<RxDocumentServiceResponse> response = this.storeClient.processMessageAsync(request);
        return response.flux();
    }
}
