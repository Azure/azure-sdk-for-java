package com.azure.monitor.opentelemetry.exporter.perf;

import com.azure.perf.test.core.PerfStressOptions;
import io.opentelemetry.sdk.trace.data.SpanData;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Collections;

public class AzureMonitorTraceExporterTest extends ServiceTest<PerfStressOptions> {

    private final Collection<SpanData> spans;

    /**
     * Creates an instance of performance test.
     * @param options the options configured for the test.
     */
    public AzureMonitorTraceExporterTest(PerfStressOptions options) {
        super(options);
        spans = getSpans();
    }

    @Override
    public void run() {
        traceExporter.export(spans);
    }

    private Collection<SpanData> getSpans() {
        return Collections.singletonList(new RemoteDependencySpanData());
    }

    @Override
    public Mono<Void> runAsync() {
        traceExporter.export(spans);
        return Mono.empty();
    }

}
