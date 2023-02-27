// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.faultinjection;

import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.RxGatewayStoreModel;
import com.azure.cosmos.implementation.RxStoreModel;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.caches.RxCollectionCache;
import com.azure.cosmos.implementation.directconnectivity.AddressSelector;
import com.azure.cosmos.implementation.directconnectivity.Uri;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdUtils;
import com.azure.cosmos.implementation.faultinjection.model.FaultInjectionConditionInternal;
import com.azure.cosmos.implementation.faultinjection.model.FaultInjectionConnectionErrorRule;
import com.azure.cosmos.implementation.faultinjection.model.FaultInjectionServerErrorRule;
import com.azure.cosmos.implementation.faultinjection.model.IFaultInjectionRuleInternal;
import com.azure.cosmos.implementation.feedranges.FeedRangeInternal;
import com.azure.cosmos.models.FaultInjectionCondition;
import com.azure.cosmos.models.FaultInjectionConnectionErrorResult;
import com.azure.cosmos.models.FaultInjectionConnectionType;
import com.azure.cosmos.models.FaultInjectionOperationType;
import com.azure.cosmos.models.FaultInjectionRule;
import com.azure.cosmos.models.FaultInjectionServerErrorResult;
import com.azure.cosmos.models.FaultInjectionServerErrorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Enrich the rule with required information: for example physical addresses
 * Mapping to internal rule model and route to the correct components.
 */
public class FaultInjectionRulesProcessor {
    private static final Logger logger = LoggerFactory.getLogger(FaultInjectionRulesProcessor.class);

    private final ConnectionMode connectionMode;
    private final RxStoreModel storeModel;
    private final RxGatewayStoreModel gatewayStoreModel;
    private final RxCollectionCache collectionCache;

    private final GlobalEndpointManager globalEndpointManager;
    private final AddressSelector addressSelector;

    public FaultInjectionRulesProcessor(
        ConnectionMode connectionMode,
        RxStoreModel storeModel,
        RxGatewayStoreModel gatewayStoreModel,
        RxCollectionCache collectionCache,
        GlobalEndpointManager globalEndpointManager,
        AddressSelector addressSelector) {

        this.connectionMode = connectionMode;
        this.storeModel = storeModel;
        this.gatewayStoreModel = gatewayStoreModel;
        this.collectionCache = collectionCache;
        this.globalEndpointManager = globalEndpointManager;
        this.addressSelector = addressSelector;
    }

    /***
     * Main logic of the fault injection processor:
     * 1. Pre-populate all required information - serviceEndpoints, physical addresses
     * @param rules
     * @param containerNameLink
     * @return
     */
    public Mono<Void> processFaultInjectionRules(List<FaultInjectionRule> rules, String containerNameLink) {
        checkNotNull(rules, "Argument 'rules' can not be null");
        checkArgument(
            StringUtils.isNotEmpty(containerNameLink),
            "Argument 'containerNameLink' can not be null nor empty.");

        // TODO: add retry logic
        return this.collectionCache.resolveByNameAsync(null, containerNameLink, null)
            .flatMap(collection -> {
                if (collection == null) {
                    return Mono.error(new IllegalStateException("Can not find collection info"));
                }

                return Flux.fromIterable(rules)
                    .flatMap(rule -> {
                        validateRule(rule);
                        return this.getEffectiveRule(rule, collection.getResourceId())
                            .flatMap(effectiveRule -> {
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
                    }).then();
            });
    }

    private void validateRule(FaultInjectionRule rule) {
        if (rule.getCondition().getConnectionType() == FaultInjectionConnectionType.DIRECT
            && this.connectionMode != ConnectionMode.DIRECT) {
            throw new IllegalArgumentException("Direct connection type rule is not supported when client is not in direct mode.");
        }
    }

    private Mono<IFaultInjectionRuleInternal> getEffectiveRule(
        FaultInjectionRule rule,
        String containerResourceId) {

        if (rule.getResult() instanceof FaultInjectionServerErrorResult) {
            return this.getEffectiveServerErrorRule(rule, containerResourceId);
        }

        if (rule.getResult() instanceof FaultInjectionConnectionErrorResult) {
            return this.getEffectiveConnectionErrorRule(rule, containerResourceId);
        }

        return Mono.error(new IllegalStateException("Result type " + rule.getResult().getClass() + " is not supported"));
    }

    private Mono<IFaultInjectionRuleInternal> getEffectiveServerErrorRule(
        FaultInjectionRule rule,
        String containerResourceId) {

        return Mono.just(rule)
            .flatMap(originalRule -> {
                // get effective condition
                FaultInjectionConditionInternal effectiveCondition = new FaultInjectionConditionInternal(containerResourceId);

                if (rule.getCondition().getOperationType() != null) {
                    effectiveCondition.setOperationType(this.getEffectiveOperationType(rule.getCondition().getOperationType()));
                }

                if (StringUtils.isNotEmpty(rule.getCondition().getRegion())) {
                    effectiveCondition.setServiceEndpoint(
                        this.globalEndpointManager.resolveFaultInjectionServiceEndpoint(
                            rule.getCondition().getRegion(),
                            this.isWriteOnlyRule(rule.getCondition())
                        ));
                }

                if (rule.getCondition().getConnectionType() == FaultInjectionConnectionType.GATEWAY) {
                    return Mono.just(effectiveCondition);
                }

                // Direct connection mode, populate physical addresses
                return this.resolvePhysicalAddresses(rule.getCondition(), containerResourceId)
                    .map(addresses -> {
                        List<URI> effectiveAddresses = addresses;
                        FaultInjectionServerErrorType errorType =
                            ((FaultInjectionServerErrorResult) rule.getResult()).getServerErrorType();
                        switch (errorType) {
                            case SERVER_RESPONSE_DELAY:
                            case SERVER_CONNECTION_DELAY:
                            case SERVER_GONE:
                                effectiveAddresses =
                                    addresses
                                        .stream()
                                        .map(address -> RntbdUtils.getServerKey(address))
                                        .collect(Collectors.toList());
                                break;
                            default:
                                break;
                        }

                        effectiveCondition.setAddresses(effectiveAddresses);
                        return effectiveCondition;
                    });

            })
            .map(effectiveCondition -> {
                return new FaultInjectionServerErrorRule(
                    rule.getId(),
                    rule.isEnabled(),
                    rule.getStartDelay(),
                    rule.getDuration(),
                    rule.getHitLimit(),
                    effectiveCondition,
                    (FaultInjectionServerErrorResult) rule.getResult()
                );
            });
    }

    private Mono<IFaultInjectionRuleInternal> getEffectiveConnectionErrorRule(
        FaultInjectionRule rule,
        String containerResourceId) {

        return Mono.just(rule)
            .flatMap(originalRule -> {
                if (rule.getCondition().getConnectionType() == FaultInjectionConnectionType.GATEWAY) {
                    // using service endpoints as addresses
                    return Mono.just(this.getServiceEndpoints(rule.getCondition()));
                }

                return this.resolvePhysicalAddresses(rule.getCondition(), containerResourceId)
                    .map(addresses -> {
                        return addresses
                            .stream()
                            .map(address -> RntbdUtils.getServerKey(address))
                            .collect(Collectors.toList());
                    });
            })
            .map(effectiveAddresses -> {
                return new FaultInjectionConnectionErrorRule(
                    rule.getId(),
                    rule.isEnabled(),
                    rule.getStartDelay(),
                    rule.getDuration(),
                    effectiveAddresses,
                    (FaultInjectionConnectionErrorResult) rule.getResult()
                );
            });
    }

    private List<URI> getServiceEndpoints(FaultInjectionCondition condition) {
        boolean isWriteOnlyEndpoints = this.isWriteOnlyRule(condition);

        if (StringUtils.isNotEmpty(condition.getRegion())) {
            return Arrays.asList(
                this.globalEndpointManager.resolveFaultInjectionServiceEndpoint(condition.getRegion(), isWriteOnlyEndpoints));
        } else {
            return isWriteOnlyEndpoints ? this.globalEndpointManager.getWriteEndpoints() : this.globalEndpointManager.getReadEndpoints();
        }
    }

    private OperationType getEffectiveOperationType(FaultInjectionOperationType faultInjectionOperationType) {
        if (faultInjectionOperationType == null) {
            return null;
        }

        switch (faultInjectionOperationType) {
            case READ:
                return OperationType.Read;
            case CREATE:
                return OperationType.Create;
            default:
                throw new IllegalStateException("FaultInjectionOperationType " + faultInjectionOperationType + " is not supported");
        }
    }

    private Mono<List<URI>> resolvePhysicalAddresses(
        FaultInjectionCondition condition,
        String containerResourceId) {

        if (condition.getEndpoints() == null) {
            return Mono.just(Arrays.asList());
        }

        List<URI> serviceEndpoints = this.getServiceEndpoints(condition);

        return Flux.fromIterable(serviceEndpoints)
            .flatMap(serviceEndpoint -> {
                RxDocumentServiceRequest request = RxDocumentServiceRequest.create(
                    null,
                    this.getEffectiveOperationType(condition.getOperationType()),
                    containerResourceId,
                    ResourceType.FaultInjection,
                    Collections.emptyMap());

                request.applyFeedRangeFilter(
                    FeedRangeInternal.convert(condition.getEndpoints().getFeedRange()));
                request.requestContext.locationEndpointToRoute = serviceEndpoint;

                if (this.isWriteOnlyRule(condition)) {
                    return this.addressSelector
                        .resolvePrimaryUriAsync(request, true)
                        .map(uri -> uri.getURI())
                        .flux();
                }

                return this.addressSelector
                    .resolveAllUriAsync(
                        request,
                        condition.getEndpoints().isIncludePrimary(),
                        true)
                    .flatMapIterable(addresses -> {
                        return addresses
                            .stream()
                            .map(uri -> uri.getURI())
                            .sorted()
                            .limit(condition.getEndpoints().getReplicaCount())
                            .collect(Collectors.toList());
                    });
            })
            .collectList();
    }

    private boolean isWriteOnlyRule(FaultInjectionCondition condition) {
        return condition.getOperationType() != null
            && this.getEffectiveOperationType(condition.getOperationType()).isWriteOperation();
    }
}
