// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.perPartitionCircuitBreaker;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Strings;
import com.azure.cosmos.implementation.guava25.base.Preconditions;

import java.util.HashMap;
import java.util.Map;

public class PerPartitionCircuitBreakerUtilities {

    private static final Map<String, String> EMPTY_MAP = new HashMap<>();
    private static final String BASE_EXCEPTION_MESSAGE = "FAILED IN Per-Partition Circuit Breaker: ";

    public static void checkNotNull(
        RxDocumentServiceRequest request,
        Object paramValue,
        String exceptionMessage) {

        try {
            Preconditions.checkNotNull(paramValue, exceptionMessage);
        } catch (Exception e) {

            CosmosDiagnostics cosmosDiagnostics = null;
            String resourceAddress = null;

            if (request != null) {

                if (request.requestContext != null) {
                    cosmosDiagnostics = request.requestContext.cosmosDiagnostics;
                    resourceAddress = request.requestContext.resourcePhysicalAddress != null
                        ? request.requestContext.resourcePhysicalAddress
                        : "N/A";
                }
            }

            CosmosException cosmosException = BridgeInternal.createCosmosException(
                BASE_EXCEPTION_MESSAGE + exceptionMessage,
                e,
                EMPTY_MAP,
                HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR,
                resourceAddress);

            cosmosException = BridgeInternal.setCosmosDiagnostics(cosmosException, cosmosDiagnostics);
            BridgeInternal.setSubStatusCode(cosmosException, HttpConstants.SubStatusCodes.PPCB_INVALID_STATE);

            throw cosmosException;
        }
    }
}
