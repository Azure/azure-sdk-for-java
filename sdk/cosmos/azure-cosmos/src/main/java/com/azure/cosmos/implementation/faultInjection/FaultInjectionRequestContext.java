// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.faultInjection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FaultInjectionRequestContext {
    private final Map<String, Integer> hitCountByRuleMap = new ConcurrentHashMap<>();
    private final List<IFaultInjectionRuleInternal> rules = new ArrayList<>();

    public void applyFaultInjectionRule(IFaultInjectionRuleInternal rule) {
        this.hitCountByRuleMap.compute(rule.getId(), (id, count) -> {
            if (count == null) {
                return 1;
            }

            return ++count;
        });

        this.rules.add(rule);
    }

    public int getFaultInjectionRuleApplyCount(String ruleId) {
        return this.hitCountByRuleMap.getOrDefault(ruleId, 0);
    }
}

