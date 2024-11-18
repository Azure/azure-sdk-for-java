// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.rest.Response;
import com.azure.core.util.logging.ClientLogger;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.logging.NetworkFriendlyExceptions;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.logging.OperationLogger;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.LiveMetricsRestAPIsForClientSDKs;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.CollectionConfigurationInfo;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.IsSubscribedHeaders;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.MonitoringDataPoint;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.utils.Strings;
import reactor.util.annotation.Nullable;

import java.net.URL;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static com.azure.monitor.opentelemetry.autoconfigure.implementation.utils.AzureMonitorMsgId.QUICK_PULSE_PING_ERROR;

class QuickPulsePingSender {
    private static final long TICKS_AT_EPOCH = 621355968000000000L;

    private static final ClientLogger logger = new ClientLogger(QuickPulsePingSender.class);

    private static final OperationLogger operationLogger
        = new OperationLogger(QuickPulsePingSender.class, "Pinging live metrics endpoint");

    // TODO (kryalama) do we still need this AtomicBoolean, or can we use throttling built in to the
    //  operationLogger?
    private static final AtomicBoolean friendlyExceptionThrown = new AtomicBoolean();

    // TODO: remove httpPipeline if not needed
    //private final HttpPipeline httpPipeline;
    private final LiveMetricsRestAPIsForClientSDKs liveMetricsRestAPIsForClientSDKs;
    //private final QuickPulseNetworkHelper networkHelper = new QuickPulseNetworkHelper();
    // private volatile QuickPulseEnvelope pingEnvelope; // cached for performance

    private final Supplier<URL> endpointUrl;
    private final Supplier<String> instrumentationKey;
    private final String roleName;
    private final String instanceName;
    private final String machineName;
    private final String quickPulseId;
    private long lastValidRequestTimeNs = System.nanoTime();
    private final String sdkVersion;

    private IsSubscribedHeaders responseHeaders;

    private static final HttpHeaderName QPS_STATUS_HEADER = HttpHeaderName.fromString("x-ms-qps-subscribed");

    QuickPulsePingSender(LiveMetricsRestAPIsForClientSDKs liveMetricsRestAPIsForClientSDKs, Supplier<URL> endpointUrl,
        Supplier<String> instrumentationKey, String roleName, String instanceName, String machineName,
        String quickPulseId, String sdkVersion) {
        this.liveMetricsRestAPIsForClientSDKs = liveMetricsRestAPIsForClientSDKs;
        this.endpointUrl = endpointUrl;
        this.instrumentationKey = instrumentationKey;
        this.roleName = roleName;
        this.instanceName = instanceName;
        this.machineName = machineName;
        this.quickPulseId = quickPulseId;
        this.sdkVersion = sdkVersion;
        this.responseHeaders = null;
    }

    IsSubscribedHeaders ping(String redirectedEndpoint) {
        String instrumentationKey = getInstrumentationKey();
        if (Strings.isNullOrEmpty(instrumentationKey)) {
            // Quick Pulse Ping uri will be null when the instrumentation key is null. When that happens,
            // turn off quick pulse.
            HttpHeaders headers = new HttpHeaders();
            headers.add(QPS_STATUS_HEADER, "false");
            return new IsSubscribedHeaders(headers);
        }

        Date currentDate = new Date();
        long transmissionTimeInTicks = currentDate.getTime() * 10000 + TICKS_AT_EPOCH;
        String endpointPrefix
            = Strings.isNullOrEmpty(redirectedEndpoint) ? getQuickPulseEndpoint() : redirectedEndpoint;

        long sendTime = System.nanoTime();

        try {
            Response<CollectionConfigurationInfo> responseMono
                = liveMetricsRestAPIsForClientSDKs
                    .isSubscribedNoCustomHeadersWithResponseAsync(endpointPrefix, instrumentationKey,
                        transmissionTimeInTicks, machineName, instanceName, quickPulseId, roleName,
                        String.valueOf(QuickPulse.QP_INVARIANT_VERSION), "", buildMonitoringDataPoint())
                    .block();
            if (responseMono == null) {
                // this shouldn't happen, the mono should complete with a response or a failure
                throw new AssertionError("http response mono returned empty");
            }

            // If we get to this point the api returned http 200
            responseHeaders = new IsSubscribedHeaders(responseMono.getHeaders());
            String isSubscribed = responseHeaders.getXMsQpsSubscribed();
            if (!Strings.isNullOrEmpty(isSubscribed)) {
                operationLogger.recordSuccess(); // when does this need to be called
                lastValidRequestTimeNs = sendTime;
            }

            return responseHeaders;
        } catch (RuntimeException e) {
            // 404 landed here
            Throwable t = e.getCause();
            if (!NetworkFriendlyExceptions.logSpecialOneTimeFriendlyException(t, getQuickPulseEndpoint(),
                friendlyExceptionThrown, logger)) {
                operationLogger.recordFailure(t.getMessage() + " (" + endpointPrefix + ")", t, QUICK_PULSE_PING_ERROR);
            }
        }
        return onPingError(sendTime);
    }

    @Nullable
    // visible for testing
    public String getInstrumentationKey() {
        return instrumentationKey.get();
    }

    // visible for testing
    String getQuickPulseEndpoint() {
        return endpointUrl.get().toString();
    }

    private MonitoringDataPoint buildMonitoringDataPoint() {
        MonitoringDataPoint dataPoint = new MonitoringDataPoint();
        dataPoint.setInstance(instanceName);
        dataPoint.setInvariantVersion(QuickPulse.QP_INVARIANT_VERSION);
        dataPoint.setMachineName(machineName);
        dataPoint.setRoleName(roleName);
        dataPoint.setStreamId(quickPulseId);
        dataPoint.setVersion(sdkVersion);

        return dataPoint;
    }

    private IsSubscribedHeaders onPingError(long sendTime) {
        HttpHeaders headers = new HttpHeaders();

        double timeFromlastValidRequestTimeNs = (sendTime - lastValidRequestTimeNs) / 1000000000.0;
        if (timeFromlastValidRequestTimeNs >= 60.0) {
            return new IsSubscribedHeaders(headers); // all headers null
        }
        headers.add(QPS_STATUS_HEADER, "false");
        return new IsSubscribedHeaders(headers); // status header set to false
    }

    public long getLastValidPingTransmissionNs() {
        return this.lastValidRequestTimeNs;
    }

    public void resetLastValidRequestTimeNs(long lastValidPostTrasmission) {
        this.lastValidRequestTimeNs = lastValidPostTrasmission;
    }
}
