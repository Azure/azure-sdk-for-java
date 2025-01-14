// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse;

import com.azure.core.util.logging.ClientLogger;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.MetricPoint;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.MonitoringDataPoint;
import org.slf4j.MDC;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;

import static com.azure.monitor.opentelemetry.autoconfigure.implementation.utils.AzureMonitorMsgId.QUICK_PULSE_SEND_ERROR;

class QuickPulseDataFetcher {

    private static final ClientLogger logger = new ClientLogger(QuickPulseDataFetcher.class);

    private final QuickPulseDataCollector collector;

    private final ArrayBlockingQueue<MonitoringDataPoint> sendQueue;

    private final String roleName;
    private final String instanceName;
    private final String machineName;
    private final String quickPulseId;

    private final String sdkVersion;

    public QuickPulseDataFetcher(QuickPulseDataCollector collector, ArrayBlockingQueue<MonitoringDataPoint> sendQueue,
        String roleName, String instanceName, String machineName, String quickPulseId, String sdkVersion) {
        this.collector = collector;
        this.sendQueue = sendQueue;
        this.roleName = roleName;
        this.instanceName = instanceName;
        this.machineName = machineName;
        this.quickPulseId = quickPulseId;
        this.sdkVersion = sdkVersion;
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
        point.setCollectionConfigurationErrors(counters.configErrors);
        return point;
    }

    private static List<MetricPoint> addMetricsToMonitoringDataPoint(QuickPulseDataCollector.FinalCounters counters) {
        List<MetricPoint> metricsList = new ArrayList<>();

        Map<String, Double> metrics = new HashMap<>();
        metrics.put("\\ApplicationInsights\\Requests/Sec", (double) counters.requests);
        if (counters.requests != 0) {
            metrics.put("\\ApplicationInsights\\Request Duration", counters.requestsDuration / counters.requests);
        }
        metrics.put("\\ApplicationInsights\\Requests Failed/Sec", (double) counters.unsuccessfulRequests);
        metrics.put("\\ApplicationInsights\\Requests Succeeded/Sec",
            (double) counters.requests - counters.unsuccessfulRequests);
        metrics.put("\\ApplicationInsights\\Dependency Calls/Sec", (double) counters.rdds);
        if (counters.rdds != 0) {
            metrics.put("\\ApplicationInsights\\Dependency Call Duration", counters.rddsDuration / counters.rdds);
        }
        metrics.put("\\ApplicationInsights\\Dependency Calls Failed/Sec", (double) counters.unsuccessfulRdds);
        metrics.put("\\ApplicationInsights\\Dependency Calls Succeeded/Sec",
            (double) counters.rdds - counters.unsuccessfulRequests);
        metrics.put("\\ApplicationInsights\\Exceptions/Sec", (double) counters.exceptions);
        metrics.put("\\Memory\\Committed Bytes", (double) counters.processPhysicalMemory); // TODO: remove old memory counter name when service side makes the UI change
        metrics.put("\\Process\\Physical Bytes", (double) counters.processPhysicalMemory);
        metrics.put("\\Processor(_Total)\\% Processor Time", counters.processNormalizedCpuUsage); // TODO: remove old cpu counter name when service side makes the UI change
        metrics.put("\\% Process\\Processor Time Normalized", counters.processNormalizedCpuUsage);

        metrics.putAll(counters.projections);

        for (Map.Entry<String, Double> entry : metrics.entrySet()) {
            MetricPoint point = new MetricPoint();
            point.setName(entry.getKey());
            point.setValue(entry.getValue());
            point.setWeight(1);
            metricsList.add(point);
        }

        return metricsList;
    }
}
