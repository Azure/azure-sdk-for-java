// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.faultinjection;

import com.azure.cosmos.implementation.CosmosSchedulers;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RntbdConnectionErrorInjector {
    private static final Logger logger = LoggerFactory.getLogger(RntbdConnectionErrorInjector.class);

    private final Map<String, FaultInjectionConnectionErrorRule> ruleMap = new ConcurrentHashMap<>();
    private final RntbdEndpoint.Provider endpointProvider;

    public RntbdConnectionErrorInjector(RntbdEndpoint.Provider endpointProvider) {
        this.endpointProvider = endpointProvider;
    }

    public Mono<Void> addFaultInjectionRule(FaultInjectionConnectionErrorRule rule) {
        this.ruleMap.put(rule.getRuleId(), rule);
        CosmosSchedulers.FAULT_INJECTION_CONNECTION_ERROR_BOUNDED_ELASTIC.schedule(
            () -> this.injectConnectionErrorTask(rule).subscribe()
        );

        return Mono.empty();
    }

    public Mono<Void> injectConnectionErrorTask(FaultInjectionConnectionErrorRule rule) {
        return Mono.delay(rule.getResult().getInterval())
            .flatMapMany(t -> {
                //check whether the rule still valid
                if (this.ruleMap.containsKey(rule.getRuleId()) && rule.isValid()) {
                    return Flux.fromIterable(rule.getAddresses())
                        .flatMap(addressUri -> {
                            this.endpointProvider.get(addressUri.getURI()).injectConnectionErrors(rule.getRuleId(), rule.getResult());
                            return Mono.empty();
                        });
                }

                return Mono.empty();
            })
            .onErrorResume(throwable -> {
                logger.warn("Inject connection error failed due to", throwable);
                return Mono.empty();
            })
            .repeat(() -> this.ruleMap.containsKey(rule.getRuleId()) && rule.isValid())
            .then();
    }
}
