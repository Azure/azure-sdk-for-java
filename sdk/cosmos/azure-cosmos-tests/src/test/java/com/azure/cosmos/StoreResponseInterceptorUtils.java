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
        String regionName,
        int maxAllowedFailureCount,
        AtomicInteger failureCount,
        int statusCode,
        int subStatusCode) {

        return (request, storeResponse) -> {

            if (OperationType.Create.equals(request.getOperationType()) && regionName.equals(request.requestContext.regionalRoutingContextToRoute.getGatewayRegionalEndpoint().toString())) {

                long globalLsn = Long.parseLong(storeResponse.getHeaderValue(WFConstants.BackendHeaders.LSN));
                long manipulatedGlobalCommittedLSN = globalLsn - 1;

                storeResponse.setHeaderValue(WFConstants.BackendHeaders.GLOBAL_COMMITTED_LSN, String.valueOf(manipulatedGlobalCommittedLSN));

                return storeResponse;
            }

            if (OperationType.Read.equals(request.getOperationType()) && regionName.equals(request.requestContext.regionalRoutingContextToRoute.getGatewayRegionalEndpoint().toString())) {

                long globalLsn = Long.parseLong(storeResponse.getHeaderValue(WFConstants.BackendHeaders.LSN));
                long itemLsn = Long.parseLong(storeResponse.getHeaderValue(WFConstants.BackendHeaders.ITEM_LSN));
                long manipulatedGlobalCommittedLSN = Math.min(globalLsn, itemLsn) - 1;
                long manipulatedGlobalLsn = itemLsn - 1;

                // Force barrier by setting GCLSN to be less than both local LSN and item LSN - applicable to strong consistency reads
                storeResponse.setHeaderValue(WFConstants.BackendHeaders.GLOBAL_COMMITTED_LSN, String.valueOf(manipulatedGlobalCommittedLSN));

                // Force barrier by setting the (LSN - can correspond to GLSN when that particular document was part of a commit increment)
                // to less than itemLsn - applicable to bounded staleness consistency reads
                storeResponse.setHeaderValue(WFConstants.BackendHeaders.LSN, String.valueOf(manipulatedGlobalLsn));

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

    public static BiFunction<RxDocumentServiceRequest, StoreResponse, StoreResponse> forceSuccessfulBarriersOnReadUntilQuorumSelectionThenForceBarrierFailures(String regionName,
                                                                                                                                                               int allowedSuccessfulHeadRequestsWithoutBarrierBeingMet,
                                                                                                                                                               AtomicInteger successfulHeadRequestCount,
                                                                                                                                                               int maxAllowedFailureCount,
                                                                                                                                                               AtomicInteger failureCount,
                                                                                                                                                               int statusCode,
                                                                                                                                                               int subStatusCode) {
        return (request, storeResponse) -> {

            if (OperationType.Read.equals(request.getOperationType()) && regionName.equals(request.requestContext.regionalRoutingContextToRoute.getGatewayRegionalEndpoint().toString())) {
                long globalLsn = Long.parseLong(storeResponse.getHeaderValue(WFConstants.BackendHeaders.LSN));
                long itemLsn = Long.parseLong(storeResponse.getHeaderValue(WFConstants.BackendHeaders.ITEM_LSN));
                long manipulatedGlobalCommittedLSN = Math.min(globalLsn, itemLsn) - 1;
                long manipulatedGlobalLsn = itemLsn - 1;

                // Force barrier by setting GCLSN to be less than both local LSN and item LSN - applicable to strong consistency reads
                storeResponse.setHeaderValue(WFConstants.BackendHeaders.GLOBAL_COMMITTED_LSN, String.valueOf(manipulatedGlobalCommittedLSN));

                // Force barrier by setting the (LSN - can correspond to GLSN when that particular document was part of a commit increment)
                // to less than itemLsn - applicable to bounded staleness consistency reads
                storeResponse.setHeaderValue(WFConstants.BackendHeaders.LSN, String.valueOf(manipulatedGlobalLsn));

                return storeResponse;
            }

            if (OperationType.Head.equals(request.getOperationType()) && regionName.equals(request.requestContext.regionalRoutingContextToRoute.getGatewayRegionalEndpoint().toString())) {

                if (successfulHeadRequestCount.incrementAndGet() <= allowedSuccessfulHeadRequestsWithoutBarrierBeingMet) {

                    long globalLsn = Long.parseLong(storeResponse.getHeaderValue(WFConstants.BackendHeaders.LSN));
                    long itemLsn = Long.parseLong(storeResponse.getHeaderValue(WFConstants.BackendHeaders.ITEM_LSN));
                    long manipulatedGlobalCommittedLSN = Math.min(globalLsn, itemLsn) - 1;
                    long manipulatedGlobalLsn = itemLsn - 1;

                    // Force barrier by setting GCLSN to be less than both local LSN and item LSN - applicable to strong consistency reads
                    storeResponse.setHeaderValue(WFConstants.BackendHeaders.GLOBAL_COMMITTED_LSN, String.valueOf(manipulatedGlobalCommittedLSN));

                    // Force barrier by setting the (LSN - can correspond to GLSN when that particular document was part of a commit increment)
                    // to less than itemLsn - applicable to bounded staleness consistency reads
                    storeResponse.setHeaderValue(WFConstants.BackendHeaders.LSN, String.valueOf(manipulatedGlobalLsn));

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
