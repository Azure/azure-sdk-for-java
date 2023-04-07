// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.test.implementation.faultinjection;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.GoneException;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.InternalServerErrorException;
import com.azure.cosmos.implementation.NotFoundException;
import com.azure.cosmos.implementation.PartitionIsMigratingException;
import com.azure.cosmos.implementation.PartitionKeyRangeIsSplittingException;
import com.azure.cosmos.implementation.RMResources;
import com.azure.cosmos.implementation.RequestRateTooLargeException;
import com.azure.cosmos.implementation.RequestTimeoutException;
import com.azure.cosmos.implementation.RetryWithException;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.directconnectivity.WFConstants;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorType;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;

public class FaultInjectionServerErrorResultInternal {
    private final FaultInjectionServerErrorType serverErrorType;
    private final Integer times;
    private final Duration delay;

    private final Boolean suppressServiceRequests;


    public FaultInjectionServerErrorResultInternal(
        FaultInjectionServerErrorType serverErrorTypes,
        Integer times,
        Duration delay,
        Boolean suppressServiceRequests) {

        checkArgument(serverErrorTypes != null, "Argument 'serverErrorType' can not be null");
        this.serverErrorType = serverErrorTypes;
        this.times = times;
        this.delay = delay;
        this.suppressServiceRequests = suppressServiceRequests;
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

    public Boolean getSuppressServiceRequests() {
        return this.suppressServiceRequests;
    }

    public boolean isApplicable(String ruleId, RxDocumentServiceRequest request) {
        return this.times == null || request.faultInjectionRequestContext.getFaultInjectionRuleApplyCount(ruleId) < this.times;
    }

    public CosmosException getInjectedServerError(RxDocumentServiceRequest request) {

        CosmosException cosmosException;
        long lsn = Long.parseLong(request.getHeaders().getOrDefault(HttpConstants.HttpHeaders.LSN, "0"));
        String partitionKeyRangeId = request.getHeaders().getOrDefault(HttpConstants.HttpHeaders.PARTITION_KEY_RANGE_ID, null);
        Map<String, String> responseHeaders = this.getInjectedErrorResponseHeaders(request, lsn, partitionKeyRangeId);

        switch (this.serverErrorType) {
            case GONE:
                GoneException goneException = new GoneException(this.getErrorMessage(RMResources.Gone));
                goneException.setIsBasedOn410ResponseFromService();
                cosmosException = goneException;
                break;

            case RETRY_WITH:
                cosmosException = new RetryWithException(null, lsn, partitionKeyRangeId, responseHeaders);
                break;

            case TOO_MANY_REQUEST:
                responseHeaders.put(
                    HttpConstants.HttpHeaders.RETRY_AFTER_IN_MILLISECONDS,
                    String.valueOf(500));
                cosmosException = new RequestRateTooLargeException(null, lsn, partitionKeyRangeId, responseHeaders);

                break;

            case TIMEOUT:
                cosmosException = new RequestTimeoutException(null, lsn, partitionKeyRangeId, responseHeaders);
                break;

            case INTERNAL_SERVER_ERROR:
                cosmosException = new InternalServerErrorException(null, lsn, partitionKeyRangeId, responseHeaders);
                break;

            case READ_SESSION_NOT_AVAILABLE:
                responseHeaders.put(WFConstants.BackendHeaders.SUB_STATUS,
                    Integer.toString(HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE));
                cosmosException = new NotFoundException(null, lsn, partitionKeyRangeId, responseHeaders);
                break;

            case PARTITION_IS_MIGRATING:
                responseHeaders.put(WFConstants.BackendHeaders.SUB_STATUS,
                    Integer.toString(HttpConstants.SubStatusCodes.COMPLETING_PARTITION_MIGRATION));
                cosmosException = new PartitionIsMigratingException(null, lsn, partitionKeyRangeId, responseHeaders);
                break;

            case PARTITION_IS_SPLITTING:
                responseHeaders.put(WFConstants.BackendHeaders.SUB_STATUS,
                    Integer.toString(HttpConstants.SubStatusCodes.COMPLETING_SPLIT_OR_MERGE));
                cosmosException = new PartitionKeyRangeIsSplittingException(null, lsn, partitionKeyRangeId, responseHeaders);
                break;

            default:
                throw new IllegalArgumentException("Server error type " + this.serverErrorType + " is not supported");
        }

        return cosmosException;
    }

    private Map<String, String> getInjectedErrorResponseHeaders(
        RxDocumentServiceRequest request,
        long lsn,
        String partitionKeyRangeId) {
        Map<String, String> responseHeaders = new ConcurrentHashMap<>();
        String activityId = request.getActivityId().toString();
        String sessionToken = request.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN);

        responseHeaders.put(WFConstants.BackendHeaders.LOCAL_LSN, String.valueOf(lsn));
        if (StringUtils.isNotEmpty(partitionKeyRangeId)) {
            responseHeaders.put(HttpConstants.HttpHeaders.PARTITION_KEY_RANGE_ID, partitionKeyRangeId);
        }
        if (StringUtils.isNotEmpty(activityId)) {
            responseHeaders.put(HttpConstants.HttpHeaders.ACTIVITY_ID, activityId);
        }
        if (StringUtils.isNotEmpty(sessionToken)) {
            responseHeaders.put(HttpConstants.HttpHeaders.SESSION_TOKEN, sessionToken);
        }

        return responseHeaders;
    }

    private String getErrorMessage(String errorMessage) {
        return String.format("Fault injection server error [%s]", errorMessage);
    }
}
