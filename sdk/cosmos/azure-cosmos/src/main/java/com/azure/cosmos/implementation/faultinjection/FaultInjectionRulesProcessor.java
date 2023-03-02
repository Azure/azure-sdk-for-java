// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.faultinjection;

import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.ThrottlingRetryOptions;
import com.azure.cosmos.implementation.BackoffRetryUtility;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.IRetryPolicy;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceThrottleRetryPolicy;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RetryContext;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.RxGatewayStoreModel;
import com.azure.cosmos.implementation.RxStoreModel;
import com.azure.cosmos.implementation.ShouldRetryResult;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.WebExceptionRetryPolicy;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.caches.RxCollectionCache;
import com.azure.cosmos.implementation.caches.RxPartitionKeyRangeCache;
import com.azure.cosmos.implementation.directconnectivity.AddressSelector;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdUtils;
import com.azure.cosmos.implementation.faultinjection.model.FaultInjectionConditionInternal;
import com.azure.cosmos.implementation.faultinjection.model.FaultInjectionConnectionErrorResultInternal;
import com.azure.cosmos.implementation.faultinjection.model.FaultInjectionConnectionErrorRule;
import com.azure.cosmos.implementation.faultinjection.model.FaultInjectionServerErrorResultInternal;
import com.azure.cosmos.implementation.faultinjection.model.FaultInjectionServerErrorRule;
import com.azure.cosmos.implementation.faultinjection.model.IFaultInjectionRuleInternal;
import com.azure.cosmos.implementation.feedranges.FeedRangeInternal;
import com.azure.cosmos.implementation.routing.PartitionKeyRangeIdentity;
import com.azure.cosmos.faultinjection.FaultInjectionCondition;
import com.azure.cosmos.faultinjection.FaultInjectionConnectionErrorResult;
import com.azure.cosmos.faultinjection.FaultInjectionConnectionType;
import com.azure.cosmos.faultinjection.FaultInjectionEndpoints;
import com.azure.cosmos.faultinjection.FaultInjectionOperationType;
import com.azure.cosmos.faultinjection.FaultInjectionRule;
import com.azure.cosmos.faultinjection.FaultInjectionServerErrorResult;
import com.azure.cosmos.faultinjection.FaultInjectionServerErrorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
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
    private final RxPartitionKeyRangeCache partitionKeyRangeCache;
    private final AddressSelector addressSelector;
    private final ThrottlingRetryOptions retryOptions;

    public FaultInjectionRulesProcessor(
        ConnectionMode connectionMode,
        RxStoreModel storeModel,
        RxGatewayStoreModel gatewayStoreModel,
        RxCollectionCache collectionCache,
        GlobalEndpointManager globalEndpointManager,
        RxPartitionKeyRangeCache partitionKeyRangeCache,
        AddressSelector addressSelector,
        ThrottlingRetryOptions retryOptions) {

        checkNotNull(connectionMode, "Argument 'connectionMode' can not be null");
        checkNotNull(storeModel, "Argument 'storeModel' can not be null");
        checkNotNull(gatewayStoreModel, "Argument 'gatewayStoreModel' can not be null");
        checkNotNull(collectionCache, "Argument 'collectionCache' can not be null");
        checkNotNull(globalEndpointManager, "Argument 'globalEndpointManager' can not be null");
        checkNotNull(partitionKeyRangeCache, "Argument 'partitionKeyRangeCache' can not be null");
        checkNotNull(addressSelector, "Argument 'addressSelector' can not be null");
        checkNotNull(retryOptions, "Argument 'addressSelector' can not be null");

        this.connectionMode = connectionMode;
        this.storeModel = storeModel;
        this.gatewayStoreModel = gatewayStoreModel;
        this.collectionCache = collectionCache;
        this.partitionKeyRangeCache = partitionKeyRangeCache;
        this.globalEndpointManager = globalEndpointManager;
        this.addressSelector = addressSelector;
        this.retryOptions = retryOptions;
    }

    /***
     * Main logic of the fault injection processor:
     * 1. Pre-populate all required information - regionEndpoints, physical addresses
     * 2. Create internal effective rule, and attach it to the original rule
     * 3. Routing the effective rule to the corresponding components: rntbd layer or gateway
     *
     * @param rules the rules to be configured.
     * @param containerNameLink the container name link.
     * @return the mono.
     */
    public Mono<Void> processFaultInjectionRules(List<FaultInjectionRule> rules, String containerNameLink) {
        checkNotNull(rules, "Argument 'rules' can not be null");
        checkArgument(
            StringUtils.isNotEmpty(containerNameLink),
            "Argument 'containerNameLink' can not be null nor empty.");

        return this.collectionCache.resolveByNameAsync(null, containerNameLink, null)
            .flatMap(collection -> {
                if (collection == null) {
                    return Mono.error(new IllegalStateException("Can not find collection info"));
                }

                return Flux.fromIterable(rules)
                    .flatMap(rule -> {
                        validateRule(rule);
                        return this.getEffectiveRule(rule, collection)
                            .flatMap(effectiveRule -> {
                                ImplementationBridgeHelpers
                                    .FaultInjectionRuleHelper
                                    .getFaultInjectionRuleAccessor()
                                    .setEffectiveFaultInjectionRule(rule, effectiveRule);

                                switch (rule.getCondition().getConnectionType()) {
                                    case DIRECT:
                                        this.storeModel.configFaultInjectionRule(effectiveRule);
                                        break;
                                    // TODO: add support for gateway mode
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
        DocumentCollection documentCollection) {

        if (rule.getResult() instanceof FaultInjectionServerErrorResult) {
            return this.getEffectiveServerErrorRule(rule, documentCollection);
        }

        if (rule.getResult() instanceof FaultInjectionConnectionErrorResult) {
            return this.getEffectiveConnectionErrorRule(rule, documentCollection);
        }

        return Mono.error(new IllegalStateException("Result type " + rule.getResult().getClass() + " is not supported"));
    }

    private Mono<IFaultInjectionRuleInternal> getEffectiveServerErrorRule(
        FaultInjectionRule rule,
        DocumentCollection documentCollection) {

        FaultInjectionServerErrorType errorType =
            ((FaultInjectionServerErrorResult) rule.getResult()).getServerErrorType();

        return Mono.just(rule)
            .flatMap(originalRule -> {
                // get effective condition
                FaultInjectionConditionInternal effectiveCondition = new FaultInjectionConditionInternal(documentCollection.getResourceId());

                if (rule.getCondition().getOperationType() != null && canErrorLimitToOperation(errorType)) {
                    effectiveCondition.setOperationType(this.getEffectiveOperationType(rule.getCondition().getOperationType()));
                }

                List<URI> regionEndpoints = this.getRegionEndpoints(rule.getCondition());
                effectiveCondition.setRegionEndpoints(regionEndpoints);

                // TODO: add handling for gateway mode

                // Direct connection mode, populate physical addresses
                return BackoffRetryUtility.executeRetry(
                        () -> this.resolvePhysicalAddresses(
                            regionEndpoints,
                            rule.getCondition().getEndpoints(),
                            this.isWriteOnlyEndpoint(rule.getCondition()),
                            documentCollection),
                        new FaultInjectionRuleProcessorRetryPolicy(this.retryOptions)
                    )
                    .map(addresses -> {
                        List<URI> effectiveAddresses = addresses;
                        if (!canErrorLimitToOperation(errorType)) {
                            effectiveAddresses =
                                addresses
                                    .stream()
                                    .map(address -> RntbdUtils.getServerKey(address))
                                    .collect(Collectors.toList());
                        }

                        effectiveCondition.setAddresses(effectiveAddresses);
                        return effectiveCondition;
                    });
            })
            .map(effectiveCondition -> {
                FaultInjectionServerErrorResult result = (FaultInjectionServerErrorResult) rule.getResult();
                return new FaultInjectionServerErrorRule(
                    rule.getId(),
                    rule.isEnabled(),
                    rule.getStartDelay(),
                    rule.getDuration(),
                    rule.getHitLimit(),
                    effectiveCondition,
                    new FaultInjectionServerErrorResultInternal(
                        result.getServerErrorType(),
                        result.getTimes(),
                        result.getDelay()
                    )
                );
            });
    }

    private boolean canErrorLimitToOperation(FaultInjectionServerErrorType errorType) {
        // Some errors makes sense to only apply for certain operationType/requests
        // but some should apply to all requests being routed to the server
        return errorType != FaultInjectionServerErrorType.SERVER_CONNECTION_DELAY
            && errorType != FaultInjectionServerErrorType.SERVER_GONE;
    }

    private Mono<IFaultInjectionRuleInternal> getEffectiveConnectionErrorRule(
        FaultInjectionRule rule,
        DocumentCollection documentCollection) {

        return Mono.just(rule)
            .flatMap(originalRule -> Mono.just(this.getRegionEndpoints(rule.getCondition())))
            .flatMap(regionEndpoints -> {
                return this.resolvePhysicalAddresses(
                        regionEndpoints,
                        rule.getCondition().getEndpoints(),
                        this.isWriteOnlyEndpoint(rule.getCondition()),
                        documentCollection)
                    .map(physicalAddresses -> {
                        List<URI> effectiveAddresses =
                            physicalAddresses
                                .stream()
                                .map(address -> RntbdUtils.getServerKey(address))
                                .collect(Collectors.toList());

                        FaultInjectionConnectionErrorResult result = (FaultInjectionConnectionErrorResult) rule.getResult();
                        return new FaultInjectionConnectionErrorRule(
                            rule.getId(),
                            rule.isEnabled(),
                            rule.getStartDelay(),
                            rule.getDuration(),
                            regionEndpoints,
                            effectiveAddresses,
                            new FaultInjectionConnectionErrorResultInternal(
                                result.getErrorType(),
                                result.getInterval(),
                                result.getThreshold()
                            )
                        );
                    });
            });
    }

    /***
     * If region is defined in the condition, then get the matching region service endpoint.
     * Else get all available read/write region service endpoints.
     *
     * @param condition the fault injection condition.
     * @return the region service endpoints.
     */
    private List<URI> getRegionEndpoints(FaultInjectionCondition condition) {
        boolean isWriteOnlyEndpoints = this.isWriteOnlyEndpoint(condition);

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
            case QUERY:
                return OperationType.Query;
            case UPSERT:
                return OperationType.Upsert;
            case REPLACE:
                return OperationType.Replace;
            case DELETE:
                return OperationType.Delete;
            default:
                throw new IllegalStateException("FaultInjectionOperationType " + faultInjectionOperationType + " is not supported");
        }
    }

    private Mono<List<URI>> resolvePhysicalAddresses(
        List<URI> regionEndpoints,
        FaultInjectionEndpoints addressEndpoints,
        boolean isWriteOnly,
        DocumentCollection documentCollection) {

        if (addressEndpoints == null) {
            return Mono.just(Arrays.asList());
        }

        return Flux.fromIterable(regionEndpoints)
            .flatMap(regionEndpoint -> {
                FeedRangeInternal feedRangeInternal = FeedRangeInternal.convert(addressEndpoints.getFeedRange());
                RxDocumentServiceRequest request = RxDocumentServiceRequest.create(
                    null,
                    OperationType.Read,
                    documentCollection.getResourceId(),
                    ResourceType.Document,
                    Collections.emptyMap());

                // The feed range can be mapped to multiple physical partitions
                // Get the feed range list and resolve addresses for each partition
                return feedRangeInternal
                    .getPartitionKeyRanges(
                        this.partitionKeyRangeCache,
                        request,
                        Mono.just(new Utils.ValueHolder<>(documentCollection))
                    )
                    .flatMapMany(pkRangeIdList -> {
                        return Flux.fromIterable(pkRangeIdList)
                            .flatMap(pkRangeId -> {
                                RxDocumentServiceRequest faultInjectionAddressRequest = RxDocumentServiceRequest.create(
                                    null,
                                    OperationType.Read,
                                    documentCollection.getResourceId(),
                                    ResourceType.Document,
                                    null);

                                faultInjectionAddressRequest.requestContext.locationEndpointToRoute = regionEndpoint;
                                faultInjectionAddressRequest.setPartitionKeyRangeIdentity(new PartitionKeyRangeIdentity(pkRangeId));

                                if (isWriteOnly) {
                                    return this.addressSelector
                                        .resolvePrimaryUriAsync(faultInjectionAddressRequest, true)
                                        .map(uri -> uri.getURI())
                                        .flux();
                                }

                                return this.addressSelector
                                    .resolveAllUriAsync(
                                        faultInjectionAddressRequest,
                                        addressEndpoints.isIncludePrimary(),
                                        true)
                                    .flatMapIterable(addresses -> {
                                        return addresses
                                            .stream()
                                            .map(uri -> uri.getURI())
                                            .sorted() // important: will be used to make sure the same replica addresses will be used across different client instances
                                            .limit(addressEndpoints.getReplicaCount())
                                            .collect(Collectors.toList());
                                    });
                            });
                    });
            })
            .collectList();
    }

    private boolean isWriteOnlyEndpoint(FaultInjectionCondition condition) {
        return condition.getOperationType() != null
            && this.getEffectiveOperationType(condition.getOperationType()).isWriteOperation();
    }

    static class FaultInjectionRuleProcessorRetryPolicy implements IRetryPolicy {
        private final ResourceThrottleRetryPolicy resourceThrottleRetryPolicy;
        private final WebExceptionRetryPolicy webExceptionRetryPolicy;
        public FaultInjectionRuleProcessorRetryPolicy(ThrottlingRetryOptions retryOptions) {
            this.resourceThrottleRetryPolicy = new ResourceThrottleRetryPolicy(
                retryOptions.getMaxRetryAttemptsOnThrottledRequests(),
                retryOptions.getMaxRetryWaitTime(),
                false);

            this.webExceptionRetryPolicy = new WebExceptionRetryPolicy();
        }

        @Override
        public Mono<ShouldRetryResult> shouldRetry(Exception e) {
            return this.webExceptionRetryPolicy.shouldRetry(e)
                .flatMap(shouldRetryResult -> {
                    if (shouldRetryResult.shouldRetry) {
                        return Mono.just(shouldRetryResult);
                    }

                    return this.resourceThrottleRetryPolicy.shouldRetry(e);
                });
        }

        @Override
        public RetryContext getRetryContext() {
            return null;
        }
    }
}
