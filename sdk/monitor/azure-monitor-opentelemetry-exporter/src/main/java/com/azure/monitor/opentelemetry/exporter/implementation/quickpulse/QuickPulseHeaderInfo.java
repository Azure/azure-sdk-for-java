// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.quickpulse;

import reactor.util.annotation.Nullable;

class QuickPulseHeaderInfo {

    private final QuickPulseStatus quickPulseStatus;
    @Nullable
    private final String qpsServiceEndpointRedirect;
    private final long qpsServicePollingInterval;

    QuickPulseHeaderInfo(QuickPulseStatus quickPulseStatus, @Nullable String qpsServiceEndpointRedirect,
        long qpsServicePollingIntervalHint) {

        this.quickPulseStatus = quickPulseStatus;
        this.qpsServiceEndpointRedirect = qpsServiceEndpointRedirect;
        this.qpsServicePollingInterval = qpsServicePollingIntervalHint;
    }

    QuickPulseHeaderInfo(QuickPulseStatus quickPulseStatus) {
        this.quickPulseStatus = quickPulseStatus;
        this.qpsServiceEndpointRedirect = null;
        this.qpsServicePollingInterval = -1;
    }

    long getQpsServicePollingInterval() {
        return qpsServicePollingInterval;
    }

    String getQpsServiceEndpointRedirect() {
        return qpsServiceEndpointRedirect;
    }

    QuickPulseStatus getQuickPulseStatus() {
        return quickPulseStatus;
    }
}
