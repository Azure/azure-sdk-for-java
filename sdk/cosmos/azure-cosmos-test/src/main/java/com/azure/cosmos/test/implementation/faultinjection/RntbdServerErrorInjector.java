// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.test.implementation.faultinjection;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.directconnectivity.rntbd.IRequestRecord;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdRequestArgs;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdRequestRecord;
import com.azure.cosmos.implementation.faultinjection.IRntbdServerErrorInjector;

import java.time.Duration;
import java.util.function.Consumer;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/***
 * Fault injector which can handle {@link FaultInjectionServerErrorRule} with DIRECT connection type.
 */
public class RntbdServerErrorInjector implements IRntbdServerErrorInjector {
    private final FaultInjectionRuleStore ruleStore;

    public RntbdServerErrorInjector(FaultInjectionRuleStore ruleStore) {
        checkNotNull(ruleStore, "Argument 'ruleStore' can not be null");

        this.ruleStore = ruleStore;
    }

    @Override
    public boolean injectRntbdServerResponseDelayBeforeProcessing(
        RntbdRequestRecord requestRecord,
        Consumer<Duration> writeRequestWithDelayConsumer) {

        RntbdRequestArgs requestArgs = requestRecord.args();

        FaultInjectionServerErrorRule serverResponseDelayRule = this.ruleStore.findRntbdServerResponseDelayRule(requestArgs);
        if (serverResponseDelayRule != null) {

            if (serverResponseDelayRule.getResult() != null &&
                serverResponseDelayRule.getResult().getSuppressServiceRequests() != null &&
                !serverResponseDelayRule.getResult().getSuppressServiceRequests()) {

                // delay will be injected after processing the request
                return false;
            }

            requestArgs.serviceRequest().faultInjectionRequestContext
                .applyFaultInjectionRule(
                    requestRecord.transportRequestId(),
                    serverResponseDelayRule.getId());

            writeRequestWithDelayConsumer.accept(serverResponseDelayRule.getResult().getDelay());
            return true;
        }

        return false;
    }

    @Override
    public boolean injectRntbdServerResponseDelayAfterProcessing(RntbdRequestRecord requestRecord,
                                                                 Consumer<Duration> writeRequestWithDelayConsumer) {
        RntbdRequestArgs requestArgs = requestRecord.args();

        FaultInjectionServerErrorRule serverResponseDelayRule = this.ruleStore.findRntbdServerResponseDelayRule(requestArgs);
        if (serverResponseDelayRule != null) {

            if (serverResponseDelayRule.getResult() == null ||
                serverResponseDelayRule.getResult().getSuppressServiceRequests() == null ||
                serverResponseDelayRule.getResult().getSuppressServiceRequests()) {

                // delay was injected before processing the request
                return false;
            }

            requestArgs.serviceRequest().faultInjectionRequestContext
                .applyFaultInjectionRule(
                    requestRecord.transportRequestId(),
                    serverResponseDelayRule.getId());

            writeRequestWithDelayConsumer.accept(serverResponseDelayRule.getResult().getDelay());
            return true;
        }

        return false;
    }

    @Override
    public boolean injectRntbdServerResponseError(RntbdRequestRecord requestRecord) {
        RntbdRequestArgs requestArgs = requestRecord.args();

        FaultInjectionServerErrorRule serverResponseErrorRule =
            this.ruleStore.findRntbdServerResponseErrorRule(requestArgs);

        if (serverResponseErrorRule != null) {
            requestArgs.serviceRequest().faultInjectionRequestContext
                .applyFaultInjectionRule(
                    requestRecord.transportRequestId(),
                    serverResponseErrorRule.getId());

            CosmosException cause = serverResponseErrorRule.getInjectedServerError(requestArgs.serviceRequest());
            requestRecord.completeExceptionally(cause);
            return true;
        }

        return false;
    }

    @Override
    public boolean injectRntbdServerConnectionDelay(
        IRequestRecord requestRecord,
        Consumer<Duration> openConnectionWithDelayConsumer) {
        if (requestRecord == null) {
            return false;
        }

        RntbdRequestArgs requestArgs = requestRecord.args();

        FaultInjectionServerErrorRule serverConnectionDelayRule =
            this.ruleStore.findRntbdServerConnectionDelayRule(requestArgs);

        if (serverConnectionDelayRule != null) {
            requestArgs.serviceRequest().faultInjectionRequestContext
                .applyFaultInjectionRule(
                    requestRecord.getRequestId(),
                    serverConnectionDelayRule.getId());
            openConnectionWithDelayConsumer.accept(serverConnectionDelayRule.getResult().getDelay());
            return true;
        }

        return false;
    }
}
