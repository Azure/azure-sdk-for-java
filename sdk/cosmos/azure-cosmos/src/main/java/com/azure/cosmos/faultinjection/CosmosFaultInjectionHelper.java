// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.faultinjection;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.Warning;
import com.azure.cosmos.util.Beta;
import reactor.core.publisher.Mono;

import java.util.List;

import static com.azure.cosmos.implementation.Warning.FAULT_INJECTION_TEST_USE_ONLY_WARNING;


@Warning(value = FAULT_INJECTION_TEST_USE_ONLY_WARNING)
@Beta(value = Beta.SinceVersion.V4_42_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public final class CosmosFaultInjectionHelper {
    @Warning(value = FAULT_INJECTION_TEST_USE_ONLY_WARNING)
    @Beta(value = Beta.SinceVersion.V4_42_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public static Mono<Void> configureFaultInjectionRules(CosmosAsyncContainer cosmosAsyncContainer, List<FaultInjectionRule> rules) {
        return ImplementationBridgeHelpers
            .CosmosAsyncContainerHelper
            .getCosmosAsyncContainerAccessor()
            .configureFaultInjectionRules(cosmosAsyncContainer, rules);
    }
}
