// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.test.implementation.faultinjection;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.faultinjection.FaultInjectionRequestArgs;
import com.azure.cosmos.implementation.faultinjection.RntbdFaultInjectionRequestArgs;
import com.azure.cosmos.test.faultinjection.FaultInjectionConnectionType;
import com.azure.cosmos.test.faultinjection.FaultInjectionRule;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;
import static com.azure.cosmos.test.faultinjection.FaultInjectionConnectionType.DIRECT;
import static com.azure.cosmos.test.faultinjection.FaultInjectionConnectionType.GATEWAY;

public class FaultInjectionRuleStore {
    private final Set<FaultInjectionServerErrorRule> serverResponseDelayRuleSet = ConcurrentHashMap.newKeySet();
    private final Set<FaultInjectionServerErrorRule> serverResponseErrorRuleSet = ConcurrentHashMap.newKeySet();
    private final Set<FaultInjectionServerErrorRule> serverConnectionDelayRuleSet = ConcurrentHashMap.newKeySet();
    private final Set<FaultInjectionConnectionErrorRule> connectionErrorRuleSet = ConcurrentHashMap.newKeySet();

    private final FaultInjectionRuleProcessor ruleProcessor;

    public FaultInjectionRuleStore(CosmosAsyncContainer container) {
        checkNotNull(container, "Argument 'container' can not be null");

        this.ruleProcessor = this.createFaultInjectionRuleProcessor(container);
    }

    private FaultInjectionRuleProcessor createFaultInjectionRuleProcessor(CosmosAsyncContainer cosmosAsyncContainer) {
        AsyncDocumentClient client = BridgeInternal.getContextClient(
            ImplementationBridgeHelpers
                .CosmosAsyncDatabaseHelper
                .getCosmosAsyncDatabaseAccessor()
                .getCosmosAsyncClient(cosmosAsyncContainer.getDatabase())
        );
        return new FaultInjectionRuleProcessor(
            client.getConnectionPolicy().getConnectionMode(),
            client.getCollectionCache(),
            client.getGlobalEndpointManager(),
            client.getPartitionKeyRangeCache(),
            client.getAddressSelector(),
            client.getConnectionPolicy().getThrottlingRetryOptions());
    }

    public Mono<IFaultInjectionRuleInternal> configureFaultInjectionRule(FaultInjectionRule rule, String containerNameLink) {
        checkNotNull(rule, "Argument 'rule' can not be null");
        checkArgument(StringUtils.isNotEmpty(containerNameLink), "Argument 'containerNameLink' can not be null");

        return this.ruleProcessor.processFaultInjectionRule(rule, containerNameLink)
            .doOnSuccess(effectiveRule -> {
                if (effectiveRule instanceof FaultInjectionConnectionErrorRule) {
                    this.connectionErrorRuleSet.add((FaultInjectionConnectionErrorRule) effectiveRule);
                } else if (effectiveRule instanceof FaultInjectionServerErrorRule) {

                    FaultInjectionServerErrorRule serverErrorRule = (FaultInjectionServerErrorRule) effectiveRule;
                    switch (serverErrorRule.getResult().getServerErrorType()) {
                        case RESPONSE_DELAY:
                            this.serverResponseDelayRuleSet.add(serverErrorRule);
                            break;
                        case CONNECTION_DELAY:
                            this.serverConnectionDelayRuleSet.add(serverErrorRule);
                            break;
                        default:
                            this.serverResponseErrorRuleSet.add(serverErrorRule);
                            break;
                    }
                }
            });
    }

    public FaultInjectionServerErrorRule findServerResponseDelayRule(FaultInjectionRequestArgs requestArgs) {
        FaultInjectionConnectionType connectionType =
            requestArgs instanceof RntbdFaultInjectionRequestArgs ? DIRECT : GATEWAY;

        for (FaultInjectionServerErrorRule serverResponseDelayRule : this.serverResponseDelayRuleSet) {
            if (serverResponseDelayRule.getConnectionType() == connectionType
                && serverResponseDelayRule.isApplicable(requestArgs)) {
                return serverResponseDelayRule;
            }
        }

        return null;
    }

    public FaultInjectionServerErrorRule findServerResponseErrorRule(FaultInjectionRequestArgs requestArgs) {
        FaultInjectionConnectionType connectionType =
            requestArgs instanceof RntbdFaultInjectionRequestArgs ? DIRECT : GATEWAY;

        for (FaultInjectionServerErrorRule serverResponseDelayRule : this.serverResponseErrorRuleSet) {
            if (serverResponseDelayRule.getConnectionType() == connectionType
                && serverResponseDelayRule.isApplicable(requestArgs)) {
                return serverResponseDelayRule;
            }
        }

        return null;
    }

    public FaultInjectionServerErrorRule findServerConnectionDelayRule(FaultInjectionRequestArgs requestArgs) {
        FaultInjectionConnectionType connectionType =
            requestArgs instanceof RntbdFaultInjectionRequestArgs ? DIRECT : GATEWAY;

        for (FaultInjectionServerErrorRule serverResponseDelayRule : this.serverConnectionDelayRuleSet) {
            if (serverResponseDelayRule.getConnectionType() == connectionType
                && serverResponseDelayRule.isApplicable(requestArgs)) {
                return serverResponseDelayRule;
            }
        }

        return null;
    }

    public boolean containsRule(FaultInjectionConnectionErrorRule rule) {
        return this.connectionErrorRuleSet.contains(rule);
    }

    public boolean removeRule(FaultInjectionConnectionErrorRule rule) {
        return this.connectionErrorRuleSet.remove(rule);
    }
}
