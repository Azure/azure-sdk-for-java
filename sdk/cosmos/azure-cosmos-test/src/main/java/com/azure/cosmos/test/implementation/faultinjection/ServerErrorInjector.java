// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.test.implementation.faultinjection;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.faultinjection.FaultInjectionRequestArgs;
import com.azure.cosmos.implementation.faultinjection.IServerErrorInjector;

import java.time.Duration;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/***
 * Fault injector which can handle {@link FaultInjectionServerErrorRule} for both direct and gateway connection type.
 */
public class ServerErrorInjector implements IServerErrorInjector {
    private final FaultInjectionRuleStore ruleStore;

    public ServerErrorInjector(FaultInjectionRuleStore ruleStore) {
        checkNotNull(ruleStore, "Argument 'ruleStore' can not be null");

        this.ruleStore = ruleStore;
    }

    @Override
    public boolean injectServerResponseDelayBeforeProcessing(
        FaultInjectionRequestArgs faultInjectionRequestArgs,
        Utils.ValueHolder<Duration> injectedDelay) {

        FaultInjectionServerErrorRule serverResponseDelayRule = this.ruleStore.findServerResponseDelayRule(faultInjectionRequestArgs);
        if (serverResponseDelayRule != null) {

            if (serverResponseDelayRule.getResult() != null
                && serverResponseDelayRule.getResult().getSuppressServiceRequests() != null
                && !serverResponseDelayRule.getResult().getSuppressServiceRequests()) {

                // delay will be injected after processing the request
                return false;
            }

            faultInjectionRequestArgs.getServiceRequest().faultInjectionRequestContext
                .applyFaultInjectionRule(
                    faultInjectionRequestArgs.getTransportRequestId(),
                    serverResponseDelayRule.getId());

            injectedDelay.v = serverResponseDelayRule.getResult().getDelay();
            return true;
        }

        return false;
    }

    @Override
    public boolean injectServerResponseDelayAfterProcessing(
        FaultInjectionRequestArgs faultInjectionRequestArgs,
        Utils.ValueHolder<Duration> injectedDelay) {

        FaultInjectionServerErrorRule serverResponseDelayRule = this.ruleStore.findServerResponseDelayRule(faultInjectionRequestArgs);
        if (serverResponseDelayRule != null) {

            if (serverResponseDelayRule.getResult() == null
                || serverResponseDelayRule.getResult().getSuppressServiceRequests() == null
                || serverResponseDelayRule.getResult().getSuppressServiceRequests()) {

                // delay was injected before processing the request
                return false;
            }

            faultInjectionRequestArgs.getServiceRequest().faultInjectionRequestContext
                .applyFaultInjectionRule(
                    faultInjectionRequestArgs.getTransportRequestId(),
                    serverResponseDelayRule.getId());

            injectedDelay.v = serverResponseDelayRule.getResult().getDelay();
            return true;
        }

        return false;
    }

    @Override
    public boolean injectServerResponseError(
        FaultInjectionRequestArgs faultInjectionRequestArgs,
        Utils.ValueHolder<CosmosException> injectedException) {

        FaultInjectionServerErrorRule serverResponseErrorRule = this.ruleStore.findServerResponseErrorRule(faultInjectionRequestArgs);

        if (serverResponseErrorRule != null) {
            faultInjectionRequestArgs.getServiceRequest().faultInjectionRequestContext
                .applyFaultInjectionRule(
                    faultInjectionRequestArgs.getTransportRequestId(),
                    serverResponseErrorRule.getId());

            CosmosException cause = serverResponseErrorRule.getInjectedServerError(faultInjectionRequestArgs.getServiceRequest());
            injectedException.v = cause;
            return true;
        }

        return false;
    }

    @Override
    public boolean injectServerConnectionDelay(
        FaultInjectionRequestArgs faultInjectionRequestArgs,
        Utils.ValueHolder<Duration> injectedDelay) {
        if (faultInjectionRequestArgs == null) {
            return false;
        }

        FaultInjectionServerErrorRule serverConnectionDelayRule = this.ruleStore.findServerConnectionDelayRule(faultInjectionRequestArgs);

        if (serverConnectionDelayRule != null) {
            faultInjectionRequestArgs.getServiceRequest().faultInjectionRequestContext
                .applyFaultInjectionRule(
                    faultInjectionRequestArgs.getTransportRequestId(),
                    serverConnectionDelayRule.getId());
            injectedDelay.v = serverConnectionDelayRule.getResult().getDelay();
            return true;
        }

        return false;
    }
}
