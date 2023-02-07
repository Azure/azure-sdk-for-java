// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.faultinjection;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.GoneException;
import com.azure.cosmos.implementation.RequestRateTooLargeException;
import com.azure.cosmos.implementation.RequestTimeoutException;
import com.azure.cosmos.implementation.RetryWithException;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.directconnectivity.StoreResponse;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdRequestRecord;
import com.azure.cosmos.implementation.faultinjection.model.FaultInjectionServerErrorRule;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RntbdServerErrorInjector {

    // TODO: should the key be condition string?
    private final Map<String, FaultInjectionServerErrorRule> serverLatencyRuleMap = new ConcurrentHashMap<>();
    private final Map<String, FaultInjectionServerErrorRule> serverConnectionRuleMap = new ConcurrentHashMap<>();
    private final Map<String, FaultInjectionServerErrorRule> serverResponseErrorMap = new ConcurrentHashMap<>();

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    public void configFaultInjectionRule(FaultInjectionServerErrorRule rule) {
        switch (rule.getResult().getServerErrorType()) {
            case SERVER_CONNECTION_UNRESPONSIVE:
                this.serverConnectionRuleMap.put(rule.getId(), rule);
                break;
            case SERVER_DELAY:
                this.serverLatencyRuleMap.put(rule.getId(), rule);
                break;
            default:
                this.serverResponseErrorMap.put(rule.getId(), rule);
                break;
        }
    }

    public boolean applyServerLatencyRule(RntbdRequestRecord requestRecord, StoreResponse storeResponse) {
        RxDocumentServiceRequest request = requestRecord.args().serviceRequest();

        for (FaultInjectionServerErrorRule latencyRule : this.serverLatencyRuleMap.values()) {
            if (latencyRule.isApplicable(request)) {
                this.executorService.schedule(
                    () -> requestRecord.complete(storeResponse),
                    latencyRule.getResult().getDelay().toMillis(),
                    TimeUnit.MILLISECONDS);
                request.faultInjectionRequestContext.applyFaultInjectionRule(latencyRule);
                return true;
            }
        }

        return false;
    }

    public boolean applyServerResponseErrorRule(RntbdRequestRecord requestRecord) {
        RxDocumentServiceRequest request = requestRecord.args().serviceRequest();
        for (FaultInjectionServerErrorRule serverResponseErrorRule : this.serverResponseErrorMap.values()) {
            if (serverResponseErrorRule.isApplicable(request)) {

                // TODO: add more error handling
                final CosmosException cause;
                switch (serverResponseErrorRule.getResult().getServerErrorType()) {
                    case SERVER_GONE:
                        GoneException goneException = new GoneException("Fault Injection SERVER_410");
                        goneException.setIsBasedOn410ResponseFromService();
                        cause = goneException;
                        break;
                    case SERVER_RETRY_WITH:
                        cause = new RetryWithException("FaultInjection SERVER_449", null);
                        break;
                    case TOO_MANY_REQUEST:
                        cause = new RequestRateTooLargeException("FaultInjection TOO_MANY_REQUEST", null);
                        break;
                    case SERVER_TIMEOUT:
                        cause = new RequestTimeoutException("FaultInjection SERVER_TIMEOUT", null);
                        break;
                    default:
                        return false;
                }

                requestRecord.completeExceptionally(cause);
                request.faultInjectionRequestContext.applyFaultInjectionRule(serverResponseErrorRule);
                return true;
            }
        }

        return false;
    }

    // TODO: Add implementation for server non-responsive during connection establishment
}
