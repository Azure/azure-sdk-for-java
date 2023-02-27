// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.faultinjection;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.directconnectivity.StoreResponse;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdRequestRecord;
import com.azure.cosmos.implementation.faultinjection.model.FaultInjectionServerErrorRule;
import com.azure.cosmos.models.FaultInjectionServerErrorResult;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class RntbdServerErrorInjector {

    // TODO: should the key be condition string?
    private final Map<String, FaultInjectionServerErrorRule> serverLatencyRuleMap = new ConcurrentHashMap<>();
    private final Map<String, FaultInjectionServerErrorRule> serverConnectionLatencyRuleMap = new ConcurrentHashMap<>();
    private final Map<String, FaultInjectionServerErrorRule> serverResponseErrorMap = new ConcurrentHashMap<>();

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    public void configFaultInjectionRule(FaultInjectionServerErrorRule rule) {
        FaultInjectionServerErrorResult serverErrorResult = rule.getResult();
        switch (serverErrorResult.getServerErrorType()) {
            case SERVER_CONNECTION_DELAY:
                this.serverConnectionLatencyRuleMap.put(rule.getId(), rule);
                break;
            case SERVER_RESPONSE_DELAY:
                this.serverLatencyRuleMap.put(rule.getId(), rule);
                break;
            default:
                this.serverResponseErrorMap.put(rule.getId(), rule);
                break;
        }
    }

    public boolean applyServerResponseLatencyRule(RntbdRequestRecord requestRecord, StoreResponse storeResponse) {
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

    public boolean applyServerResponseLatencyRule(RntbdRequestRecord requestRecord, CosmosException cosmosException) {
        RxDocumentServiceRequest request = requestRecord.args().serviceRequest();

        for (FaultInjectionServerErrorRule latencyRule : this.serverLatencyRuleMap.values()) {
            if (latencyRule.isApplicable(request)) {
                this.executorService.schedule(
                    () -> requestRecord.completeExceptionally(cosmosException),
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
                CosmosException cause = serverResponseErrorRule.getInjectedServerError();
                requestRecord.completeExceptionally(cause);
                request.faultInjectionRequestContext.applyFaultInjectionRule(serverResponseErrorRule);
                return true;
            }
        }

        return false;
    }

    public boolean applyServerConnectionLatencyErrorRule(
        RntbdRequestRecord requestRecord,
        Consumer<Duration> openConnectionWithFaultInjection) {

        RxDocumentServiceRequest request = requestRecord.args().serviceRequest();

        for (FaultInjectionServerErrorRule serverConnectionLatencyErrorRule : this.serverConnectionLatencyRuleMap.values()) {
            if (serverConnectionLatencyErrorRule.isApplicable(request)) {
                openConnectionWithFaultInjection.accept(serverConnectionLatencyErrorRule.getResult().getDelay());
                request.faultInjectionRequestContext.applyFaultInjectionRule(serverConnectionLatencyErrorRule);
                return true;
            }
        }

        return false;
    }
}
