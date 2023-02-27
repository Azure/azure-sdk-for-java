// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.GoneException;
import com.azure.cosmos.implementation.InternalServerErrorException;
import com.azure.cosmos.implementation.RequestRateTooLargeException;
import com.azure.cosmos.implementation.RequestTimeoutException;
import com.azure.cosmos.implementation.RetryWithException;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;

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

    public CosmosException getInjectedServerError() {
        // TODO: add more error handling
        switch (this.serverErrorType) {
            case SERVER_GONE:
                GoneException goneException = new GoneException("Fault Injection SERVER_410");
                goneException.setIsBasedOn410ResponseFromService();
                return goneException;
            case SERVER_RETRY_WITH:
                return new RetryWithException("FaultInjection SERVER_449", null);
            case TOO_MANY_REQUEST:
                return new RequestRateTooLargeException("FaultInjection TOO_MANY_REQUEST", null);
            case SERVER_TIMEOUT:
                return new RequestTimeoutException("FaultInjection SERVER_TIMEOUT", null);
            case INTERNAL_SERVER_ERROR:
                return new InternalServerErrorException("FaultInjection INTERNAL_SERVER_ERROR");
            default:
                throw new IllegalArgumentException("Server error type " + this.serverErrorType + " is not supported");
        }
    }
}
