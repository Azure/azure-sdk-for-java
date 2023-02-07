// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.faultinjection.model;

public class RntbdFaultInjectionConnectionResetEvent {
    private String ruleId;
    public RntbdFaultInjectionConnectionResetEvent(String ruleId) {
        this.ruleId = ruleId;
    }
}
