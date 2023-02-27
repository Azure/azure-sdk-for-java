// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.faultinjection;

import com.azure.cosmos.implementation.faultinjection.model.IFaultInjectionRuleInternal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FaultInjectionRequestContext {
    private final Map<String, Integer> hitCountByRuleMap = new ConcurrentHashMap<>();

    public void applyFaultInjectionRule(IFaultInjectionRuleInternal rule) {
        this.hitCountByRuleMap.compute(rule.getId(), (id, count) -> {
            if (count == null) {
                return 1;
            }

            return ++count;
        });
    }

    public int getFaultInjectionRuleApplyCount(String ruleId) {
        return this.hitCountByRuleMap.getOrDefault(ruleId, 0);
    }
}

