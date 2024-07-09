// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.quickpulse;

import com.azure.core.http.HttpRequest;
import com.azure.core.util.logging.ClientLogger;
import com.azure.monitor.opentelemetry.exporter.implementation.quickpulse.model.QuickPulseEnvelope;
import com.azure.monitor.opentelemetry.exporter.implementation.quickpulse.model.QuickPulseMetrics;
import com.azure.monitor.opentelemetry.exporter.implementation.quickpulse.util.CustomCharacterEscapes;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.Strings;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.MDC;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.Supplier;

import static com.azure.monitor.opentelemetry.exporter.implementation.utils.AzureMonitorMsgId.QUICK_PULSE_SEND_ERROR;

class QuickPulseDataFetcher {

    private static final ClientLogger logger = new ClientLogger(QuickPulseDataFetcher.class);

    private static final ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.getFactory().setCharacterEscapes(new CustomCharacterEscapes());
    }

    private final QuickPulseDataCollector collector;

    private final ArrayBlockingQueue<HttpRequest> sendQueue;
    private final QuickPulseNetworkHelper networkHelper = new QuickPulseNetworkHelper();
    private QuickPulseConfiguration quickPulseConfiguration = QuickPulseConfiguration.getInstance();

    private final Supplier<URL> endpointUrl;
    private final Supplier<String> instrumentationKey;
    private final String roleName;
    private final String instanceName;
    private final String machineName;
    private final String quickPulseId;

    private final String sdkVersion;

    public QuickPulseDataFetcher(
        QuickPulseDataCollector collector,
        ArrayBlockingQueue<HttpRequest> sendQueue,
        Supplier<URL> endpointUrl,
        Supplier<String> instrumentationKey,
        String roleName,
        String instanceName,
        String machineName,
        String quickPulseId) {
        this.collector = collector;
        this.sendQueue = sendQueue;
        this.endpointUrl = endpointUrl;
        this.instrumentationKey = instrumentationKey;
        this.roleName = roleName;
        this.instanceName = instanceName;
        this.machineName = machineName;
        this.quickPulseId = quickPulseId;

        sdkVersion = getCurrentSdkVersion();
    }

    /**
     * Returns SDK Version from properties.
     */
    // visible for testing
    // TODO krishna to get sdk version
    String getCurrentSdkVersion() {
        return "unknown";
    }

    @SuppressWarnings("try")
    public void prepareQuickPulseDataForSend(String redirectedEndpoint) {
        try {
            QuickPulseDataCollector.FinalCounters counters = collector.getAndRestart();

            if (counters == null) {
                return;
            }

            Date currentDate = new Date();
            String endpointPrefix =
                Strings.isNullOrEmpty(redirectedEndpoint) ? getQuickPulseEndpoint() : redirectedEndpoint;
            HttpRequest request =
                networkHelper.buildRequest(currentDate, this.getEndpointUrl(endpointPrefix), quickPulseConfiguration.getEtag());
            request.setBody(buildPostEntity(counters));

            if (!sendQueue.offer(request)) {
                logger.verbose("Quick Pulse send queue is full");
            }
        } catch (Throwable e) {
            if (e instanceof Error) {
                throw (Error) e;
            }
            try {
                try (MDC.MDCCloseable ignored = QUICK_PULSE_SEND_ERROR.makeActive()) {
                    logger.error("Quick Pulse failed to prepare data for send", e);
                }
            } catch (Throwable t2) {
                if (t2 instanceof Error) {
                    throw (Error) t2;
                }
            }
        }
    }

    // visible for testing
    String getEndpointUrl(String endpointPrefix) {
        return endpointPrefix + "/post?ikey=" + instrumentationKey.get();
    }

    // visible for testing
    String getQuickPulseEndpoint() {
        return endpointUrl.get().toString() + "QuickPulseService.svc";
    }

    private String buildPostEntity(QuickPulseDataCollector.FinalCounters counters)
        throws JsonProcessingException {
        List<QuickPulseEnvelope> envelopes = new ArrayList<>();
        QuickPulseEnvelope postEnvelope = new QuickPulseEnvelope();
        postEnvelope.setDocuments(counters.documentList);
        postEnvelope.setInstance(instanceName);
        postEnvelope.setInvariantVersion(QuickPulse.QP_INVARIANT_VERSION);
        postEnvelope.setMachineName(machineName);
        // FIXME (heya) what about azure functions consumption plan where role name not available yet?
        postEnvelope.setRoleName(roleName);
        // For historical reasons, instrumentation key is provided both in the query string and
        // envelope.
        postEnvelope.setInstrumentationKey(instrumentationKey.get());
        postEnvelope.setStreamId(quickPulseId);
        postEnvelope.setVersion(sdkVersion);
        postEnvelope.setTimeStamp("/Date(" + System.currentTimeMillis() + ")/");
        postEnvelope.setMetrics(addMetricsToQuickPulseEnvelope(counters, collector.retrieveOpenTelMetrics()));
        envelopes.add(postEnvelope);
        return mapper.writeValueAsString(envelopes);
    }

    private static List<QuickPulseMetrics> addMetricsToQuickPulseEnvelope(
        QuickPulseDataCollector.FinalCounters counters,
        List<QuickPulseMetrics> openTelemetryMetrics) {
        List<QuickPulseMetrics> metricsList = new ArrayList<>();
        metricsList.add(
            new QuickPulseMetrics("\\ApplicationInsights\\Requests/Sec", counters.requests, 1));
        if (counters.requests != 0) {
            metricsList.add(
                new QuickPulseMetrics(
                    "\\ApplicationInsights\\Request Duration",
                    counters.requestsDuration / counters.requests,
                    counters.requests));
        }
        metricsList.add(
            new QuickPulseMetrics(
                "\\ApplicationInsights\\Requests Failed/Sec", counters.unsuccessfulRequests, 1));
        metricsList.add(
            new QuickPulseMetrics(
                "\\ApplicationInsights\\Requests Succeeded/Sec",
                counters.requests - counters.unsuccessfulRequests,
                1));
        metricsList.add(
            new QuickPulseMetrics("\\ApplicationInsights\\Dependency Calls/Sec", counters.rdds, 1));
        if (counters.rdds != 0) {
            metricsList.add(
                new QuickPulseMetrics(
                    "\\ApplicationInsights\\Dependency Call Duration",
                    counters.rddsDuration / counters.rdds,
                    (int) counters.rdds));
        }
        metricsList.add(
            new QuickPulseMetrics(
                "\\ApplicationInsights\\Dependency Calls Failed/Sec", counters.unsuccessfulRdds, 1));
        metricsList.add(
            new QuickPulseMetrics(
                "\\ApplicationInsights\\Dependency Calls Succeeded/Sec",
                counters.rdds - counters.unsuccessfulRdds,
                1));
        metricsList.add(
            new QuickPulseMetrics("\\ApplicationInsights\\Exceptions/Sec", counters.exceptions, 1));
        metricsList.add(
            new QuickPulseMetrics("\\Memory\\Committed Bytes", counters.memoryCommitted, 1));
        metricsList.add(
            new QuickPulseMetrics("\\Processor(_Total)\\% Processor Time", counters.cpuUsage, 1));
        metricsList.addAll(openTelemetryMetrics);
        return metricsList;
    }
}
