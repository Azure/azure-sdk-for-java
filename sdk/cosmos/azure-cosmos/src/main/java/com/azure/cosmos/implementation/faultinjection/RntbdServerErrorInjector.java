// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.faultinjection;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdRequestRecord;
import com.azure.cosmos.implementation.faultinjection.model.FaultInjectionServerErrorResultInternal;
import com.azure.cosmos.implementation.faultinjection.model.FaultInjectionServerErrorRule;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

public class RntbdServerErrorInjector {

    private final Map<String, FaultInjectionServerErrorRule> serverLatencyRuleMap = new ConcurrentHashMap<>();
    private final Map<String, FaultInjectionServerErrorRule> serverConnectionLatencyRuleMap = new ConcurrentHashMap<>();
    private final Map<String, FaultInjectionServerErrorRule> serverResponseErrorMap = new ConcurrentHashMap<>();

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    public void configFaultInjectionRule(FaultInjectionServerErrorRule rule) {
        FaultInjectionServerErrorResultInternal serverErrorResult = rule.getResult();
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

    public boolean applyServerResponseLatencyRule(
        RntbdRequestRecord requestRecord,
        Consumer<Duration> writeRequestWithDelayConsumer) {

        RxDocumentServiceRequest request = requestRecord.args().serviceRequest();

        for (FaultInjectionServerErrorRule latencyRule : this.serverLatencyRuleMap.values()) {
            if (latencyRule.isApplicable(request)) {
                request.faultInjectionRequestContext.applyFaultInjectionRule(requestRecord.transportRequestId(), latencyRule);

                writeRequestWithDelayConsumer.accept(latencyRule.getResult().getDelay());
                return true;
            }
        }

        return false;
    }

    public boolean applyServerResponseErrorRule(RntbdRequestRecord requestRecord) {
        RxDocumentServiceRequest request = requestRecord.args().serviceRequest();

        for (FaultInjectionServerErrorRule serverResponseErrorRule : this.serverResponseErrorMap.values()) {
            if (serverResponseErrorRule.isApplicable(request)) {
                request.faultInjectionRequestContext.applyFaultInjectionRule(requestRecord.transportRequestId(), serverResponseErrorRule);

                CosmosException cause = serverResponseErrorRule.getInjectedServerError(request);
                requestRecord.completeExceptionally(cause);
                return true;
            }
        }

        return false;
    }

    public boolean applyServerConnectionDelayRule(
        RntbdRequestRecord requestRecord,
        Consumer<Duration> openConnectionWithDelayConsumer) {

        if (requestRecord == null) {
            return false;
        }

        RxDocumentServiceRequest request = requestRecord.args().serviceRequest();

        for (FaultInjectionServerErrorRule connectionDelayRule : this.serverConnectionLatencyRuleMap.values()) {
            if (connectionDelayRule.isApplicable(request)) {
                request.faultInjectionRequestContext.applyFaultInjectionRule(requestRecord.transportRequestId(), connectionDelayRule);

                openConnectionWithDelayConsumer.accept(connectionDelayRule.getResult().getDelay());

                return true;
            }
        }

        return false;
    }
}
