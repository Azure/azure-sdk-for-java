// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.faultinjection;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Utils;

import java.net.URI;
import java.time.Duration;

/***
 * Gateway server error injector.
 */
public interface IGatewayServerErrorInjector {

    boolean injectGatewayServerResponseDelayBeforeProcessing(
        long transportRequestId,
        URI requestUri,
        RxDocumentServiceRequest serviceRequest,
        Utils.ValueHolder<Duration> delayToBeInjected);

    boolean injectGatewayServerResponseDelayAfterProcessing(
        long transportRequestId,
        URI requestUri,
        RxDocumentServiceRequest serviceRequest,
        Utils.ValueHolder<Duration> delayToBeInjected);

    boolean injectGatewayServerResponseError(
        long transportRequestId,
        URI requestUri,
        RxDocumentServiceRequest serviceRequest,
        Utils.ValueHolder<CosmosException> exceptionToBeInjected);

    boolean injectGatewayServerConnectionDelay(
        long transportRequestId,
        URI requestUri,
        RxDocumentServiceRequest serviceRequest,
        Utils.ValueHolder<Duration> delayToBeInjected);
}
