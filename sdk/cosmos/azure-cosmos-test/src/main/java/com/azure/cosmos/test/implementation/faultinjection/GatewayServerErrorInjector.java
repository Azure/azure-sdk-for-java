// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.test.implementation.faultinjection;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.faultinjection.IGatewayServerErrorInjector;
import com.azure.cosmos.test.faultinjection.FaultInjectionConnectionType;

import java.net.URI;
import java.time.Duration;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class GatewayServerErrorInjector implements IGatewayServerErrorInjector {

    private final FaultInjectionRuleStore ruleStore;

    public GatewayServerErrorInjector(FaultInjectionRuleStore ruleStore) {
        checkNotNull(ruleStore, "Argument 'ruleStore' can not be null");

        this.ruleStore = ruleStore;
    }

    @Override
    public boolean injectGatewayServerResponseDelayBeforeProcessing(
        long transportRequestId,
        URI requestUri,
        RxDocumentServiceRequest serviceRequest,
        Utils.ValueHolder<Duration> delayToBeInjected) {

        FaultInjectionRequestArgs requestArgs =
            this.createFaultInjectionRequestArgs(transportRequestId, requestUri, serviceRequest);

        FaultInjectionServerErrorRule serverResponseDelayRule = this.ruleStore.findServerResponseDelayRule(requestArgs);
        if (serverResponseDelayRule != null) {

            if (serverResponseDelayRule.getResult() != null
                && serverResponseDelayRule.getResult().getSuppressServiceRequests() != null
                && !serverResponseDelayRule.getResult().getSuppressServiceRequests()) {

                // delay will be injected after processing the request
                return false;
            }

            serviceRequest.faultInjectionRequestContext
                .applyFaultInjectionRule(
                    transportRequestId,
                    serverResponseDelayRule.getId());

            delayToBeInjected.v = serverResponseDelayRule.getResult().getDelay();
            return true;
        }

        return false;
    }

    @Override
    public boolean injectGatewayServerResponseDelayAfterProcessing(
        long transportRequestId,
        URI requestUri,
        RxDocumentServiceRequest serviceRequest,
        Utils.ValueHolder<Duration> delayToBeInjected) {

        FaultInjectionRequestArgs requestArgs =
            this.createFaultInjectionRequestArgs(transportRequestId, requestUri, serviceRequest);

        FaultInjectionServerErrorRule serverResponseDelayRule =
            this.ruleStore.findServerResponseDelayRule(requestArgs);
        if (serverResponseDelayRule != null) {

            if (serverResponseDelayRule.getResult() == null
                || serverResponseDelayRule.getResult().getSuppressServiceRequests() == null
                || serverResponseDelayRule.getResult().getSuppressServiceRequests()) {

                // delay was injected before processing the request
                return false;
            }

            serviceRequest.faultInjectionRequestContext
                .applyFaultInjectionRule(
                    transportRequestId,
                    serverResponseDelayRule.getId());

            delayToBeInjected.v = serverResponseDelayRule.getResult().getDelay();
            return true;
        }

        return false;

    }

    @Override
    public boolean injectGatewayServerResponseError(
        long transportRequestId,
        URI requestUri,
        RxDocumentServiceRequest serviceRequest,
        Utils.ValueHolder<CosmosException> exceptionToBeInjected) {

        FaultInjectionRequestArgs requestArgs =
            this.createFaultInjectionRequestArgs(transportRequestId, requestUri, serviceRequest);

        FaultInjectionServerErrorRule serverResponseErrorRule = this.ruleStore.findServerResponseErrorRule(requestArgs);

        if (serverResponseErrorRule != null) {
            serviceRequest.faultInjectionRequestContext
                .applyFaultInjectionRule(
                    transportRequestId,
                    serverResponseErrorRule.getId());

            CosmosException cause = serverResponseErrorRule.getInjectedServerError(serviceRequest);
            exceptionToBeInjected.v = cause;
            return true;
        }

        return false;
    }

    @Override
    public boolean injectGatewayServerConnectionDelay(
        long transportRequestId,
        URI requestUri,
        RxDocumentServiceRequest serviceRequest,
        Utils.ValueHolder<Duration> delayToBeInjected) {

        FaultInjectionRequestArgs requestArgs =
            this.createFaultInjectionRequestArgs(transportRequestId, requestUri, serviceRequest);

        FaultInjectionServerErrorRule serverConnectionDelayRule = this.ruleStore.findServerConnectionDelayRule(requestArgs);

        if (serverConnectionDelayRule != null) {
            serviceRequest.faultInjectionRequestContext
                .applyFaultInjectionRule(
                    transportRequestId,
                    serverConnectionDelayRule.getId());

            delayToBeInjected.v = serverConnectionDelayRule.getResult().getDelay();
            return true;
        }

        return false;
    }

    private FaultInjectionRequestArgs createFaultInjectionRequestArgs(
        long transportRequestId,
        URI requestUri,
        RxDocumentServiceRequest request) {
        return new FaultInjectionRequestArgs(
            transportRequestId,
            requestUri,
            false,
            request,
            FaultInjectionConnectionType.GATEWAY);
    }
}
