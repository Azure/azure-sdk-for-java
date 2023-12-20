// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.faultinjection;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.http.HttpRequest;
import com.azure.cosmos.implementation.http.HttpResponse;
import com.azure.cosmos.implementation.http.ReactorNettyRequestRecord;
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

    private List<IServerErrorInjector> faultInjectors = new ArrayList<>();

    public GatewayServerErrorInjector(Configs configs) {
        checkNotNull(configs, "Argument 'configs' can not be null");
        this.configs = configs;
    }

    public void registerServerErrorInjector(IServerErrorInjector serverErrorInjector) {
        checkNotNull(serverErrorInjector, "Argument 'serverErrorInjector' can not be null");
        this.faultInjectors.add(serverErrorInjector);
    }

    public Mono<HttpResponse> injectGatewayErrors(
        Duration responseTimeout,
        HttpRequest httpRequest,
        RxDocumentServiceRequest serviceRequest,
        Mono<HttpResponse> originalResponseMono) {
        return injectGatewayErrors(
            responseTimeout,
            httpRequest,
            serviceRequest,
            originalResponseMono,
            serviceRequest.requestContext.resolvedPartitionKeyRange != null
                ? Arrays.asList(serviceRequest.requestContext.resolvedPartitionKeyRange.getId()) : null);
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
                    Duration connectionAcquireTimeout = this.configs.getConnectionAcquireTimeout();
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
