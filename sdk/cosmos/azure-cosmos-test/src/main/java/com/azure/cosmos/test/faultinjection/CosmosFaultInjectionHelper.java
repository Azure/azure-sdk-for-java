// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.


package com.azure.cosmos.test.faultinjection;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.test.implementation.faultinjection.FaultInjectorProvider;
import reactor.core.publisher.Mono;

import java.util.List;

/***
 * Cosmos fault injection helper.
 */
public class CosmosFaultInjectionHelper {

    /***
     * Configure fault injection rules.
     *
     * @param container the container.
     * @param rules the fault injection rules.
     * @return the Mono.
     */
    public static Mono<Void> configureFaultInjectionRules(CosmosAsyncContainer container, List<FaultInjectionRule> rules) {
        FaultInjectorProvider injectorProvider =
            (FaultInjectorProvider) ImplementationBridgeHelpers
                .CosmosAsyncContainerHelper
                .getCosmosAsyncContainerAccessor()
                .getOrConfigureFaultInjectorProvider(container, () -> new FaultInjectorProvider(container));

        return injectorProvider.configureFaultInjectionRules(rules);
    }
}
