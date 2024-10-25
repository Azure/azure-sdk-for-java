// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.quickpulse;

import com.azure.core.http.HttpRequest;
import com.azure.core.util.logging.ClientLogger;
import com.azure.monitor.opentelemetry.exporter.implementation.quickpulse.model.QuickPulseEnvelope;
import com.azure.monitor.opentelemetry.exporter.implementation.quickpulse.model.QuickPulseMonitoringDataPoints;
import com.azure.monitor.opentelemetry.exporter.implementation.quickpulse.model.QuickPulseMetrics;
import com.azure.monitor.opentelemetry.exporter.implementation.quickpulse.model.swagger.models.MetricPoint;
import com.azure.monitor.opentelemetry.exporter.implementation.quickpulse.model.swagger.models.MonitoringDataPoint;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.Strings;
import org.slf4j.MDC;

import java.io.IOException;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.Supplier;

import static com.azure.monitor.opentelemetry.exporter.implementation.utils.AzureMonitorMsgId.QUICK_PULSE_SEND_ERROR;

class QuickPulseDataFetcher {

    private static final ClientLogger logger = new ClientLogger(QuickPulseDataFetcher.class);

    private final QuickPulseDataCollector collector;

    private final ArrayBlockingQueue<MonitoringDataPoint> sendQueue;
    private final QuickPulseNetworkHelper networkHelper = new QuickPulseNetworkHelper();
    private final String roleName;
    private final String instanceName;
    private final String machineName;
    private final String quickPulseId;

    private final String sdkVersion;

    public QuickPulseDataFetcher(QuickPulseDataCollector collector, ArrayBlockingQueue<MonitoringDataPoint> sendQueue, String roleName, String instanceName,
        String machineName, String quickPulseId) {
        this.collector = collector;
        this.sendQueue = sendQueue;
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
    public void prepareQuickPulseDataForSend() {
        try {
            QuickPulseDataCollector.FinalCounters counters = collector.getAndRestart();

            if (counters == null) {
                return;
            }
            //String endpointPrefix
                //= Strings.isNullOrEmpty(redirectedEndpoint) ? getQuickPulseEndpoint() : redirectedEndpoint;

            //HttpRequest request = networkHelper.buildRequest(currentDate, this.getEndpointUrl(endpointPrefix));
            //request.setBody(buildPostEntity(counters));
            MonitoringDataPoint point = buildMonitoringDataPoint(counters);

            if (!sendQueue.offer(point)) {
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
    /*String getEndpointUrl(String endpointPrefix) {
        return endpointPrefix + "/post?ikey=" + instrumentationKey.get();
    }*/

    // visible for testing
    /*String getQuickPulseEndpoint() {
        return endpointUrl.get().toString() + "QuickPulseService.svc";
    }*/

    /*private String buildPostEntity(QuickPulseDataCollector.FinalCounters counters) throws IOException {
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
        //postEnvelope.setInstrumentationKey(instrumentationKey.get());
        postEnvelope.setStreamId(quickPulseId);
        postEnvelope.setVersion(sdkVersion);
        postEnvelope.setTimeStamp("/Date(" + System.currentTimeMillis() + ")/");
        postEnvelope.setMetrics(addMetricsToQuickPulseEnvelope(counters));
        envelopes.add(postEnvelope);
        QuickPulseMonitoringDataPoints points = new QuickPulseMonitoringDataPoints(envelopes);
        // By default '/' is not escaped in JSON, so we need to escape it manually as the backend requires it.
        return points.toJsonString().replace("/", "\\/");
    }*/

    private MonitoringDataPoint buildMonitoringDataPoint(QuickPulseDataCollector.FinalCounters counters) {
        MonitoringDataPoint point = new MonitoringDataPoint();
        point.setDocuments(counters.documentList);
        point.setInstance(instanceName);
        point.setInvariantVersion(QuickPulse.QP_INVARIANT_VERSION);
        point.setMachineName(machineName);
        point.setRoleName(roleName);
        point.setStreamId(quickPulseId);
        point.setVersion(sdkVersion);
        point.setTimestamp(OffsetDateTime.now());
        point.setMetrics(addMetricsToMonitoringDataPoint(counters));
        return point;
    }

    private static List<MetricPoint>
        addMetricsToMonitoringDataPoint(QuickPulseDataCollector.FinalCounters counters) {
        List<MetricPoint> metricsList = new ArrayList<>();

        List<String> metricNames = new ArrayList<>(
            List.of("\\ApplicationInsights\\Requests/Sec",
                "\\ApplicationInsights\\Requests Failed/Sec",
                "\\ApplicationInsights\\Requests Succeeded/Sec",
                "\\ApplicationInsights\\Dependency Calls/Sec",
                "\\ApplicationInsights\\Dependency Calls Failed/Sec",
                "\\ApplicationInsights\\Dependency Calls Succeeded/Sec",
                "\\ApplicationInsights\\Exceptions/Sec",
                "\\Memory\\Committed Bytes", // TODO: remove old memory counter name when service side makes the UI change
                "\\Process\\Physical Bytes",
                "\\Processor(_Total)\\% Processor Time", // TODO: remove old cpu counter name when service side makes the UI change
                "\\% Process\\Processor Time Normalized")
        );

        List<Double> values = new ArrayList<>(
            List.of((double) counters.requests,
                (double)counters.unsuccessfulRequests,
                (double)counters.requests - counters.unsuccessfulRequests,
                (double)counters.rdds,
                (double)counters.unsuccessfulRdds,
                (double)counters.rdds - counters.unsuccessfulRequests,
                (double)counters.exceptions,
                (double)counters.processPhysicalMemory,
                (double)counters.processPhysicalMemory,
                counters.processNormalizedCpuUsage,
                counters.processNormalizedCpuUsage)
        );

        for (int i = 0; i < metricNames.size(); i++) {
            MetricPoint point = new MetricPoint();
            point.setName(metricNames.get(i));
            point.setValue(values.get(i));
            point.setWeight(1);
            metricsList.add(point);
        }

        if (counters.requests != 0) {
            MetricPoint point = new MetricPoint();
            point.setName("\\ApplicationInsights\\Request Duration");
            point.setValue(counters.requestsDuration / counters.requests);
            point.setWeight(1);
            metricsList.add(point);
        }

        if (counters.rdds != 0) {
            MetricPoint point = new MetricPoint();
            point.setName("\\ApplicationInsights\\Dependency Call Duration");
            point.setValue(counters.rddsDuration / counters.rdds);
            point.setWeight(1);
            metricsList.add(point);
        }

        return metricsList;
    }
}
