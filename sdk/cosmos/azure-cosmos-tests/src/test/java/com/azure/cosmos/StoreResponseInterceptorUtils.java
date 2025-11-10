// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.directconnectivity.StoreResponse;
import com.azure.cosmos.implementation.directconnectivity.WFConstants;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

public class StoreResponseInterceptorUtils {

    public static BiFunction<RxDocumentServiceRequest, StoreResponse, StoreResponse> forceBarrierFollowedByBarrierFailure(
        OperationType operationType,
        String regionName,
        int maxAllowedFailureCount,
        AtomicInteger failureCount,
        int statusCode,
        int subStatusCode) {

        return (request, storeResponse) -> {

            if (OperationType.Create.equals(request.getOperationType()) && regionName.equals(request.requestContext.regionalRoutingContextToRoute.getGatewayRegionalEndpoint().toString())) {

                long localLsn = Long.parseLong(storeResponse.getHeaderValue(WFConstants.BackendHeaders.LOCAL_LSN));
                long manipulatedGCLSN = localLsn - 1;

                storeResponse.setHeaderValue(WFConstants.BackendHeaders.GLOBAL_COMMITTED_LSN, String.valueOf(manipulatedGCLSN));
            }

            if (OperationType.Read.equals(request.getOperationType()) && regionName.equals(request.requestContext.regionalRoutingContextToRoute.getGatewayRegionalEndpoint().toString())) {

                long localLsn = Long.parseLong(storeResponse.getHeaderValue(WFConstants.BackendHeaders.LOCAL_LSN));
                long itemLsn = Long.parseLong(storeResponse.getHeaderValue(WFConstants.BackendHeaders.ITEM_LSN));
                long manipulatedGCLSN = Math.min(localLsn, itemLsn) - 1;

                storeResponse.setHeaderValue(WFConstants.BackendHeaders.GLOBAL_COMMITTED_LSN, String.valueOf(manipulatedGCLSN));
            }

            if (OperationType.Head.equals(request.getOperationType()) && regionName.equals(request.requestContext.regionalRoutingContextToRoute.getGatewayRegionalEndpoint().toString())) {
                if (failureCount.incrementAndGet() <= maxAllowedFailureCount) {
                    throw Utils.createCosmosException(statusCode, subStatusCode, new Exception("An intercepted exception occurred. Check status and substatus code for details."), null);
                }
            }

            return storeResponse;
        };
    }
}
