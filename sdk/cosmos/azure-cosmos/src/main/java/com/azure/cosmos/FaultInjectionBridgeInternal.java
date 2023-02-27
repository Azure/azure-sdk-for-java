// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.models.FaultInjectionRule;
import reactor.core.publisher.Mono;

import java.util.List;


/**
 * ONLY USED FOR TESTING.
 * This is meant to be used only for fault injection during testing.
 **/
public final class FaultInjectionBridgeInternal {
    private FaultInjectionBridgeInternal() {}

    public static Mono<Void> configFaultInjectionRules(CosmosAsyncContainer cosmosAsyncContainer, List<FaultInjectionRule> rules) {
        return cosmosAsyncContainer.configFaultInjectionRules(rules);
    }

    public static void configFaultInjectionRules(CosmosContainer cosmosContainer, List<FaultInjectionRule> rules) {
        cosmosContainer.configFaultInjectionRules(rules);
    }
}
