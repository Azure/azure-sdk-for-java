// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.faultinjection;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.RxGatewayStoreModel;
import com.azure.cosmos.implementation.RxStoreModel;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.caches.RxCollectionCache;
import com.azure.cosmos.implementation.directconnectivity.AddressSelector;
import com.azure.cosmos.implementation.directconnectivity.Uri;
import com.azure.cosmos.implementation.faultinjection.model.FaultInjectionConditionInternal;
import com.azure.cosmos.implementation.faultinjection.model.FaultInjectionConnectionErrorRule;
import com.azure.cosmos.implementation.faultinjection.model.FaultInjectionServerErrorRule;
import com.azure.cosmos.implementation.faultinjection.model.IFaultInjectionRuleInternal;
import com.azure.cosmos.models.FaultInjectionCondition;
import com.azure.cosmos.models.FaultInjectionConnectionErrorResult;
import com.azure.cosmos.models.FaultInjectionConnectionType;
import com.azure.cosmos.models.FaultInjectionEndpoints;
import com.azure.cosmos.models.FaultInjectionOperationType;
import com.azure.cosmos.models.FaultInjectionRule;
import com.azure.cosmos.models.FaultInjectionServerErrorResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Enrich the rule with physical addresses if needed
 * Mapping to internal rule model
 */
public class FaultInjectionRulesProcessor {
    private final ConnectionMode connectionMode;
    private final RxStoreModel storeModel;
    private final RxGatewayStoreModel gatewayStoreModel;
    private final RxCollectionCache collectionCache;
    private final AddressSelector addressSelector;

    public FaultInjectionRulesProcessor(
        ConnectionMode connectionMode,
        RxStoreModel storeModel,
        RxGatewayStoreModel gatewayStoreModel,
        RxCollectionCache collectionCache,
        AddressSelector addressSelector) {

        this.connectionMode = connectionMode;
        this.storeModel = storeModel;
        this.gatewayStoreModel = gatewayStoreModel;
        this.collectionCache = collectionCache;
        this.addressSelector = addressSelector;
    }

    public Mono<Void> processFaultInjectionRule(List<FaultInjectionRule> rules, String containerNameLink) {
        checkNotNull(rules, "Argument 'rules' can not be null");

        return Flux.fromIterable(rules)
            .flatMap(rule -> {
                validateRule(rule);
                return resolvePhysicalAddresses(rule, containerNameLink)
                    .flatMap(addresses -> {
                        // create effective internal rules
                        IFaultInjectionRuleInternal effectiveRule =
                            this.getEffectiveRule(rule, containerNameLink, addresses);

                        ImplementationBridgeHelpers
                            .FaultInjectionRuleHelper
                            .getFaultInjectionRuleAccessor()
                            .setEffectiveFaultInjectionRule(rule, effectiveRule);

                        switch (rule.getCondition().getConnectionType()) {
                            case DIRECT:
                                this.storeModel.configFaultInjectionRule(effectiveRule);
                                break;
                            case GATEWAY:
                                this.gatewayStoreModel.configFaultInjectionRule(effectiveRule);
                                break;
                            default:
                                return Mono.error(new IllegalStateException("Connection type is not supported"));
                        }

                        return Mono.empty();
                    });
            })
            .then();
    }

    private void validateRule(FaultInjectionRule rule) {
        // TODO: Discussion: Do we really need to define the connection type? By the operation type, SDK should be able to defer
        // which connection it is targeted for
        if (rule.getCondition().getConnectionType() == FaultInjectionConnectionType.DIRECT
        && this.connectionMode != ConnectionMode.DIRECT) {
            throw new IllegalArgumentException("Direct connection type rule is not supported when client is not in direct mode.");
        }
    }

    private Mono<List<Uri>> resolvePhysicalAddresses(FaultInjectionRule rule, String containerNameLink) {
        if (rule.getCondition().getConnectionType() == FaultInjectionConnectionType.GATEWAY) {
            return Mono.just(Collections.emptyList());
        }

        if (rule.getCondition().getEndpoints() == null) {
            return Mono.just(Collections.emptyList());
        }

        // TODO: bad implementation, need to find a better way
        FaultInjectionEndpoints endpoints = rule.getCondition().getEndpoints();
        String path = Utils.joinPath(containerNameLink, null);
        RxDocumentServiceRequest request =
            RxDocumentServiceRequest.create(
                null,
                OperationType.Read,
                ResourceType.FaultInjection,
                path,
                new ConcurrentHashMap<>(),
                null);

        request.setPartitionKeyInternal(BridgeInternal.getPartitionKeyInternal(endpoints.getPartitionKey()));
        return this.collectionCache
            .resolveByNameAsync(null, containerNameLink, null)
            .flatMap(documentCollection -> {
                request.requestContext.resolvedCollectionRid = documentCollection.getResourceId();
                request.requestContext.faultInjectionLocationToRoute = rule.getCondition().getRegion();

                if (rule.getCondition().getOperationType() == FaultInjectionOperationType.CREATE) {
                    return this.addressSelector.resolvePrimaryUriAsync(request, true)
                        .map(primaryUri -> Arrays.asList(primaryUri));
                } else {
                    return this.addressSelector.resolveAllUriAsync(
                        request,
                        rule.getCondition().getEndpoints().isIncludePrimary(),
                        true
                    );
                }
            })
            .map(allAddresses -> {
                return allAddresses
                    .stream()
                    .sorted() // Always sort the addresses so the results can be deterministic across different clients
                    .limit(
                        ImplementationBridgeHelpers
                            .FaultInjectionConditionHelper
                            .getFaultInjectionConditionAccessor()
                            .getEffectiveReplicaCount(rule.getCondition()))
                    .collect(Collectors.toList());

            });
    }

    private OperationType getOperationType(FaultInjectionOperationType faultInjectionOperationType) {
        switch (faultInjectionOperationType) {
            case READ:
                return OperationType.Read;
            case CREATE:
                return OperationType.Create;
            default:
                throw new IllegalStateException("FaultInjectionOperationType " + faultInjectionOperationType + " is not supported");
        }
    }

    private FaultInjectionConditionInternal getEffectiveCondition(
        FaultInjectionCondition condition,
        String containerNameLink,
        List<Uri> physicalAddresses) {

        return new FaultInjectionConditionInternal(
            this.getOperationType(condition.getOperationType()),
            condition.getRegion(),
            containerNameLink,
            physicalAddresses
        );
    }

    private IFaultInjectionRuleInternal getEffectiveRule(
        FaultInjectionRule rule,
        String containerNameLink,
        List<Uri> physicalAddresses) {

        FaultInjectionConditionInternal effectiveCondition =
            this.getEffectiveCondition(rule.getCondition(), containerNameLink, physicalAddresses);

        IFaultInjectionRuleInternal effectiveRule;
        if (rule.getResult() instanceof FaultInjectionServerErrorResult) {
            effectiveRule = new FaultInjectionServerErrorRule(
                rule.getId(),
                effectiveCondition,
                (FaultInjectionServerErrorResult) rule.getResult(),
                rule.getDuration(),
                rule.getRequestHitLimit(),
                rule.isEnabled());
        } else if (rule.getResult() instanceof FaultInjectionConnectionErrorResult) {
            effectiveRule = new FaultInjectionConnectionErrorRule(
                rule.getId(),
                effectiveCondition,
                (FaultInjectionConnectionErrorResult) rule.getResult(),
                rule.getDuration(),
                rule.isEnabled());
        } else {
            throw new IllegalStateException("Cannot handle rule with result type " + rule.getResult().getClass());
        }

        return effectiveRule;
    }
}
