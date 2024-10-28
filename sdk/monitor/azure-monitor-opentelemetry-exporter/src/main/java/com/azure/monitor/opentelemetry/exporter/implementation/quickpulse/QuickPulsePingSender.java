// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.quickpulse;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.rest.Response;
import com.azure.core.util.logging.ClientLogger;
import com.azure.monitor.opentelemetry.exporter.implementation.logging.NetworkFriendlyExceptions;
import com.azure.monitor.opentelemetry.exporter.implementation.logging.OperationLogger;
import com.azure.monitor.opentelemetry.exporter.implementation.quickpulse.model.QuickPulseEnvelope;
import com.azure.monitor.opentelemetry.exporter.implementation.quickpulse.model.swagger.LiveMetricsRestAPIsForClientSDKs;
import com.azure.monitor.opentelemetry.exporter.implementation.quickpulse.model.swagger.models.CollectionConfigurationInfo;
import com.azure.monitor.opentelemetry.exporter.implementation.quickpulse.model.swagger.models.IsSubscribedHeaders;
import com.azure.monitor.opentelemetry.exporter.implementation.quickpulse.model.swagger.models.MonitoringDataPoint;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.Strings;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

//import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static com.azure.monitor.opentelemetry.exporter.implementation.utils.AzureMonitorMsgId.QUICK_PULSE_PING_ERROR;

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
    private long lastValidTransmission = 0;
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
        Mono<Response<CollectionConfigurationInfo>> responseMono = null;

        try {
            responseMono = liveMetricsRestAPIsForClientSDKs.isSubscribedNoCustomHeadersWithResponseAsync(endpointPrefix,
                instrumentationKey, transmissionTimeInTicks, machineName, instanceName, quickPulseId, roleName,
                String.valueOf(QuickPulse.QP_INVARIANT_VERSION), "", buildMonitoringDataPoint());
            responseMono.doOnNext(response -> { //do on Success or do on Next??
                responseHeaders = new IsSubscribedHeaders(response.getHeaders());
                String isSubscribed = responseHeaders.getXMsQpsSubscribed();
                if (isSubscribed.equalsIgnoreCase("true")) {
                    lastValidTransmission = sendTime;
                    operationLogger.recordSuccess();
                    // consume response body here
                }
            });
            return responseHeaders;
        } catch (Throwable t) {
            if (!NetworkFriendlyExceptions.logSpecialOneTimeFriendlyException(t, getQuickPulseEndpoint(),
                friendlyExceptionThrown, logger)) {
                operationLogger.recordFailure(t.getMessage() + " (" + endpointPrefix + ")", t, QUICK_PULSE_PING_ERROR);
            }
        }
        return onPingError(sendTime);
    }

    // visible for testing
    String getQuickPulsePingUri(String endpointPrefix) {
        return endpointPrefix + "/ping?ikey=" + getInstrumentationKey();
    }

    @Nullable
    private String getInstrumentationKey() {
        return instrumentationKey.get();
    }

    // visible for testing
    String getQuickPulseEndpoint() {
        return endpointUrl.get().toString() + "QuickPulseService.svc";
    }

    /*private String buildPingEntity(long timeInMillis) throws IOException {
        if (pingEnvelope == null) {
            pingEnvelope = new QuickPulseEnvelope();
            pingEnvelope.setInstance(instanceName);
            pingEnvelope.setInvariantVersion(QuickPulse.QP_INVARIANT_VERSION);
            pingEnvelope.setMachineName(machineName);
            pingEnvelope.setRoleName(roleName);
            pingEnvelope.setStreamId(quickPulseId);
            pingEnvelope.setVersion(sdkVersion);
        }
        pingEnvelope.setTimeStamp("/Date(" + timeInMillis + ")/");
    
        // By default '/' is not escaped in JSON, so we need to escape it manually as the backend requires it.
        return pingEnvelope.toJsonString().replace("/", "\\/");
    }*/

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

        double timeFromLastValidTransmission = (sendTime - lastValidTransmission) / 1000000000.0;
        if (timeFromLastValidTransmission >= 60.0) {
            return new IsSubscribedHeaders(headers); // all headers null
        }
        headers.add(QPS_STATUS_HEADER, "false");
        return new IsSubscribedHeaders(headers); // status header set to false
    }
}
