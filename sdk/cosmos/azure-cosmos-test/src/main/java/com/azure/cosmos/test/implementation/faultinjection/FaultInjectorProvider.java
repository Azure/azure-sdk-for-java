// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.test.implementation.faultinjection;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdEndpoint;
import com.azure.cosmos.implementation.faultinjection.IFaultInjectorProvider;
import com.azure.cosmos.implementation.faultinjection.IRntbdServerErrorInjector;
import com.azure.cosmos.test.faultinjection.FaultInjectionRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/***
 * Fault injector provider.
 */
public class FaultInjectorProvider implements IFaultInjectorProvider {
    private final Logger logger = LoggerFactory.getLogger(FaultInjectorProvider.class);
    private final FaultInjectionRuleStore ruleStore;
    private final RntbdServerErrorInjector serverErrorInjector;
    private final String containerNameLink;

    private RntbdConnectionErrorInjector connectionErrorInjector;

    public FaultInjectorProvider(CosmosAsyncContainer cosmosAsyncContainer) {
        checkNotNull(cosmosAsyncContainer, "Argument 'cosmosAsyncContainer' can not be null");

        this.containerNameLink =
            Utils.trimBeginningAndEndingSlashes(BridgeInternal.extractContainerSelfLink(cosmosAsyncContainer));
        this.ruleStore = new FaultInjectionRuleStore(cosmosAsyncContainer);
        this.serverErrorInjector = new RntbdServerErrorInjector(this.ruleStore);
    }

    public Mono<Void> configureFaultInjectionRules(List<FaultInjectionRule> rules) {
        return Flux.fromIterable(rules)
            .flatMap(rule -> this.ruleStore.configureFaultInjectionRule(rule, this.containerNameLink))
            .doOnNext(effectiveRule -> {
                // Important step: this step will start the connection error injection task
                this.connectionErrorInjector.accept(effectiveRule);
            })
            .then();
    }

    @Override
    public IRntbdServerErrorInjector getRntbdServerErrorInjector() {
        return this.serverErrorInjector;
    }

    @Override
    public void registerConnectionErrorInjector(RntbdEndpoint.Provider provider) {
        this.connectionErrorInjector = new RntbdConnectionErrorInjector(provider, this.ruleStore);
    }
}
