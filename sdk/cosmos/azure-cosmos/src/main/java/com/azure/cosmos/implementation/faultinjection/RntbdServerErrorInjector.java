// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.faultinjection;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.directconnectivity.rntbd.IRequestRecord;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdRequestRecord;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class RntbdServerErrorInjector {
    private List<IServerErrorInjector> faultInjectors = new ArrayList<>();

    public void registerServerErrorInjector(IServerErrorInjector serverErrorInjector) {
        checkNotNull(serverErrorInjector, "Argument 'serverErrorInjector' can not be null");
        this.faultInjectors.add(serverErrorInjector);
    }

    public boolean injectRntbdServerResponseDelayBeforeProcessing(
        RntbdRequestRecord requestRecord,
        Consumer<Duration> writeRequestWithDelayConsumer) {

        Utils.ValueHolder<Duration> injectedDelay = new Utils.ValueHolder<>();
        for (IServerErrorInjector injector : this.faultInjectors) {
            if (injector.injectServerResponseDelayBeforeProcessing(
                this.createFaultInjectionRequestArgs(requestRecord), injectedDelay)) {

                writeRequestWithDelayConsumer.accept(injectedDelay.v);
                return true;
            }
        }

        return false;
    }

    public boolean injectRntbdServerResponseDelayAfterProcessing(RntbdRequestRecord requestRecord,
                                                                 Consumer<Duration> writeRequestWithDelayConsumer) {
        Utils.ValueHolder<Duration> injectedDelay = new Utils.ValueHolder<>();
        for (IServerErrorInjector injector : this.faultInjectors) {
            if (injector.injectServerResponseDelayAfterProcessing(
                this.createFaultInjectionRequestArgs(requestRecord), injectedDelay)) {

                writeRequestWithDelayConsumer.accept(injectedDelay.v);
                return true;
            }
        }

        return false;
    }

    public boolean injectRntbdServerResponseError(RntbdRequestRecord requestRecord) {

        Utils.ValueHolder<CosmosException> injectedException = new Utils.ValueHolder<>();
        for (IServerErrorInjector injector : this.faultInjectors) {
            if (injector.injectServerResponseError(
                this.createFaultInjectionRequestArgs(requestRecord), injectedException)) {

                requestRecord.completeExceptionally(injectedException.v);
                return true;
            }
        }

        return false;
    }

    public boolean injectRntbdServerConnectionDelay(
        IRequestRecord requestRecord,
        Consumer<Duration> openConnectionWithDelayConsumer) {

        Utils.ValueHolder<Duration> injectedDelay = new Utils.ValueHolder<>();
        for (IServerErrorInjector injector : this.faultInjectors) {
            if (injector.injectServerConnectionDelay(
                this.createFaultInjectionRequestArgs(requestRecord), injectedDelay)) {

                openConnectionWithDelayConsumer.accept(injectedDelay.v);
                return true;
            }
        }

        return false;
    }

    private RntbdFaultInjectionRequestArgs createFaultInjectionRequestArgs(RntbdRequestRecord requestRecord) {
        if (requestRecord == null) {
            return null;
        }

        return new RntbdFaultInjectionRequestArgs(
            requestRecord.args().transportRequestId(),
            requestRecord.args().physicalAddressUri().getURI(),
            requestRecord.args().physicalAddressUri().isPrimary(),
            requestRecord.args().serviceRequest());
    }

    private RntbdFaultInjectionRequestArgs createFaultInjectionRequestArgs(IRequestRecord requestRecord) {
        if (requestRecord == null) {
            return null;
        }

        return new RntbdFaultInjectionRequestArgs(
            requestRecord.getRequestId(),
            requestRecord.args().physicalAddressUri().getURI(),
            requestRecord.args().physicalAddressUri().isPrimary(),
            requestRecord.args().serviceRequest());
    }
}
