// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.faultinjection.model;

public class RntbdFaultInjectionConnectionCloseEvent {
    private final String ruleId;

    public RntbdFaultInjectionConnectionCloseEvent(String ruleId) {
        this.ruleId = ruleId;
    }
}
