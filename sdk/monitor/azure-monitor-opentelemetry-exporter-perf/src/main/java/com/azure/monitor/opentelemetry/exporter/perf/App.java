package com.azure.monitor.opentelemetry.exporter.perf;

import com.azure.perf.test.core.PerfStressProgram;

public class App {

    public static void main(String[] args) {
        PerfStressProgram.run(new Class<?>[]{AzureMonitorTraceExporterTest.class}, args);
    }
}
