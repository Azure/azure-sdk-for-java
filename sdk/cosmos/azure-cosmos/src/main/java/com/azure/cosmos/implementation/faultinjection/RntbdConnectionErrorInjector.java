// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.faultinjection;

import com.azure.cosmos.implementation.CosmosSchedulers;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdEndpoint;
import com.azure.cosmos.implementation.faultinjection.model.FaultInjectionConnectionErrorRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class RntbdConnectionErrorInjector {
    private static final Logger logger = LoggerFactory.getLogger(RntbdConnectionErrorInjector.class);

    private final Map<String, FaultInjectionConnectionErrorRule> ruleMap = new ConcurrentHashMap<>();
    private final RntbdEndpoint.Provider endpointProvider;

    public RntbdConnectionErrorInjector(RntbdEndpoint.Provider endpointProvider) {
        this.endpointProvider = endpointProvider;
    }

    public void configFaultInjectionRule(FaultInjectionConnectionErrorRule rule) {
        this.ruleMap.computeIfAbsent(rule.getId(), ruleId -> {
            CosmosSchedulers.FAULT_INJECTION_CONNECTION_ERROR_BOUNDED_ELASTIC.schedule(
                () -> this.injectConnectionErrorTask(rule).subscribe()
            );

            return rule;
        });
    }

    public Mono<Void> injectConnectionErrorTask(FaultInjectionConnectionErrorRule rule) {

        return Mono.delay(rule.getResult().getInterval())
            .flatMapMany(t -> {
                //check whether the rule still valid
                if (this.isEffectiveRule(rule)) {

                    // Inject connect error to rntbd endpoint with matching physical addresses
                    if (rule.getAddresses() != null && rule.getAddresses().size() > 0) {
                        return Flux.fromIterable(rule.getAddresses())
                            .flatMap(addressURI -> {
                                RntbdEndpoint rntbdEndpoint = this.endpointProvider.get(addressURI);
                                if (rntbdEndpoint != null) {
                                    rntbdEndpoint.injectConnectionErrors(rule.getId(), rule.getResult());
                                }

                                return Mono.empty();
                            });
                    }

                    // There is no specific physical addresses being defined in the rule,
                    // Inject connect error to rntbd endpoint with matching region endpoint
                    if (rule.getRegionEndpoints() != null && rule.getRegionEndpoints().size() > 0) {
                        return Flux.fromIterable(rule.getRegionEndpoints())
                            .flatMap(regionEndpoint -> {
                                return Flux.fromIterable(
                                    this.endpointProvider
                                        .list()
                                        .filter(rntbdEndpoint -> regionEndpoint == rntbdEndpoint.serverKey())
                                        .collect(Collectors.toList())
                                    )
                                    .flatMap(rntbdEndpoint -> {
                                        rntbdEndpoint.injectConnectionErrors(rule.getId(), rule.getResult());
                                        return Mono.empty();
                                    });
                            });
                    }

                    // If we reach here,then it means there is no specific addresses/region specified on the rule
                    // Inject connect error all available rntbd endpoints
                    return Flux.fromIterable(this.endpointProvider.list().collect(Collectors.toList()))
                        .flatMap(rntbdEndpoint -> {
                            rntbdEndpoint.injectConnectionErrors(rule.getId(), rule.getResult());
                            return Mono.empty();
                        });
                }

                return Mono.empty();
            })
            .onErrorResume(throwable -> {
                logger.warn("Inject connection error for rule [{}] failed due to", rule.getId(), throwable);
                return Mono.empty();
            })
            .repeat(() -> this.isEffectiveRule(rule))
            .then()
            .doFinally(signalType -> {
                this.ruleMap.remove(rule.getId()); // important steps
            });
    }

    private boolean isEffectiveRule(FaultInjectionConnectionErrorRule rule) {
        return this.ruleMap.containsKey(rule.getId()) && rule.isValid();
    }
}
