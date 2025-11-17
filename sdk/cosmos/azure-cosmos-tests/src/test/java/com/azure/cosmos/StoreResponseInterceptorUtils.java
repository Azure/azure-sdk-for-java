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
        ConsistencyLevel operationConsistencyLevel,
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

                return storeResponse;
            }

            if (OperationType.Read.equals(request.getOperationType()) && regionName.equals(request.requestContext.regionalRoutingContextToRoute.getGatewayRegionalEndpoint().toString())) {

                if (ConsistencyLevel.STRONG.equals(operationConsistencyLevel)) {

                    long globalLsn = Long.parseLong(storeResponse.getHeaderValue(WFConstants.BackendHeaders.LSN));
                    long itemLsn = Long.parseLong(storeResponse.getHeaderValue(WFConstants.BackendHeaders.ITEM_LSN));
                    long manipulatedGlobalCommittedLSN = Math.min(globalLsn, itemLsn) - 1;

                    storeResponse.setHeaderValue(WFConstants.BackendHeaders.GLOBAL_COMMITTED_LSN, String.valueOf(manipulatedGlobalCommittedLSN));

                    return storeResponse;
                } else if (ConsistencyLevel.BOUNDED_STALENESS.equals(operationConsistencyLevel)) {

                    long manipulatedItemLSN = -1;
                    long manipulatedGlobalLSN = 0;

                    storeResponse.setHeaderValue(WFConstants.BackendHeaders.LSN, String.valueOf(manipulatedGlobalLSN));
                    storeResponse.setHeaderValue(WFConstants.BackendHeaders.LOCAL_LSN, String.valueOf(manipulatedGlobalLSN));
                    storeResponse.setHeaderValue(WFConstants.BackendHeaders.ITEM_LSN, String.valueOf(manipulatedItemLSN));
                    storeResponse.setHeaderValue(WFConstants.BackendHeaders.ITEM_LOCAL_LSN, String.valueOf(manipulatedItemLSN));

                    return storeResponse;
                }

                return storeResponse;
            }

            if (OperationType.Head.equals(request.getOperationType()) && regionName.equals(request.requestContext.regionalRoutingContextToRoute.getGatewayRegionalEndpoint().toString())) {
                if (failureCount.incrementAndGet() <= maxAllowedFailureCount) {
                    throw Utils.createCosmosException(statusCode, subStatusCode, new Exception("An intercepted exception occurred. Check status and substatus code for details."), null);
                }
            }

            return storeResponse;
        };
    }

    public static BiFunction<RxDocumentServiceRequest, StoreResponse, StoreResponse> forceSuccessfulBarriersOnReadUntilQuorumSelectionThenForceBarrierFailures(
        ConsistencyLevel operationConsistencyLevel,
        String regionName,
        int allowedSuccessfulHeadRequestsWithoutBarrierBeingMet,
        AtomicInteger successfulHeadRequestCount,
        int maxAllowedFailureCount,
        AtomicInteger failureCount,
        int statusCode,
                                                                                                                                                               int subStatusCode) {
        return (request, storeResponse) -> {

            if (OperationType.Read.equals(request.getOperationType()) && regionName.equals(request.requestContext.regionalRoutingContextToRoute.getGatewayRegionalEndpoint().toString())) {

                if (ConsistencyLevel.STRONG.equals(operationConsistencyLevel)) {

                    long globalLsn = Long.parseLong(storeResponse.getHeaderValue(WFConstants.BackendHeaders.LSN));
                    long itemLsn = Long.parseLong(storeResponse.getHeaderValue(WFConstants.BackendHeaders.ITEM_LSN));
                    long manipulatedGlobalCommittedLSN = Math.min(globalLsn, itemLsn) - 1;

                    storeResponse.setHeaderValue(WFConstants.BackendHeaders.GLOBAL_COMMITTED_LSN, String.valueOf(manipulatedGlobalCommittedLSN));

                    return storeResponse;
                } else if (ConsistencyLevel.BOUNDED_STALENESS.equals(operationConsistencyLevel)) {

                    long manipulatedItemLSN = -1;
                    long manipulatedGlobalLSN = 0;

                    storeResponse.setHeaderValue(WFConstants.BackendHeaders.LSN, String.valueOf(manipulatedGlobalLSN));
                    storeResponse.setHeaderValue(WFConstants.BackendHeaders.LOCAL_LSN, String.valueOf(manipulatedGlobalLSN));
                    storeResponse.setHeaderValue(WFConstants.BackendHeaders.ITEM_LSN, String.valueOf(manipulatedItemLSN));
                    storeResponse.setHeaderValue(WFConstants.BackendHeaders.ITEM_LOCAL_LSN, String.valueOf(manipulatedItemLSN));

                    return storeResponse;
                }

                return storeResponse;
            }

            if (OperationType.Head.equals(request.getOperationType()) && regionName.equals(request.requestContext.regionalRoutingContextToRoute.getGatewayRegionalEndpoint().toString())) {

                if (successfulHeadRequestCount.incrementAndGet() <= allowedSuccessfulHeadRequestsWithoutBarrierBeingMet) {

                    if (ConsistencyLevel.STRONG.equals(operationConsistencyLevel)) {
                        System.out.println("Allowing successful barrier for head request number: " + successfulHeadRequestCount.get());

                        long localLsn = Long.parseLong(storeResponse.getHeaderValue(WFConstants.BackendHeaders.LOCAL_LSN));
                        long itemLsn = Long.parseLong(storeResponse.getHeaderValue(WFConstants.BackendHeaders.ITEM_LSN));
                        long manipulatedGCLSN = Math.min(localLsn, itemLsn) - 1;

                        storeResponse.setHeaderValue(WFConstants.BackendHeaders.GLOBAL_COMMITTED_LSN, String.valueOf(manipulatedGCLSN));

                        return storeResponse;
                    } else if (ConsistencyLevel.BOUNDED_STALENESS.equals(operationConsistencyLevel)) {
                        System.out.println("Allowing successful barrier for head request number: " + successfulHeadRequestCount.get());

                        long manipulatedItemLSN = -1;
                        long manipulatedGlobalLSN = -1;

                        storeResponse.setHeaderValue(WFConstants.BackendHeaders.LSN, String.valueOf(manipulatedGlobalLSN));
                        storeResponse.setHeaderValue(WFConstants.BackendHeaders.LOCAL_LSN, String.valueOf(manipulatedGlobalLSN));
                        storeResponse.setHeaderValue(WFConstants.BackendHeaders.ITEM_LSN, String.valueOf(manipulatedItemLSN));
                        storeResponse.setHeaderValue(WFConstants.BackendHeaders.ITEM_LOCAL_LSN, String.valueOf(manipulatedItemLSN));

                        return storeResponse;
                    }

                    return storeResponse;
                }

                if (failureCount.incrementAndGet() <= maxAllowedFailureCount) {
                    throw Utils.createCosmosException(statusCode, subStatusCode, new Exception("An intercepted exception occurred. Check status and substatus code for details."), null);
                }
            }

            return storeResponse;
        };
    }
}
