// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.faultinjection;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Utils;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class GatewayServerErrorInjector implements IGatewayServerErrorInjector {

    private List<IGatewayServerErrorInjector> faultInjectors = new ArrayList<>();

    public void registerServerErrorInjector(IGatewayServerErrorInjector serverErrorInjector) {
        checkNotNull(serverErrorInjector, "Argument 'serverErrorInjector' can not be null");
        this.faultInjectors.add(serverErrorInjector);
    }

    @Override
    public boolean injectGatewayServerResponseDelayBeforeProcessing(
        long transportRequestId,
        URI requestUri,
        RxDocumentServiceRequest serviceRequest,
        Utils.ValueHolder<Duration> delayToBeInjected) {

        for (IGatewayServerErrorInjector serverErrorInjector : faultInjectors) {
            if(serverErrorInjector.injectGatewayServerResponseDelayBeforeProcessing(transportRequestId, requestUri, serviceRequest, delayToBeInjected)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean injectGatewayServerResponseDelayAfterProcessing(
        long transportRequestId,
        URI requestUri,
        RxDocumentServiceRequest serviceRequest,
        Utils.ValueHolder<Duration> delayToBeInjected) {

        for (IGatewayServerErrorInjector serverErrorInjector : faultInjectors) {
            if(serverErrorInjector.injectGatewayServerResponseDelayAfterProcessing(transportRequestId, requestUri, serviceRequest, delayToBeInjected)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean injectGatewayServerResponseError(
        long transportRequestId,
        URI requestUri,
        RxDocumentServiceRequest serviceRequest,
        Utils.ValueHolder<CosmosException> exceptionToBeInjected) {
        for (IGatewayServerErrorInjector serverErrorInjector : faultInjectors) {
            if(serverErrorInjector.injectGatewayServerResponseError(transportRequestId, requestUri, serviceRequest, exceptionToBeInjected)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean injectGatewayServerConnectionDelay(
        long transportRequestId,
        URI requestUri,
        RxDocumentServiceRequest serviceRequest,
        Utils.ValueHolder<Duration> delayToBeInjected) {
        for (IGatewayServerErrorInjector serverErrorInjector : faultInjectors) {
            if(serverErrorInjector.injectGatewayServerConnectionDelay(transportRequestId, requestUri, serviceRequest, delayToBeInjected)) {
                return true;
            }
        }
        return false;
    }
}
