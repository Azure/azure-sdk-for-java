// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.faultInjection;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.RxGatewayStoreModel;
import com.azure.cosmos.implementation.RxStoreModel;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.caches.RxCollectionCache;
import com.azure.cosmos.implementation.directconnectivity.AddressSelector;
import com.azure.cosmos.implementation.directconnectivity.Uri;
import com.azure.cosmos.models.FaultInjectionConnectionErrorResult;
import com.azure.cosmos.models.FaultInjectionEndpoints;
import com.azure.cosmos.models.FaultInjectionOperationType;
import com.azure.cosmos.models.FaultInjectionRequestProtocol;
import com.azure.cosmos.models.FaultInjectionRule;
import com.azure.cosmos.models.FaultInjectionServerErrorResult;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Enrich the rule with physical addresses if needed
 * Mapping to internal rule model
 */
public class FaultInjectionRulesProcessor {
    private final RxStoreModel storeModel;
    private final RxGatewayStoreModel gatewayStoreModel;
    private final RxCollectionCache collectionCache;
    private final AddressSelector addressSelector;

    public FaultInjectionRulesProcessor(
        RxStoreModel storeModel,
        RxGatewayStoreModel gatewayStoreModel,
        RxCollectionCache collectionCache,
        AddressSelector addressSelector) {
        this.storeModel = storeModel;
        this.gatewayStoreModel = gatewayStoreModel;
        this.collectionCache = collectionCache;
        this.addressSelector = addressSelector;
    }

    public Mono<Void> addFaultInjectionRule(FaultInjectionRule rule, String containerNameLink) {
        checkNotNull(rule, "Argument 'rule' can not be null");
        checkArgument(StringUtils.isNotEmpty(containerNameLink), "Argument 'containerNameLink' can not be null");

        if (rule.getCondition().getProtocol() == FaultInjectionRequestProtocol.TCP) {
            return this.getApplicableAddresses(
                    rule.getCondition().getEndpoints(),
                    rule.getCondition().getRegion(),
                    containerNameLink)
                .flatMap(addresses -> {
                    if (rule.getResult() instanceof FaultInjectionServerErrorResult) {
                        FaultInjectionServerErrorRule serverErrorRule =
                            new FaultInjectionServerErrorRule(
                                rule.getRuleId(),
                                this.getOperationType(rule.getCondition().getOperationType()),
                                (FaultInjectionServerErrorResult) rule.getResult(),
                                rule.isEnabled(),
                                rule.getDuration(),
                                rule.getRequestHitLimit(),
                                addresses);
                        this.storeModel.addFaultInjectionRule(serverErrorRule);
                        ImplementationBridgeHelpers
                            .CosmosFaultInjectionRuleHelper
                            .getFaultInjectionRuleAccessor()
                            .setEffectiveFaultInjectionRule(rule, serverErrorRule);
                    } else if (rule.getResult() instanceof FaultInjectionConnectionErrorResult) {
                        FaultInjectionConnectionErrorRule connectionErrorRule =
                            new FaultInjectionConnectionErrorRule(
                                rule.getRuleId(),
                                (FaultInjectionConnectionErrorResult) rule.getResult(),
                                rule.isEnabled(),
                                rule.getDuration(),
                                addresses);
                        this.storeModel.addFaultInjectionRule(connectionErrorRule);
                        ImplementationBridgeHelpers
                            .CosmosFaultInjectionRuleHelper
                            .getFaultInjectionRuleAccessor()
                            .setEffectiveFaultInjectionRule(rule, connectionErrorRule);
                    }

                    return Mono.empty();
                });
        } else {
            FaultInjectionServerErrorRule serverErrorRule =
                new FaultInjectionServerErrorRule(
                    rule.getRuleId(),
                    this.getOperationType(rule.getCondition().getOperationType()),
                    (FaultInjectionServerErrorResult) rule.getResult(),
                    rule.isEnabled(),
                    rule.getDuration(),
                    rule.getRequestHitLimit(),
                    Collections.emptyList());
            this.gatewayStoreModel.addFaultInjectionRule(serverErrorRule);
            return Mono.empty();
        }
    }

    private Mono<List<Uri>> getApplicableAddresses(
        FaultInjectionEndpoints faultInjectionEndpoints,
        String region,
        String containerLink) {

        // TODO: bad implementation, need to change
        if (faultInjectionEndpoints == null) {
            return Mono.just(Collections.emptyList());
        }

        String path = Utils.joinPath(containerLink, null);
        RxDocumentServiceRequest request =
            RxDocumentServiceRequest.create(
                null,
                OperationType.Read,
                ResourceType.FaultInjection,
                path,
                new ConcurrentHashMap<>(),
                null);

        request.setPartitionKeyInternal(BridgeInternal.getPartitionKeyInternal(faultInjectionEndpoints.getPartitionKey()));
        return this.collectionCache
            .resolveByNameAsync(null, containerLink, null)
            .flatMap(documentCollection -> {
                request.requestContext.resolvedCollectionRid = documentCollection.getResourceId();
                request.requestContext.faultInjectionLocationToRoute = region;


                return this.addressSelector.resolveAllUriAsync(
                    request,
                    !faultInjectionEndpoints.isExcludePrimary(),
                    true);
            })
            .map(allAddresses -> allAddresses.stream().limit(faultInjectionEndpoints.getReplicaCount()).collect(Collectors.toList()));
    }

    private OperationType getOperationType(FaultInjectionOperationType faultInjectionOperationType) {
        switch (faultInjectionOperationType) {
            case READ:
                return OperationType.Read;
            case CREATE:
                return OperationType.Create;
            default:
                throw new IllegalStateException("FaultInjectionOperationType " + faultInjectionOperationType + " is not supported in RntbdFaultInjector");
        }
    }
}
