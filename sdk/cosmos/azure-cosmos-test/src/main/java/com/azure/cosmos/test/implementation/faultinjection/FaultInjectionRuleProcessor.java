// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.test.implementation.faultinjection;

import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.ThrottlingRetryOptions;
import com.azure.cosmos.implementation.BackoffRetryUtility;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.IRetryPolicy;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceThrottleRetryPolicy;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RetryContext;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.ShouldRetryResult;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.WebExceptionRetryPolicy;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.caches.RxCollectionCache;
import com.azure.cosmos.implementation.caches.RxPartitionKeyRangeCache;
import com.azure.cosmos.implementation.directconnectivity.AddressSelector;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdUtils;
import com.azure.cosmos.implementation.feedranges.FeedRangeInternal;
import com.azure.cosmos.implementation.routing.PartitionKeyRangeIdentity;
import com.azure.cosmos.test.faultinjection.FaultInjectionCondition;
import com.azure.cosmos.test.faultinjection.FaultInjectionConnectionErrorResult;
import com.azure.cosmos.test.faultinjection.FaultInjectionConnectionType;
import com.azure.cosmos.test.faultinjection.FaultInjectionEndpoints;
import com.azure.cosmos.test.faultinjection.FaultInjectionOperationType;
import com.azure.cosmos.test.faultinjection.FaultInjectionRule;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorResult;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorType;
import com.azure.cosmos.test.implementation.ImplementationBridgeHelpers;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.ArrayList;
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
public class FaultInjectionRuleProcessor {
    private final ConnectionMode connectionMode;
    private final RxCollectionCache collectionCache;
    private final GlobalEndpointManager globalEndpointManager;
    private final RxPartitionKeyRangeCache partitionKeyRangeCache;
    private final AddressSelector addressSelector;
    private final ThrottlingRetryOptions retryOptions;

    public FaultInjectionRuleProcessor(
        ConnectionMode connectionMode,
        RxCollectionCache collectionCache,
        GlobalEndpointManager globalEndpointManager,
        RxPartitionKeyRangeCache partitionKeyRangeCache,
        AddressSelector addressSelector,
        ThrottlingRetryOptions retryOptions) {

        checkNotNull(connectionMode, "Argument 'connectionMode' can not be null");
        checkNotNull(collectionCache, "Argument 'collectionCache' can not be null");
        checkNotNull(globalEndpointManager, "Argument 'globalEndpointManager' can not be null");
        checkNotNull(partitionKeyRangeCache, "Argument 'partitionKeyRangeCache' can not be null");
        checkNotNull(addressSelector, "Argument 'addressSelector' can not be null");
        checkNotNull(retryOptions, "Argument 'addressSelector' can not be null");

        this.connectionMode = connectionMode;
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
     *
     * @param rule the rule to be configured.
     * @param containerNameLink the container name link.
     * @return the mono.
     */
    public Mono<IFaultInjectionRuleInternal> processFaultInjectionRule(
        FaultInjectionRule rule,
        String containerNameLink) {
        checkNotNull(rule, "Argument 'rule' can not be null");
        checkArgument(
            StringUtils.isNotEmpty(containerNameLink),
            "Argument 'containerNameLink' can not be null nor empty.");

        return this.collectionCache.resolveByNameAsync(null, containerNameLink, null)
            .flatMap(collection -> {
                if (collection == null) {
                    return Mono.error(new IllegalStateException("Can not find collection info"));
                }

                validateRule(rule);
                return this.getEffectiveRule(rule, collection)
                    .map(effectiveRule -> {
                        ImplementationBridgeHelpers
                            .FaultInjectionRuleHelper
                            .getFaultInjectionRuleAccessor()
                            .setEffectiveFaultInjectionRule(rule, effectiveRule);

                        return effectiveRule;
                    });
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

                if (StringUtils.isEmpty(rule.getCondition().getRegion())) {
                    // if region is not specific configured, then also add the defaultEndpoint
                    List<URI> regionEndpointsWithDefault = new ArrayList<>(regionEndpoints);
                    regionEndpointsWithDefault.add(this.globalEndpointManager.getDefaultEndpoint());
                    effectiveCondition.setRegionEndpoints(regionEndpointsWithDefault);
                } else {
                    effectiveCondition.setRegionEndpoints(regionEndpoints);
                }

                // TODO: add handling for gateway mode

                // Direct connection mode, populate physical addresses
                boolean primaryAddressesOnly = this.isWriteOnly(rule.getCondition());
                return BackoffRetryUtility.executeRetry(
                        () -> this.resolvePhysicalAddresses(
                            regionEndpoints,
                            rule.getCondition().getEndpoints(),
                            primaryAddressesOnly,
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

                        effectiveCondition.setAddresses(effectiveAddresses, primaryAddressesOnly);
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
                    rule.getCondition().getConnectionType(),
                    effectiveCondition,
                    new FaultInjectionServerErrorResultInternal(
                        result.getServerErrorType(),
                        result.getTimes(),
                        result.getDelay(),
                        result.getSuppressServiceRequests()
                    )
                );
            });
    }

    private boolean canErrorLimitToOperation(FaultInjectionServerErrorType errorType) {
        // Some errors makes sense to only apply for certain operationType/requests
        // but some should apply to all requests being routed to the server
        return errorType != FaultInjectionServerErrorType.CONNECTION_DELAY
            && errorType != FaultInjectionServerErrorType.GONE;
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
                        this.isWriteOnly(rule.getCondition()),
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
                            rule.getCondition().getConnectionType(),
                            result
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
        boolean isWriteOnlyEndpoints = this.isWriteOnly(condition);

        if (StringUtils.isNotEmpty(condition.getRegion())) {
            return Arrays.asList(
                this.globalEndpointManager.resolveFaultInjectionServiceEndpoint(condition.getRegion(), isWriteOnlyEndpoints));
        } else {
            return isWriteOnlyEndpoints
                ? this.globalEndpointManager.getAvailableWriteEndpoints()
                : this.globalEndpointManager.getAvailableReadEndpoints();
        }
    }

    private OperationType getEffectiveOperationType(FaultInjectionOperationType faultInjectionOperationType) {
        if (faultInjectionOperationType == null) {
            return null;
        }

        switch (faultInjectionOperationType) {
            case READ_ITEM:
                return OperationType.Read;
            case CREATE_ITEM:
                return OperationType.Create;
            case QUERY_ITEM:
                return OperationType.Query;
            case UPSERT_ITEM:
                return OperationType.Upsert;
            case REPLACE_ITEM:
                return OperationType.Replace;
            case DELETE_ITEM:
                return OperationType.Delete;
            case PATCH_ITEM:
                return OperationType.Patch;
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
                                        // There are two rules need to happens here:
                                        // 1. if isIncludePrimary is true, then basically make sure primary replica address will always be returned
                                        // 2. make sure the same replica addresses will be used across different client instances
                                        return addresses
                                            .stream()
                                            .sorted((o1, o2) -> {
                                                if (o1.isPrimary()) {
                                                    return -1;
                                                }

                                                if (o2.isPrimary()) {
                                                    return 1;
                                                }

                                                return o1.getURIAsString().compareTo(o2.getURIAsString());
                                            })
                                            .map(uri -> uri.getURI())
                                            .limit(addressEndpoints.getReplicaCount())
                                            .collect(Collectors.toList());
                                    });
                            });
                    });
            })
            .collectList();
    }

    private boolean isWriteOnly(FaultInjectionCondition condition) {
        return condition.getOperationType() != null
            && this.getEffectiveOperationType(condition.getOperationType()).isWriteOperation();
    }

    static class FaultInjectionRuleProcessorRetryPolicy implements IRetryPolicy {
        private final ResourceThrottleRetryPolicy resourceThrottleRetryPolicy;
        private final WebExceptionRetryPolicy webExceptionRetryPolicy;
        FaultInjectionRuleProcessorRetryPolicy(ThrottlingRetryOptions retryOptions) {
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
