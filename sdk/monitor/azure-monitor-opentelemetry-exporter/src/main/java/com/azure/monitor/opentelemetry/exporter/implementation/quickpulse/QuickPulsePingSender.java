// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.quickpulse;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.logging.ClientLogger;
import com.azure.monitor.opentelemetry.exporter.implementation.logging.NetworkFriendlyExceptions;
import com.azure.monitor.opentelemetry.exporter.implementation.logging.OperationLogger;
import com.azure.monitor.opentelemetry.exporter.implementation.quickpulse.model.QuickPulseEnvelope;
import com.azure.monitor.opentelemetry.exporter.implementation.quickpulse.util.CustomCharacterEscapes;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.Strings;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import reactor.util.annotation.Nullable;

import java.net.URL;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static com.azure.monitor.opentelemetry.exporter.implementation.utils.AzureMonitorMsgId.QUICK_PULSE_PING_ERROR;

class QuickPulsePingSender {

    private static final ClientLogger logger = new ClientLogger(QuickPulsePingSender.class);

    private static final ObjectMapper mapper;

    private static final OperationLogger operationLogger =
        new OperationLogger(QuickPulsePingSender.class, "Pinging live metrics endpoint");

    // TODO (kryalama) do we still need this AtomicBoolean, or can we use throttling built in to the
    //  operationLogger?
    private static final AtomicBoolean friendlyExceptionThrown = new AtomicBoolean();

    static {
        mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.getFactory().setCharacterEscapes(new CustomCharacterEscapes());
    }

    private final HttpPipeline httpPipeline;
    private final QuickPulseNetworkHelper networkHelper = new QuickPulseNetworkHelper();
    private volatile QuickPulseEnvelope pingEnvelope; // cached for performance

    private final Supplier<URL> endpointUrl;
    private final Supplier<String> instrumentationKey;
    private final String roleName;
    private final String instanceName;
    private final String machineName;
    private final String quickPulseId;
    private long lastValidTransmission = 0;
    private final String sdkVersion;

    QuickPulsePingSender(
        HttpPipeline httpPipeline,
        Supplier<URL> endpointUrl,
        Supplier<String> instrumentationKey,
        String roleName,
        String instanceName,
        String machineName,
        String quickPulseId,
        String sdkVersion) {
        this.httpPipeline = httpPipeline;
        this.endpointUrl = endpointUrl;
        this.instrumentationKey = instrumentationKey;
        this.roleName = roleName;
        this.instanceName = instanceName;
        this.machineName = machineName;
        this.quickPulseId = quickPulseId;
        this.sdkVersion = sdkVersion;
    }

    QuickPulseHeaderInfo ping(String redirectedEndpoint) {
        System.out.println("QuickPulsePingSender.ping");
        System.out.println("redirectedEndpoint = " + redirectedEndpoint);
        String instrumentationKey = getInstrumentationKey();
        System.out.println("instrumentationKey = " + instrumentationKey);
        if (Strings.isNullOrEmpty(instrumentationKey)) {
            System.out.println("QuickPulsePingSender.ping. Missing instrumentation key");
            // Quick Pulse Ping uri will be null when the instrumentation key is null. When that happens,
            // turn off quick pulse.
            return new QuickPulseHeaderInfo(QuickPulseStatus.QP_IS_OFF);
        }

        Date currentDate = new Date();
        String endpointPrefix =
            Strings.isNullOrEmpty(redirectedEndpoint) ? getQuickPulseEndpoint() : redirectedEndpoint;
        System.out.println("endpointPrefix = " + endpointPrefix);
        HttpRequest request =
            networkHelper.buildPingRequest(
                currentDate,
                getQuickPulsePingUri(endpointPrefix),
                quickPulseId,
                machineName,
                roleName,
                instanceName);

        long sendTime = System.nanoTime();
        HttpResponse response = null;
        try {
            request.setBody(buildPingEntity(currentDate.getTime()));
            if(httpPipeline == null) {
                System.out.println("httpPipeline is null");
                return new QuickPulseHeaderInfo(QuickPulseStatus.QP_IS_OFF);
            }
            System.out.println("QuickPulsePingSender.ping. Sending request");
            response = httpPipeline.send(request).block();
            System.out.println("QuickPulsePingSender.ping. Response received");
            if (response == null) {
                System.out.println("QuickPulsePingSender.ping. Response is null");
                // this shouldn't happen, the mono should complete with a response or a failure
                throw new AssertionError("http response mono returned empty");
            }

            if (networkHelper.isSuccess(response)) {
                System.out.println("QuickPulsePingSender.ping. Response is success");
                QuickPulseHeaderInfo quickPulseHeaderInfo = networkHelper.getQuickPulseHeaderInfo(response);
                QuickPulseStatus quickPulseStatus = quickPulseHeaderInfo.getQuickPulseStatus();
                System.out.println("quickPulseStatus = " + quickPulseStatus);
                switch (quickPulseStatus) {
                    case QP_IS_OFF:
                    case QP_IS_ON:
                        lastValidTransmission = sendTime;
                        operationLogger.recordSuccess();
                        return quickPulseHeaderInfo;

                    default:
                        break;
                }
            }
            else {
                System.out.println("QuickPulsePingSender.ping. Response is not success");
            }
        } catch (Throwable t) {
            if (!NetworkFriendlyExceptions.logSpecialOneTimeFriendlyException(
                t, getQuickPulseEndpoint(), friendlyExceptionThrown, logger)) {
                System.out.println("QuickPulsePingSender.ping. Exception occurred");
                operationLogger.recordFailure(
                    t.getMessage() + " (" + endpointPrefix + ")", t, QUICK_PULSE_PING_ERROR);
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

    private String buildPingEntity(long timeInMillis) throws JsonProcessingException {
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
        return mapper.writeValueAsString(pingEnvelope);
    }

    private QuickPulseHeaderInfo onPingError(long sendTime) {
        System.out.println("QuickPulsePingSender.onPingError. Handling ping error");
        double timeFromLastValidTransmission = (sendTime - lastValidTransmission) / 1000000000.0;
        if (timeFromLastValidTransmission >= 60.0) {
            System.out.println("QuickPulsePingSender.onPingError. Time from last valid transmission is greater than 60 seconds");
            return new QuickPulseHeaderInfo(QuickPulseStatus.ERROR);
        }
        System.out.println("QuickPulsePingSender.onPingError. Time from last valid transmission is less than 60 seconds");
        return new QuickPulseHeaderInfo(QuickPulseStatus.QP_IS_OFF);
    }
}
