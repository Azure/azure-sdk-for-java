// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.faultinjection.model;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.GoneException;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.InternalServerErrorException;
import com.azure.cosmos.implementation.NotFoundException;
import com.azure.cosmos.implementation.RMResources;
import com.azure.cosmos.implementation.RequestRateTooLargeException;
import com.azure.cosmos.implementation.RequestTimeoutException;
import com.azure.cosmos.implementation.RetryWithException;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.directconnectivity.WFConstants;
import com.azure.cosmos.models.FaultInjectionServerErrorType;

import java.time.Duration;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;

public class FaultInjectionServerErrorResultInternal {
    private final FaultInjectionServerErrorType serverErrorType;
    private final Integer times;
    private final Duration delay;

    public FaultInjectionServerErrorResultInternal(
        FaultInjectionServerErrorType serverErrorTypes,
        Integer times,
        Duration delay) {

        checkArgument(serverErrorTypes != null, "Argument 'serverErrorType' can not be null");
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

    public CosmosException getInjectedServerError(RxDocumentServiceRequest request) {

        CosmosException cosmosException;
        switch (this.serverErrorType) {
            case SERVER_GONE:
                GoneException goneException = new GoneException(this.getErrorMessage(RMResources.Gone));
                goneException.setIsBasedOn410ResponseFromService();
                cosmosException = goneException;
                break;

            case SERVER_RETRY_WITH:
                cosmosException =
                    new RetryWithException(
                        this.getErrorMessage(RMResources.RetryWith),
                        request.requestContext.storePhysicalAddress);
                break;

            case TOO_MANY_REQUEST:
                cosmosException =
                    new RequestRateTooLargeException(
                        this.getErrorMessage(RMResources.TooManyRequests),
                        request.requestContext.storePhysicalAddress);
                cosmosException.getResponseHeaders().put(
                    HttpConstants.HttpHeaders.RETRY_AFTER_IN_MILLISECONDS,
                    String.valueOf(500));
                break;

            case SERVER_TIMEOUT:
                cosmosException =
                    new RequestTimeoutException(
                        this.getErrorMessage(RMResources.RequestTimeout),
                        request.requestContext.storePhysicalAddress);
                break;

            case INTERNAL_SERVER_ERROR:
                cosmosException =
                    new InternalServerErrorException(this.getErrorMessage(RMResources.InternalServerError));
                break;

            case READ_SESSION_NOT_AVAILABLE:
                cosmosException = new NotFoundException();
                cosmosException.getResponseHeaders().put(WFConstants.BackendHeaders.SUB_STATUS,
                    Integer.toString(HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE));
                break;

            default:
                throw new IllegalArgumentException("Server error type " + this.serverErrorType + " is not supported");
        }

        return cosmosException;
    }

    private String getErrorMessage(String errorMessage) {
        return String.format("Fault injection server error [%s]", errorMessage);
    }
}
