// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.faultinjection;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;

public class RntbdFaultInjectionConnectionCloseEvent {
    private final String faultInjectionRuleId;

    public RntbdFaultInjectionConnectionCloseEvent(String faultInjectionRuleId) {
        checkArgument(StringUtils.isNotEmpty(faultInjectionRuleId), "Argument 'faultInjectionRuleId' can not be null nor empty");
        this.faultInjectionRuleId = faultInjectionRuleId;
    }

    public String getFaultInjectionRuleId() {
        return this.faultInjectionRuleId;
    }
}
