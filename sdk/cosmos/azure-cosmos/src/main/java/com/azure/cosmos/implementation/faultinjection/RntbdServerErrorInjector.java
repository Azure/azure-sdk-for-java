// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.faultinjection;

import com.azure.cosmos.implementation.directconnectivity.rntbd.IRequestRecord;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdRequestRecord;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class RntbdServerErrorInjector implements IRntbdServerErrorInjector {
    private List<IRntbdServerErrorInjector> faultInjectors = new ArrayList<>();

    public void registerServerErrorInjector(IRntbdServerErrorInjector serverErrorInjector) {
        checkNotNull(serverErrorInjector, "Argument 'serverErrorInjector' can not be null");
        this.faultInjectors.add(serverErrorInjector);
    }

    @Override
    public boolean injectRntbdServerResponseDelayBeforeProcessing(
        RntbdRequestRecord requestRecord,
        Consumer<Duration> writeRequestWithDelayConsumer) {

        for (IRntbdServerErrorInjector injector : this.faultInjectors) {
            if (injector.injectRntbdServerResponseDelayBeforeProcessing(requestRecord, writeRequestWithDelayConsumer)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean injectRntbdServerResponseDelayAfterProcessing(RntbdRequestRecord requestRecord,
                                                                 Consumer<Duration> writeRequestWithDelayConsumer) {
        for (IRntbdServerErrorInjector injector : this.faultInjectors) {
            if (injector.injectRntbdServerResponseDelayAfterProcessing(requestRecord, writeRequestWithDelayConsumer)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean injectRntbdServerResponseError(RntbdRequestRecord requestRecord) {

        for (IRntbdServerErrorInjector injector : this.faultInjectors) {
            if (injector.injectRntbdServerResponseError(requestRecord)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean injectRntbdServerConnectionDelay(
        IRequestRecord requestRecord,
        Consumer<Duration> openConnectionWithDelayConsumer) {

        for (IRntbdServerErrorInjector injector : this.faultInjectors) {
            if (injector.injectRntbdServerConnectionDelay(requestRecord, openConnectionWithDelayConsumer)) {
                return true;
            }
        }

        return false;
    }
}
