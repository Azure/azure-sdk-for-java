package com.azure.monitor.opentelemetry.exporter.implementation.quickpulse;

import com.azure.monitor.opentelemetry.exporter.implementation.MetricDataMapper;
import com.azure.monitor.opentelemetry.exporter.implementation.logging.OperationLogger;
import com.azure.monitor.opentelemetry.exporter.implementation.models.TelemetryItem;
import io.opentelemetry.sdk.metrics.data.MetricData;

import java.util.Collection;
import java.util.function.Consumer;

import static com.azure.monitor.opentelemetry.exporter.implementation.utils.AzureMonitorMsgId.EXPORTER_MAPPING_ERROR;

public class QuickPulseMetricReceiver implements Runnable {

    private static QuickPulseHeaderInfo quickPulseHeaderInfo;
    private QuickPulseMetricReader quickPulseMetricReader;
    private QuickPulseDataCollector collector;
    private static final OperationLogger metricReceiverLogger
        = new OperationLogger(QuickPulseMetricReceiver.class, "Exporting metric");
    private final MetricDataMapper mapper;
    private final Consumer<TelemetryItem> quickPulseConsumer;

    public QuickPulseMetricReceiver(QuickPulseMetricReader quickPulseMetricReader, MetricDataMapper metricDataMapper,
        QuickPulseDataCollector collector) {
        this.quickPulseMetricReader = quickPulseMetricReader;
        this.mapper = metricDataMapper;
        this.collector = collector;
        this.quickPulseConsumer = telemetryItem -> {
            if (this.collector.isEnabled()) {
                this.collector.addOtelMetric(telemetryItem);
            }
        };
    }

    public static synchronized QuickPulseHeaderInfo getQuickPulseHeaderInfo() {
        return quickPulseHeaderInfo;
    }

    public static synchronized void setQuickPulseHeaderInfo(QuickPulseHeaderInfo info) {
        quickPulseHeaderInfo = info;
    }

    @Override
    public void run() {
        while (true) {

            Collection<MetricData> metrics = quickPulseMetricReader.collectAllMetrics();
            QuickPulseHeaderInfo headerInfo = getQuickPulseHeaderInfo();

            if (headerInfo == null || headerInfo.getQuickPulseStatus() != QuickPulseStatus.QP_IS_ON) {
                continue;
            }

            for (MetricData metricData : metrics) {
                try {
                    mapper.mapMetrics(metricData, quickPulseConsumer);
                    metricReceiverLogger.recordSuccess();
                } catch (Throwable t) {
                    metricReceiverLogger.recordFailure(t.getMessage(), t, EXPORTER_MAPPING_ERROR);
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}
