// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.faultinjection;

import com.azure.cosmos.implementation.routing.RegionalRoutingContext;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/***
 * Only used in fault injection.
 * It has two purposes:
 * 1. Keep track how many times a certain fault injection rule has applied on the operation.
 * 2. Track for each network request, which fault injection rule has applied.
 */
public class FaultInjectionRequestContext {
    private final Map<String, Integer> hitCountByRuleMap;
    private final Map<Long, String> transportRequestIdRuleIdMap;
    private final Map<Long, List<String>> transportRequestIdRuleEvaluationMap;
    private final AtomicBoolean addressForceRefreshed;

    private volatile RegionalRoutingContext regionalRoutingContextToRoute;

    /***
     * This usually is called during retries.
     * The hit count cap will need to be copied over so that the total times defined in error result be honored.
     * The transportRequestIdRuleIdMap can be re-initialized as all the required diagnostics has been recorded.
     *
     * @param cloneContext the previous fault injection context.
     */
    public FaultInjectionRequestContext(FaultInjectionRequestContext cloneContext) {
        this.hitCountByRuleMap = cloneContext.hitCountByRuleMap;
        this.transportRequestIdRuleIdMap = new ConcurrentHashMap<>();
        this.transportRequestIdRuleEvaluationMap = new ConcurrentHashMap<>();
        this.addressForceRefreshed = new AtomicBoolean(false);
    }

    public FaultInjectionRequestContext() {
        this.hitCountByRuleMap = new ConcurrentHashMap<>();
        this.transportRequestIdRuleIdMap = new ConcurrentHashMap<>();
        this.transportRequestIdRuleEvaluationMap = new ConcurrentHashMap<>();
        this.addressForceRefreshed = new AtomicBoolean(false);
    }

    public void applyFaultInjectionRule(long transportId, String ruleId) {
        this.hitCountByRuleMap.compute(ruleId, (id, count) -> {
            if (count == null) {
                return 1;
            }

            count++;
            return count;
        });

        this.transportRequestIdRuleIdMap.put(transportId, ruleId);
    }

    public void recordFaultInjectionRuleEvaluation(long transportId, String ruleEvaluationResult) {
        this.transportRequestIdRuleEvaluationMap.compute(transportId, (id, evaluations) -> {
            if (evaluations == null) {
                evaluations = new ArrayList<>();
            }

            evaluations.add(ruleEvaluationResult);
            return evaluations;
        });
    }

    public void recordAddressForceRefreshed(boolean forceRefreshed) {
        if (forceRefreshed) {
            this.addressForceRefreshed.compareAndSet(false, true);
        }
    }

    public boolean getAddressForceRefreshed() {
        return this.addressForceRefreshed.get();
    }

    public int getFaultInjectionRuleApplyCount(String ruleId) {
        if (this.hitCountByRuleMap.isEmpty()) {
            return 0;
        }

        return this.hitCountByRuleMap.getOrDefault(ruleId, 0);
    }
    public String getFaultInjectionRuleId(long transportRequestId) {
        if (this.transportRequestIdRuleIdMap.isEmpty()) {
            return null;
        }

        return this.transportRequestIdRuleIdMap.getOrDefault(transportRequestId, null);
    }

    public void setRegionalRoutingContextToRoute(RegionalRoutingContext regionalRoutingContextToRoute) {
        this.regionalRoutingContextToRoute = regionalRoutingContextToRoute;
    }

    public RegionalRoutingContext getRegionalRoutingContextToRoute() {
        return this.regionalRoutingContextToRoute;
    }

    public List<String> getFaultInjectionRuleEvaluationResults(long transportRequestId) {
        if (this.transportRequestIdRuleEvaluationMap.isEmpty()) {
            return null;
        }

        return this.transportRequestIdRuleEvaluationMap.getOrDefault(transportRequestId, null);
    }
}

