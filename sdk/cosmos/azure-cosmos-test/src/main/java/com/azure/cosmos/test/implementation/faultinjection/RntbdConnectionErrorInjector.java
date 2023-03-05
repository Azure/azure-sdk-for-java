// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.test.implementation.faultinjection;

import com.azure.cosmos.implementation.CosmosSchedulers;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdEndpoint;
import com.azure.cosmos.implementation.faultinjection.RntbdFaultInjectionConnectionCloseEvent;
import com.azure.cosmos.implementation.faultinjection.RntbdFaultInjectionConnectionResetEvent;
import com.azure.cosmos.test.faultinjection.FaultInjectionConnectionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/***
 * Fault injector which can handle {@link FaultInjectionConnectionErrorRule} with direct connection type.
 */
public class RntbdConnectionErrorInjector {
    private static final Logger logger = LoggerFactory.getLogger(RntbdConnectionErrorInjector.class);

    private final RntbdEndpoint.Provider endpointProvider;
    private final FaultInjectionRuleStore ruleStore;

    public RntbdConnectionErrorInjector(RntbdEndpoint.Provider endpointProvider, FaultInjectionRuleStore ruleStore) {
        checkNotNull(endpointProvider, "Argument 'endpointProvider' can not be null");
        checkNotNull(ruleStore, "Argument 'ruleStore' can not be null");

        this.endpointProvider = endpointProvider;
        this.ruleStore = ruleStore;
    }

    public boolean accept(IFaultInjectionRuleInternal rule) {
        if (rule.getConnectionType() == FaultInjectionConnectionType.DIRECT
            && (rule instanceof FaultInjectionConnectionErrorRule)) {

            CosmosSchedulers.FAULT_INJECTION_CONNECTION_ERROR_BOUNDED_ELASTIC.schedule(
                () -> this.injectConnectionErrorTask((FaultInjectionConnectionErrorRule) rule).subscribe()
            );

            return true;
        }

        return false;
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
                                    rntbdEndpoint.injectConnectionErrors(
                                        rule.getId(),
                                        rule.getResult().getThreshold(),
                                        this.getCloseEventType(rule));
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
                                        rntbdEndpoint.injectConnectionErrors(
                                            rule.getId(),
                                            rule.getResult().getThreshold(),
                                            this.getCloseEventType(rule));
                                        return Mono.empty();
                                    });
                            });
                    }

                    // If we reach here,then it means there is no specific addresses/region specified on the rule
                    // Inject connect error all available rntbd endpoints
                    return Flux.fromIterable(this.endpointProvider.list().collect(Collectors.toList()))
                        .flatMap(rntbdEndpoint -> {
                            rntbdEndpoint.injectConnectionErrors(
                                rule.getId(),
                                rule.getResult().getThreshold(),
                                this.getCloseEventType(rule));
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
                this.ruleStore.removeRule(rule); // important steps
            });
    }

    private Class<?> getCloseEventType(FaultInjectionConnectionErrorRule connectionErrorRule) {
        switch (connectionErrorRule.getResult().getErrorType()) {
            case CONNECTION_RESET:
                return RntbdFaultInjectionConnectionResetEvent.class;
            case CONNECTION_CLOSE:
                return RntbdFaultInjectionConnectionCloseEvent.class;
            default:
                throw new IllegalArgumentException("Connection error type " + connectionErrorRule.getResult().getErrorType() + " is not supported");
        }
    }

    private boolean isEffectiveRule(FaultInjectionConnectionErrorRule rule) {
        return this.ruleStore.containsRule(rule) && rule.isValid();
    }
}
