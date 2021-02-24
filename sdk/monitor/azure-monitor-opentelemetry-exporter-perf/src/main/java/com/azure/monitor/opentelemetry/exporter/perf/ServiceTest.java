package com.azure.monitor.opentelemetry.exporter.perf;

import com.azure.monitor.opentelemetry.exporter.AzureMonitorExporterBuilder;
import com.azure.monitor.opentelemetry.exporter.AzureMonitorTraceExporter;
import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.PerfStressTest;

public abstract class ServiceTest<TOptions extends PerfStressOptions> extends PerfStressTest<TOptions> {
    protected final AzureMonitorTraceExporter traceExporter;

    /**
     * Creates an instance of performance test.
     * @param options the options configured for the test.
     */
    public ServiceTest(TOptions options) {
        super(options);
        traceExporter = new AzureMonitorExporterBuilder().buildTraceExporter();
    }
}
