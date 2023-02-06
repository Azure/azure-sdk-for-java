// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.faultInjection;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.GoneException;
import com.azure.cosmos.implementation.RequestRateTooLargeException;
import com.azure.cosmos.implementation.RequestTimeoutException;
import com.azure.cosmos.implementation.RetryWithException;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.directconnectivity.StoreResponse;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdRequestRecord;
import com.azure.cosmos.models.FaultInjectionServerErrorType;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RntbdServerErrorInjector {
    private final Map<String, FaultInjectionServerErrorRule> ruleMap = new ConcurrentHashMap<>();
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    public Mono<Void> addFaultInjectionRule(FaultInjectionServerErrorRule rule) {
        this.ruleMap.put(rule.getRuleId(), rule);
        return Mono.empty();
    }

    public boolean applyRule(RntbdRequestRecord requestRecord, StoreResponse storeResponse) {
        RxDocumentServiceRequest request = requestRecord.args().serviceRequest();

        // TODO: add operation type, region, containerRid check
        for (FaultInjectionServerErrorRule rule : ruleMap.values()) {
            if (rule.isValid()
                && request.faultInjectionRequestContext.getFaultInjectionRuleApplyCount(rule.getRuleId()) < rule.getResult().getTimes()) {

                if (rule.getResult().getServerErrorType() == FaultInjectionServerErrorType.SERVER_DELAY) {
                    executorService.schedule(
                        () -> requestRecord.complete(storeResponse),
                        rule.getResult().getDelay().toMillis(),
                        TimeUnit.MILLISECONDS);
                    request.faultInjectionRequestContext.applyFaultInjectionRule(rule);
                    return true;
                } else {
                    boolean applyRule = applyRuleCore(requestRecord, rule.getResult().getServerErrorType(), rule);
                    if (applyRule) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public boolean applyRule(RntbdRequestRecord requestRecord, CosmosException cause) {
        RxDocumentServiceRequest request = requestRecord.args().serviceRequest();

        for (FaultInjectionServerErrorRule rule : ruleMap.values()) {
            if (rule.isValid()
                && request.faultInjectionRequestContext.getFaultInjectionRuleApplyCount(rule.getRuleId()) < rule.getResult().getTimes()) {
                if (rule.getResult().getServerErrorType() == FaultInjectionServerErrorType.SERVER_DELAY) {
                    executorService.schedule(
                        () -> requestRecord.completeExceptionally(cause),
                        rule.getResult().getDelay().toMillis(),
                        TimeUnit.MILLISECONDS);
                    request.faultInjectionRequestContext.applyFaultInjectionRule(rule);
                    return true;
                } else {
                    boolean applyRule = applyRuleCore(requestRecord, rule.getResult().getServerErrorType(), rule);
                    if (applyRule) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean applyRuleCore(
        RntbdRequestRecord requestRecord,
        FaultInjectionServerErrorType errorType,
        FaultInjectionServerErrorRule rule) {
        RxDocumentServiceRequest request = requestRecord.args().serviceRequest();

        switch (errorType) {
            case SERVER_410:
                GoneException goneException = new GoneException("Fault Injection SERVER_410");
                goneException.setIsBasedOn410ResponseFromService();
                requestRecord.completeExceptionally(goneException);
                request.faultInjectionRequestContext.applyFaultInjectionRule(rule);
                return true;
            case SERVER_449:
                requestRecord.completeExceptionally(new RetryWithException("FaultInjection SERVER_449", null));
                request.faultInjectionRequestContext.applyFaultInjectionRule(rule);
                return true;
            case TOO_MANY_REQUEST:
                requestRecord.completeExceptionally(new RequestRateTooLargeException("FaultInjection TOO_MANY_REQUEST", null));
                request.faultInjectionRequestContext.applyFaultInjectionRule(rule);
                return true;
            case SERVER_TIMEOUT:
                requestRecord.completeExceptionally(new RequestTimeoutException("FaultInjection SERVER_TIMEOUT", null));
                request.faultInjectionRequestContext.applyFaultInjectionRule(rule);
                return true;
            default:
                return false;
        }
    }
}
