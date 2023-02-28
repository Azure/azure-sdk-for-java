// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.faultinjection;

import com.azure.cosmos.implementation.faultinjection.model.IFaultInjectionRuleInternal;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FaultInjectionRequestContext {
    private final Map<String, Integer> hitCountByRuleMap = new ConcurrentHashMap<>();
    private final Map<UUID, String> activityIdAndRuleMap = new ConcurrentHashMap<>();

    public void applyFaultInjectionRule(UUID requestActivityId, IFaultInjectionRuleInternal rule) {
        this.hitCountByRuleMap.compute(rule.getId(), (id, count) -> {
            if (count == null) {
                return 1;
            }

            return ++count;
        });

        this.activityIdAndRuleMap.put(requestActivityId, rule.getId());
    }

    public int getFaultInjectionRuleApplyCount(String ruleId) {
        return this.hitCountByRuleMap.getOrDefault(ruleId, 0);
    }
    public String getFaultInjectionRuleId(UUID activityId) { return this.activityIdAndRuleMap.getOrDefault(activityId, null); }
}

