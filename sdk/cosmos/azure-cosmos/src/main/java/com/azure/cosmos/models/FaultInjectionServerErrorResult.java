// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.GoneException;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.InternalServerErrorException;
import com.azure.cosmos.implementation.RequestRateTooLargeException;
import com.azure.cosmos.implementation.RequestTimeoutException;
import com.azure.cosmos.implementation.RetryWithException;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.directconnectivity.WFConstants;

import java.time.Duration;

public class FaultInjectionServerErrorResult implements IFaultInjectionResult{
    private final FaultInjectionServerErrorType serverErrorType;
    private final Integer times;
    private final Duration delay;

    public FaultInjectionServerErrorResult(FaultInjectionServerErrorType serverErrorTypes, Integer times, Duration delay) {
        this.serverErrorType = serverErrorTypes;
        this.times = times;
        this.delay = delay;
    }

    public FaultInjectionServerErrorType getServerErrorType() {
        return serverErrorType;
    }

    public Integer getTimes() {
        return times;
    }

    public Duration getDelay() {
        return delay;
    }

    public boolean isApplicable(String ruleId, RxDocumentServiceRequest request) {
        return this.times == null || request.faultInjectionRequestContext.getFaultInjectionRuleApplyCount(ruleId) < this.times;
    }

    public CosmosException getInjectedServerError(RxDocumentServiceRequest request, String ruleId) {

        CosmosException cosmosException;
        // TODO: add more error handling
        switch (this.serverErrorType) {
            case SERVER_GONE:
                GoneException goneException = new GoneException(this.getErrorMessage("SERVER_GONE", ruleId));
                goneException.setIsBasedOn410ResponseFromService();
                cosmosException = goneException;
                break;
            case SERVER_RETRY_WITH:
                cosmosException = new RetryWithException(
                    this.getErrorMessage("SERVER_449", ruleId),
                    request.requestContext.storePhysicalAddress);
                break;
            case TOO_MANY_REQUEST:
                cosmosException = new RequestRateTooLargeException(
                    this.getErrorMessage("TOO_MANY_REQUEST", ruleId),
                    request.requestContext.storePhysicalAddress);
                break;
            case SERVER_TIMEOUT:
                cosmosException = new RequestTimeoutException(
                    this.getErrorMessage("SERVER_TIMEOUT", ruleId),
                    request.requestContext.storePhysicalAddress);
                break;
            case INTERNAL_SERVER_ERROR:
                cosmosException = new InternalServerErrorException(
                    this.getErrorMessage("INTERNAL_SERVER_ERROR", ruleId));
                break;
            default:
                throw new IllegalArgumentException("Server error type " + this.serverErrorType + " is not supported");
        }

        cosmosException.getResponseHeaders().put(WFConstants.BackendHeaders.SUB_STATUS,
            Integer.toString(HttpConstants.SubStatusCodes.FAULT_INJECTION_ERROR));

        return cosmosException;
    }

    private String getErrorMessage(String errorMessage, String ruleId) {
        return String.format("Fault injection server error [%s], ruleId [%s]", errorMessage, ruleId);
    }
}
