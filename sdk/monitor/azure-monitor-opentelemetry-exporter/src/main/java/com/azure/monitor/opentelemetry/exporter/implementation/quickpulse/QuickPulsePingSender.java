// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.quickpulse;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.monitor.opentelemetry.exporter.implementation.logging.NetworkFriendlyExceptions;
import com.azure.monitor.opentelemetry.exporter.implementation.logging.OperationLogger;
import com.azure.monitor.opentelemetry.exporter.implementation.quickpulse.model.QuickPulseEnvelope;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.Strings;
import reactor.util.annotation.Nullable;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static com.azure.monitor.opentelemetry.exporter.implementation.utils.AzureMonitorMsgId.QUICK_PULSE_PING_ERROR;

class QuickPulsePingSender {

    private static final ClientLogger logger = new ClientLogger(QuickPulsePingSender.class);

    private static final OperationLogger operationLogger
        = new OperationLogger(QuickPulsePingSender.class, "Pinging live metrics endpoint");

    // TODO (kryalama) do we still need this AtomicBoolean, or can we use throttling built in to the
    //  operationLogger?
    private static final AtomicBoolean friendlyExceptionThrown = new AtomicBoolean();

    private final HttpPipeline httpPipeline;
    private final QuickPulseNetworkHelper networkHelper = new QuickPulseNetworkHelper();
    private QuickPulseConfiguration quickPulseConfiguration;
    private volatile QuickPulseEnvelope pingEnvelope; // cached for performance

    private final Supplier<URL> endpointUrl;
    private final Supplier<String> instrumentationKey;
    private final String roleName;
    private final String instanceName;
    private final String machineName;
    private final String quickPulseId;
    private long lastValidTransmission = 0;
    private final String sdkVersion;

    QuickPulsePingSender(HttpPipeline httpPipeline, Supplier<URL> endpointUrl, Supplier<String> instrumentationKey,
        String roleName, String instanceName, String machineName, String quickPulseId, String sdkVersion,
        QuickPulseConfiguration quickPulseConfiguration) {
        this.httpPipeline = httpPipeline;
        this.endpointUrl = endpointUrl;
        this.instrumentationKey = instrumentationKey;
        this.roleName = roleName;
        this.instanceName = instanceName;
        this.machineName = machineName;
        this.quickPulseId = quickPulseId;
        this.sdkVersion = sdkVersion;
        this.quickPulseConfiguration = quickPulseConfiguration;
    }

    QuickPulseHeaderInfo ping(String redirectedEndpoint) {
        String instrumentationKey = getInstrumentationKey();
        if (Strings.isNullOrEmpty(instrumentationKey)) {
            // Quick Pulse Ping uri will be null when the instrumentation key is null. When that happens,
            // turn off quick pulse.
            return new QuickPulseHeaderInfo(QuickPulseStatus.QP_IS_OFF);
        }

        Date currentDate = new Date();
        String endpointPrefix
            = Strings.isNullOrEmpty(redirectedEndpoint) ? getQuickPulseEndpoint() : redirectedEndpoint;
        HttpRequest request = networkHelper.buildPingRequest(currentDate, getQuickPulsePingUri(endpointPrefix),
            quickPulseId, machineName, roleName, instanceName);

        long sendTime = System.nanoTime();
        HttpResponse response = null;
        try {
            request.setBody(buildPingEntity(currentDate.getTime()));
            response = httpPipeline.sendSync(request, Context.NONE);
            if (response == null) {
                // this shouldn't happen, the mono should complete with a response or a failure
                throw new AssertionError("http response mono returned empty");
            }

            if (networkHelper.isSuccess(response)) {
                QuickPulseHeaderInfo quickPulseHeaderInfo = networkHelper.getQuickPulseHeaderInfo(response);
                switch (quickPulseHeaderInfo.getQuickPulseStatus()) {
                    case QP_IS_OFF:
                    case QP_IS_ON:
                        lastValidTransmission = sendTime;
                        String etagValue = networkHelper.getEtagHeaderValue(response);
                        if (etagValue != null) {
                            ConcurrentHashMap<String, ArrayList<QuickPulseConfiguration.DerivedMetricInfo>> otelMetrics
                                = quickPulseConfiguration.parseDerivedMetrics(response);
                            quickPulseConfiguration.updateConfig(etagValue, otelMetrics);
                        }
                        operationLogger.recordSuccess();
                        return quickPulseHeaderInfo;

                    default:
                        break;
                }
            }
        } catch (Throwable t) {
            if (!NetworkFriendlyExceptions.logSpecialOneTimeFriendlyException(t, getQuickPulseEndpoint(),
                friendlyExceptionThrown, logger)) {
                operationLogger.recordFailure(t.getMessage() + " (" + endpointPrefix + ")", t, QUICK_PULSE_PING_ERROR);
            }
        } finally {
            if (response != null) {

                // need to consume the body or close the response, otherwise get netty ByteBuf leak
                // warnings:
                // io.netty.util.ResourceLeakDetector - LEAK: ByteBuf.release() was not called before
                // it's garbage-collected (see https://github.com/Azure/azure-sdk-for-java/issues/10467)
                response.close();
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

    private String buildPingEntity(long timeInMillis) throws IOException {
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
    }

    private QuickPulseHeaderInfo onPingError(long sendTime) {
        double timeFromLastValidTransmission = (sendTime - lastValidTransmission) / 1000000000.0;
        if (timeFromLastValidTransmission >= 60.0) {
            return new QuickPulseHeaderInfo(QuickPulseStatus.ERROR);
        }

        return new QuickPulseHeaderInfo(QuickPulseStatus.QP_IS_OFF);
    }
}
