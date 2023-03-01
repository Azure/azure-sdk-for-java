// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.faultinjection;

import com.azure.cosmos.implementation.faultinjection.model.IFaultInjectionRuleInternal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FaultInjectionRequestContext {
    private final Map<String, Integer> hitCountByRuleMap;
    private final Map<Long, String> transportRequestIdRuleIdMap;

    public FaultInjectionRequestContext(FaultInjectionRequestContext cloneContext) {
        this.hitCountByRuleMap = cloneContext.hitCountByRuleMap;
        this.transportRequestIdRuleIdMap = new ConcurrentHashMap<>();
    }

    public FaultInjectionRequestContext() {
        this.hitCountByRuleMap = new ConcurrentHashMap<>();
        this.transportRequestIdRuleIdMap = new ConcurrentHashMap<>();
    }

    public void applyFaultInjectionRule(long transportId, IFaultInjectionRuleInternal rule) {
        this.hitCountByRuleMap.compute(rule.getId(), (id, count) -> {
            if (count == null) {
                return 1;
            }

            count++;
            return count;
        });

        this.transportRequestIdRuleIdMap.put(transportId, rule.getId());
    }

    public int getFaultInjectionRuleApplyCount(String ruleId) {
        return this.hitCountByRuleMap.getOrDefault(ruleId, 0);
    }
    public String getFaultInjectionRuleId(long transportRequesetId) {
        return this.transportRequestIdRuleIdMap.getOrDefault(transportRequesetId, null); }
}

