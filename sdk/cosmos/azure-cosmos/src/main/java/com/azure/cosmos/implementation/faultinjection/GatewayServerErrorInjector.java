// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.faultinjection;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.caches.RxCollectionCache;
import com.azure.cosmos.implementation.caches.RxPartitionKeyRangeCache;
import com.azure.cosmos.implementation.http.HttpRequest;
import com.azure.cosmos.implementation.http.HttpResponse;
import com.azure.cosmos.implementation.http.ReactorNettyRequestRecord;
import com.azure.cosmos.implementation.routing.PartitionKeyInternal;
import com.azure.cosmos.implementation.routing.PartitionKeyInternalHelper;
import io.netty.channel.ConnectTimeoutException;
import io.netty.handler.timeout.ReadTimeoutException;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class GatewayServerErrorInjector {

    private final Configs configs;
    private final RxCollectionCache collectionCache;
    private final RxPartitionKeyRangeCache partitionKeyRangeCache;

    private List<IServerErrorInjector> faultInjectors = new ArrayList<>();

    public GatewayServerErrorInjector(
        Configs configs,
        RxCollectionCache collectionCache,
        RxPartitionKeyRangeCache partitionKeyRangeCache) {
        checkNotNull(configs, "Argument 'configs' can not be null");
        this.configs = configs;
        this.collectionCache = collectionCache;
        this.partitionKeyRangeCache = partitionKeyRangeCache;
    }

    public GatewayServerErrorInjector(Configs configs) {
        this(configs, null, null);
    }

    public void registerServerErrorInjector(IServerErrorInjector serverErrorInjector) {
        checkNotNull(serverErrorInjector, "Argument 'serverErrorInjector' can not be null");
        this.faultInjectors.add(serverErrorInjector);
    }

    private Mono<Utils.ValueHolder<PartitionKeyRange>> resolvePartitionKeyRange(RxDocumentServiceRequest request) {
        // faultInjection rule can be configured to only apply for a certain partition
        // but in the normal flow, only session consistency will populate the resolvePartitionKey when apply session token
        // so for other consistencies, we need to calculate here
        if (request.getResourceType() != ResourceType.Document) {
            return Mono.just(Utils.ValueHolder.initialize(null));
        }

        if (this.collectionCache == null || this.partitionKeyRangeCache == null) {
            return Mono.just(Utils.ValueHolder.initialize(null));
        }

        if (request == null || request.requestContext == null) {
            return Mono.just(Utils.ValueHolder.initialize(null));
        }

        if (request.requestContext.resolvedPartitionKeyRange != null) {
            return Mono.just(Utils.ValueHolder.initialize(request.requestContext.resolvedPartitionKeyRange));
        }

        StringBuilder sb = new StringBuilder();
        sb.append("GatewayServerErrorInjector.resolvePartitionKeyRange:").append(",");

        return this.collectionCache
            .resolveCollectionAsync(
                BridgeInternal.getMetaDataDiagnosticContext(request.requestContext.cosmosDiagnostics), request)
            .flatMap(collectionValueHolder -> {
                return partitionKeyRangeCache
                    .tryLookupAsync(
                        BridgeInternal.getMetaDataDiagnosticContext(request.requestContext.cosmosDiagnostics),
                        collectionValueHolder.v.getResourceId(),
                        null,
                        null,
                        sb)
                    .flatMap(collectionRoutingMapValueHolder -> {
                        String partitionKeyRangeId =
                            request.getHeaders().get(HttpConstants.HttpHeaders.PARTITION_KEY_RANGE_ID);
                        PartitionKeyInternal partitionKeyInternal = request.getPartitionKeyInternal();
                        if (StringUtils.isNotEmpty(partitionKeyRangeId)) {
                            PartitionKeyRange range =
                                collectionRoutingMapValueHolder.v.getRangeByPartitionKeyRangeId(partitionKeyRangeId);
                            request.requestContext.resolvedPartitionKeyRange = range;
                        } else if (partitionKeyInternal != null) {
                            String effectivePartitionKeyString = PartitionKeyInternalHelper
                                .getEffectivePartitionKeyString(
                                    partitionKeyInternal,
                                    collectionValueHolder.v.getPartitionKey());
                            PartitionKeyRange range =
                                collectionRoutingMapValueHolder.v.getRangeByEffectivePartitionKey(effectivePartitionKeyString);
                            request.requestContext.resolvedPartitionKeyRange = range;
                        }

                        return Mono.just(Utils.ValueHolder.initialize(request.requestContext.resolvedPartitionKeyRange));
                    });
            });
    }

    public Mono<HttpResponse> injectGatewayErrors(
        Duration responseTimeout,
        HttpRequest httpRequest,
        RxDocumentServiceRequest serviceRequest,
        Mono<HttpResponse> originalResponseMono) {

        return this.resolvePartitionKeyRange(serviceRequest)
            .flatMap(resolvedPartitionKeyRangeValueHolder -> {
                return injectGatewayErrors(
                    responseTimeout,
                    httpRequest,
                    serviceRequest,
                    originalResponseMono,
                    resolvedPartitionKeyRangeValueHolder.v == null ? null : Arrays.asList(resolvedPartitionKeyRangeValueHolder.v.getId()));
            });
    }

    public Mono<HttpResponse> injectGatewayErrors(
        Duration responseTimeout,
        HttpRequest httpRequest,
        RxDocumentServiceRequest serviceRequest,
        Mono<HttpResponse> originalResponseMono,
        List<String> partitionKeyRangeIds) {

        return Mono.just(responseTimeout)
            .flatMap(effectiveResponseTimeout -> {
                Utils.ValueHolder<CosmosException> exceptionToBeInjected = new Utils.ValueHolder<>();
                Utils.ValueHolder<Duration> delayToBeInjected = new Utils.ValueHolder<>();
                FaultInjectionRequestArgs faultInjectionRequestArgs =
                    this.createFaultInjectionRequestArgs(
                        httpRequest.reactorNettyRequestRecord(),
                        httpRequest.uri(),
                        serviceRequest,
                        partitionKeyRangeIds);

                if (this.injectGatewayServerResponseError(faultInjectionRequestArgs, exceptionToBeInjected)) {
                    return Mono.error(exceptionToBeInjected.v);
                }

                if (this.injectGatewayServerConnectionDelay(faultInjectionRequestArgs, delayToBeInjected)) {
                    Duration connectionAcquireTimeout = Configs.getConnectionAcquireTimeout();
                    if (delayToBeInjected.v.toMillis() >= connectionAcquireTimeout.toMillis()) {
                        return Mono.delay(connectionAcquireTimeout)
                            .then(Mono.error(new ConnectTimeoutException()));
                    } else {
                        return Mono.delay(delayToBeInjected.v)
                            .then(originalResponseMono);
                    }
                }

                if (this.injectGatewayServerResponseDelayBeforeProcessing(faultInjectionRequestArgs, delayToBeInjected)) {
                    if (delayToBeInjected.v.toMillis() >= effectiveResponseTimeout.toMillis()) {
                        return Mono.delay(effectiveResponseTimeout)
                            .then(Mono.error(new ReadTimeoutException()));
                    } else {
                        return Mono.delay(delayToBeInjected.v)
                            .then(originalResponseMono);
                    }
                }

                if (this.injectGatewayServerResponseDelayAfterProcessing(faultInjectionRequestArgs, delayToBeInjected)) {
                    if (delayToBeInjected.v.toMillis() >= effectiveResponseTimeout.toMillis()) {
                        return originalResponseMono
                            .delayElement(delayToBeInjected.v)
                            .then(Mono.error(new ReadTimeoutException()));
                    } else {
                        return originalResponseMono
                            .delayElement(delayToBeInjected.v);
                    }
                }

                return originalResponseMono;
            });
    }

    private boolean injectGatewayServerResponseDelayBeforeProcessing(
        FaultInjectionRequestArgs faultInjectionRequestArgs,
        Utils.ValueHolder<Duration> delayToBeInjected) {

        for (IServerErrorInjector serverErrorInjector : faultInjectors) {
            if(serverErrorInjector.injectServerResponseDelayBeforeProcessing(faultInjectionRequestArgs, delayToBeInjected)) {
                return true;
            }
        }
        return false;
    }

    private boolean injectGatewayServerResponseDelayAfterProcessing(
        FaultInjectionRequestArgs faultInjectionRequestArgs,
        Utils.ValueHolder<Duration> delayToBeInjected) {

        for (IServerErrorInjector serverErrorInjector : faultInjectors) {
            if(serverErrorInjector.injectServerResponseDelayAfterProcessing(faultInjectionRequestArgs, delayToBeInjected)) {
                return true;
            }
        }
        return false;
    }

    private boolean injectGatewayServerResponseError(
       FaultInjectionRequestArgs faultInjectionRequestArgs,
        Utils.ValueHolder<CosmosException> exceptionToBeInjected) {
        for (IServerErrorInjector serverErrorInjector : faultInjectors) {
            if(serverErrorInjector.injectServerResponseError(faultInjectionRequestArgs, exceptionToBeInjected)) {
                return true;
            }
        }
        return false;
    }

    private boolean injectGatewayServerConnectionDelay(
        FaultInjectionRequestArgs faultInjectionRequestArgs,
        Utils.ValueHolder<Duration> delayToBeInjected) {
        for (IServerErrorInjector serverErrorInjector : faultInjectors) {
            if(serverErrorInjector.injectServerConnectionDelay(faultInjectionRequestArgs, delayToBeInjected)) {
                return true;
            }
        }
        return false;
    }

    private GatewayFaultInjectionRequestArgs createFaultInjectionRequestArgs(
        ReactorNettyRequestRecord requestRecord,
        URI requestUri,
        RxDocumentServiceRequest serviceRequest,
        List<String> partitionKeyRangeIds) {
        return new GatewayFaultInjectionRequestArgs(
            requestRecord.getTransportRequestId(),
            requestUri,
            serviceRequest,
            partitionKeyRangeIds);
    }
}
