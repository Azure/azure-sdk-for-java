// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;


import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.implementation.BadRequestException;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.RMResources;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.RxDocumentServiceResponse;
import com.azure.cosmos.implementation.RxStoreModel;
import com.azure.cosmos.implementation.Strings;
import com.azure.cosmos.implementation.throughputControl.ThroughputControlStore;
import reactor.core.publisher.Mono;

public class ServerStoreModel implements RxStoreModel {
    private final StoreClient storeClient;

    public ServerStoreModel(StoreClient storeClient) {
        this.storeClient = storeClient;
    }

    public Mono<RxDocumentServiceResponse> processMessage(RxDocumentServiceRequest request) {
        String requestConsistencyLevelHeaderValue = request.getHeaders().get(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL);

        request.requestContext.originalRequestConsistencyLevel = null;

        if (!Strings.isNullOrEmpty(requestConsistencyLevelHeaderValue)) {
            ConsistencyLevel requestConsistencyLevel;
            if ((requestConsistencyLevel = BridgeInternal.fromServiceSerializedFormat(requestConsistencyLevelHeaderValue)) == null) {
                return Mono.error(new BadRequestException(
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

        return this.storeClient.processMessageAsync(request);
    }

    @Override
    public void enableThroughputControl(ThroughputControlStore throughputControlStore) {
        this.storeClient.enableThroughputControl(throughputControlStore);
    }
}
